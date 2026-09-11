package com.lkh.demo.notify;

import java.util.*;
import java.util.concurrent.*;

/**
 * ================================================================================================
 * 【生产级】拼团回调通知任务 —— 完整演示（自包含，仅供阅读，不接入工程）
 * ================================================================================================
 *
 * 这份文件把"回调通知任务"在生产里真正会怎么写，从头到尾串一遍。
 * 所有依赖（DAO、Redis、HTTP、线程池）都用内联的最小接口/桩代替，只为了让"逻辑流"完整可读。
 *
 * 它和教程 / 你当前实现的核心区别（也是生产真正在乎的点），共 8 条，文中用【生产点N】标注：
 *
 *   【生产点1】HTTP 状态 与 业务状态 分两层判断（先看通信成不成，再看对端业务返回啥）
 *   【生产点2】重试不是"立即再发"，而是"指数退避 + next_retry_time 调度"（教程缺这块，最重要）
 *   【生产点3】状态更新带 CAS 条件（UPDATE ... WHERE status=旧值），靠 updateCount==1 保证幂等/防并发
 *   【生产点4】回调请求签名（HMAC），对端能验签防伪造（真实 webhook 必备）
 *   【生产点5】分布式锁只保护"发送那一下"，配合幂等，多实例安全
 *   【生产点6】批量捞 + 有界线程池并发发送，单条慢不拖垮整批
 *   【生产点7】超时/IO 失败归为"可重试"，业务永久失败(对端明确拒绝/超次数)归为"死信"，二者分开
 *   【生产点8】双触发：结算成功后立即发(提时效) + 定时任务兜底(保最终一致)，本质是 Outbox 模式
 *
 * ================================================================================================
 */
public class ProductionNotifyJobDemo {

    // ============================================================================================
    // 一、枚举与数据模型
    // ============================================================================================

    /**
     * 任务状态机：
     *   INIT(0)  ──发送成功──► SUCCESS(1)
     *     │
     *     ├──对端要求重试/超时──► RETRY(2) ──(到达最大次数)──► DEAD(4) 死信，人工介入/告警
     *     │         ▲                │
     *     │         └────────────────┘  (退避时间到了再发)
     *     │
     *     └──对端明确永久拒绝──────► FAIL(3) 业务级失败，不再重试
     *
     * 注意 RETRY 和 DEAD/FAIL 的区别：RETRY 是"还会再试"，FAIL/DEAD 是"终态、不再动"。
     */
    enum NotifyStatus {
        INIT(0), SUCCESS(1), RETRY(2), FAIL(3), DEAD(4);
        final int code;
        NotifyStatus(int code) { this.code = code; }
    }

    /** 对端回调接口返回给我们的"业务结果"（注意：这是 body 内容，不是 HTTP 状态码） */
    enum NotifyResult {
        /** 对端确认已成功处理 */            ACK,
        /** 对端临时处理失败，请稍后重试 */    RETRY,
        /** 对端明确永久拒绝（如订单已取消）*/ REJECT,
        /** 我们这边没真正发出去（没抢到锁/未到退避时间），本轮跳过，不算一次尝试 */ SKIP
    }

    /** notify_task 表对应的领域对象 */
    static class NotifyTask {
        Long id;
        String teamId;          // 拼团队伍ID（业务唯一键）
        Long activityId;
        String notifyUrl;       // 回调地址
        String parameterJson;   // 回调报文
        int notifyCount;        // 已尝试次数
        NotifyStatus status;    // 当前状态
        long nextRetryTime;     // 【生产点2】下次允许重试的时间戳(ms)，退避调度的核心
        long version;           // 乐观锁版本号（可选，本 demo 用状态 CAS 代替）

        String lockKey() { return "notify_task_lock_" + teamId; }
    }

    // ============================================================================================
    // 二、基础设施桩（生产里分别是 MyBatis Mapper / Redisson / OkHttp / 线程池，这里只给签名）
    // ============================================================================================

    interface NotifyTaskDao {
        /** 捞一批"该发的"任务：状态 in (INIT, RETRY) 且 next_retry_time <= now，按 id 升序，limit N。
         *  【生产点2】关键是带上 next_retry_time <= now，没到退避时间的不捞。 */
        List<NotifyTask> queryExecutableTasks(int limit, long now);

        /** 按 teamId 捞该发的任务（即时回调路径用），同样过滤状态与退避时间 */
        List<NotifyTask> queryExecutableTasksByTeamId(String teamId, long now);

        /** 【生产点3】CAS 改成功：UPDATE notify_task SET status=SUCCESS, update_time=now
         *               WHERE team_id=? AND status IN (INIT, RETRY)。返回影响行数。 */
        int casToSuccess(String teamId);

        /** 【生产点3】【生产点2】CAS 改重试 + 次数自增 + 写下次退避时间：
         *  UPDATE ... SET status=RETRY, notify_count=notify_count+1, next_retry_time=?
         *         WHERE team_id=? AND status IN (INIT, RETRY)。 */
        int casToRetry(String teamId, long nextRetryTime);

        /** CAS 改死信（超过最大次数）：status=DEAD */
        int casToDead(String teamId);

        /** CAS 改业务失败（对端永久拒绝）：status=FAIL */
        int casToFail(String teamId);
    }

    interface DistributedLock {
        boolean tryLock(String key, long leaseSeconds);
        void unlock(String key);
    }

    interface HttpCaller {
        /** 返回 HTTP 响应（状态码 + body），通信失败（超时/拒绝/DNS）直接抛 IOException 类异常 */
        HttpResponse postJson(String url, String body, Map<String, String> headers) throws Exception;
    }

    static class HttpResponse {
        int statusCode;
        String body;
        HttpResponse(int statusCode, String body) { this.statusCode = statusCode; this.body = body; }
        boolean isSuccessful() { return statusCode >= 200 && statusCode < 300; }
    }

    // ============================================================================================
    // 三、回调网关 —— 只负责"发一次 HTTP，翻译成业务结果"
    //     【生产点1】【生产点4】【生产点7】都在这一层
    // ============================================================================================

    static class GroupBuyNotifyGateway {

        private final HttpCaller httpCaller;
        private final String signSecret;   // 与对端约定的签名密钥

        GroupBuyNotifyGateway(HttpCaller httpCaller, String signSecret) {
            this.httpCaller = httpCaller;
            this.signSecret = signSecret;
        }

        /**
         * 发一次回调，返回"业务结果" NotifyResult。
         * 注意：这里把"通信层异常"统一翻译成 RETRY，绝不把异常抛给上层 —— 让上层只面对清晰的 4 种结果。
         */
        NotifyResult notifyOnce(NotifyTask task) {
            try {
                // 【生产点4】给报文签名：对端用同样的密钥验签，确认请求确实来自我们、未被篡改
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Timestamp", String.valueOf(currentTimeMillis()));
                headers.put("X-Sign", hmacSha256(task.parameterJson + headers.get("X-Timestamp"), signSecret));

                HttpResponse resp = httpCaller.postJson(task.notifyUrl, task.parameterJson, headers);

                // 【生产点1】第一层：先判 HTTP 状态。非 2xx 说明"通信/服务端层面"出了问题 ——
                //            这跟"对端业务怎么处理"是两码事，body 此时往往是错误页，不能拿去解析业务码。
                if (!resp.isSuccessful()) {
                    // 4xx(客户端错误，比如地址/参数错)重试也没用，但为简化这里统一按可重试，靠次数兜底；
                    // 更严格的生产实现会：5xx -> RETRY，4xx -> REJECT(直接判失败)。
                    log("回调 HTTP 非2xx, teamId=%s code=%d", task.teamId, resp.statusCode);
                    return NotifyResult.RETRY;
                }

                // 【生产点1】第二层：HTTP 200 了，body 才是对端"业务上"给的回执，按约定协议解析。
                //            约定对端返回 JSON: {"code":"ACK"} / {"code":"RETRY"} / {"code":"REJECT"}
                return parseBusinessResult(resp.body);

            } catch (Exception e) {
                // 【生产点7】超时 / 连接拒绝 / DNS 失败 等通信异常 -> 可重试。不抛出去。
                log("回调通信异常, teamId=%s err=%s", task.teamId, e.getMessage());
                return NotifyResult.RETRY;
            }
        }

        private NotifyResult parseBusinessResult(String body) {
            // 真实实现用 JSON 解析；这里简化按关键字判断
            if (body == null) return NotifyResult.RETRY;
            if (body.contains("ACK")) return NotifyResult.ACK;
            if (body.contains("REJECT")) return NotifyResult.REJECT;
            return NotifyResult.RETRY; // 未知 body 当可重试，避免误判成功
        }
    }

    // ============================================================================================
    // 四、领域服务 —— 任务调度的核心逻辑
    //     【生产点2】【生产点3】【生产点5】【生产点6】在这里
    // ============================================================================================

    static class TradeNotifyService {

        private static final int MAX_RETRY = 5;        // 最大尝试次数，超过进死信
        private static final int BATCH_SIZE = 50;      // 每批捞多少
        private static final long LOCK_LEASE = 10;     // 锁租约(秒)，必须 > 单次回调最大耗时

        private final NotifyTaskDao dao;
        private final DistributedLock lock;
        private final GroupBuyNotifyGateway gateway;
        private final ExecutorService pool;            // 【生产点6】有界线程池，并发发送

        TradeNotifyService(NotifyTaskDao dao, DistributedLock lock,
                           GroupBuyNotifyGateway gateway, ExecutorService pool) {
            this.dao = dao; this.lock = lock; this.gateway = gateway; this.pool = pool;
        }

        // ---- 入口1：定时任务调用（兜底，保最终一致） ----
        Map<String, Integer> execNotifyJob() {
            List<NotifyTask> tasks = dao.queryExecutableTasks(BATCH_SIZE, currentTimeMillis());
            return execBatch(tasks);
        }

        // ---- 入口2：结算成功后立即调用（提时效），见【生产点8】 ----
        Map<String, Integer> execNotifyJobByTeamId(String teamId) {
            List<NotifyTask> tasks = dao.queryExecutableTasksByTeamId(teamId, currentTimeMillis());
            return execBatch(tasks);
        }

        /**
         * 【生产点6】并发处理一批任务：提交到线程池，等全部完成后汇总。
         * 比教程的 for 串行发送强在：50 条里有 1 条卡住(等到超时)，不会把其它 49 条堵死。
         */
        private Map<String, Integer> execBatch(List<NotifyTask> tasks) {
            if (tasks == null || tasks.isEmpty()) return summarize(0, 0, 0, 0);

            List<Future<NotifyResult>> futures = new ArrayList<>();
            for (NotifyTask task : tasks) {
                futures.add(pool.submit(() -> handleOne(task)));
            }

            int ack = 0, retry = 0, dead = 0, reject = 0;
            for (Future<NotifyResult> f : futures) {
                NotifyResult r;
                try { r = f.get(); } catch (Exception e) { r = NotifyResult.SKIP; }
                switch (r) {
                    case ACK:    ack++;    break;
                    case RETRY:  retry++;  break;
                    case REJECT: reject++; break;
                    default:     /* SKIP 不计入有效处理 */ break;
                }
            }
            log("本批处理完成 total=%d ack=%d retry=%d reject=%d", tasks.size(), ack, retry, reject);
            return summarize(tasks.size(), ack, retry, reject + dead);
        }

        /**
         * 处理单条任务：抢锁 → 发送 → 按结果落库。
         * 返回的 NotifyResult 仅用于统计；真正的"状态推进"在各 CAS 更新里完成。
         */
        private NotifyResult handleOne(NotifyTask task) {
            // 【生产点5】分布式锁：多实例 / 即时回调与定时任务并发时，保证同一 teamId 同时只有一个在发
            if (!lock.tryLock(task.lockKey(), LOCK_LEASE)) {
                return NotifyResult.SKIP; // 没抢到 = 别人正在处理，本轮跳过，不算尝试
            }
            try {
                NotifyResult result = gateway.notifyOnce(task);

                switch (result) {
                    case ACK:
                        // 【生产点3】CAS：只有当前还是 INIT/RETRY 才改成 SUCCESS。
                        //            若返回 0，说明别的实例已经处理过了 —— 不重复计数，天然幂等。
                        dao.casToSuccess(task.teamId);
                        return NotifyResult.ACK;

                    case REJECT:
                        // 【生产点7】对端业务永久拒绝（如拼团已关闭）→ 终态 FAIL，不再重试
                        dao.casToFail(task.teamId);
                        return NotifyResult.REJECT;

                    case RETRY:
                        if (task.notifyCount + 1 >= MAX_RETRY) {
                            // 超过最大次数 → 死信，等待告警/人工。绝不无限重试。
                            dao.casToDead(task.teamId);
                            return NotifyResult.RETRY;
                        } else {
                            // 【生产点2】指数退避：第n次失败后，隔 backoff(n) 才允许再发。
                            //            避免对端抖动时被我们高频重试打爆。
                            long nextTime = currentTimeMillis() + backoffMillis(task.notifyCount + 1);
                            dao.casToRetry(task.teamId, nextTime);
                            return NotifyResult.RETRY;
                        }

                    case SKIP:
                    default:
                        return NotifyResult.SKIP;
                }
            } finally {
                lock.unlock(task.lockKey());
            }
        }

        /**
         * 【生产点2】指数退避 + 上限。第1次重试 2s，之后翻倍：2s,4s,8s,16s,...，封顶 5 分钟。
         * 真实生产常再叠加一点随机抖动(jitter)，避免大量任务同一时刻一起重试(惊群)。
         */
        private long backoffMillis(int attempt) {
            long base = 2000L * (1L << Math.min(attempt - 1, 8)); // 2s 起，最多左移8位
            return Math.min(base, 5 * 60 * 1000L);                // 封顶 5 分钟
        }

        private Map<String, Integer> summarize(int wait, int ack, int retry, int fail) {
            Map<String, Integer> m = new HashMap<>();
            m.put("waitCount", wait);
            m.put("successCount", ack);
            m.put("retryCount", retry);
            m.put("errorCount", fail);
            return m;
        }
    }

    // ============================================================================================
    // 五、触发器 —— 【生产点8】双触发：即时 + 定时兜底（Outbox 模式）
    // ============================================================================================

    /**
     * 结算服务：拼团达成后，先把回调任务落库(Outbox)，再尝试"立即发一次"。
     *
     *   关键顺序：先在 同一个事务 里写好 notify_task（status=INIT），事务提交后再触发即时回调。
     *   就算即时回调失败、甚至应用此刻宕机 —— 任务已经在库里，定时任务一定会补发。
     *   这就是"本地消息表 / Outbox"：业务数据与待发消息同事务落库，保证不丢。
     */
    static class TradeSettlementService {
        private final TradeNotifyService notifyService;
        TradeSettlementService(TradeNotifyService notifyService) { this.notifyService = notifyService; }

        void onGroupBuyCompleted(String teamId /*, 事务里已写入 notify_task */) {
            // 1) 此处省略：在结算事务内完成 订单结算 + 写 notify_task(INIT)。事务已提交。

            // 2) 【生产点8】立即触发一次定向回调，把时效从"最多等一个定时周期"提前到"秒级"。
            //    失败也无所谓：状态仍是 INIT/RETRY，定时任务会兜底。所以这里异步、吞掉异常即可。
            try {
                notifyService.execNotifyJobByTeamId(teamId);
            } catch (Exception e) {
                log("即时回调失败，转由定时任务兜底 teamId=%s", teamId);
            }
        }
    }

    /**
     * 定时任务：固定频率扫描"该发"的任务做兜底补偿。
     * 生产里通常用 XXL-JOB / ElasticJob 这类分布式调度，而不是单机 @Scheduled ——
     * 这样不依赖"恰好这台机器活着"，且天然分片、可重跑、有调度台可观测。
     */
    static class NotifyJobTrigger {
        private final TradeNotifyService notifyService;
        NotifyJobTrigger(TradeNotifyService notifyService) { this.notifyService = notifyService; }

        // 等价于 @Scheduled(cron="0/15 * * * * ?") 或 XXL-JOB 的 JobHandler
        void run() {
            Map<String, Integer> result = notifyService.execNotifyJob();
            log("定时回调任务执行结果: %s", result);
        }
    }

    // ============================================================================================
    // 六、工具桩
    // ============================================================================================
    static long currentTimeMillis() { return System.currentTimeMillis(); }
    static String hmacSha256(String data, String secret) { return "sign(" + data.hashCode() + ")"; /* 示意 */ }
    static void log(String fmt, Object... args) { System.out.println(String.format(fmt, args)); }
}
