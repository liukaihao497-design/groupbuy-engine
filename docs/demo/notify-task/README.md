# 拼团回调通知任务 —— 生产级写法演示

配套代码：[`ProductionNotifyJobDemo.java`](./ProductionNotifyJobDemo.java)（自包含，仅供阅读）

这份 demo 不是要替换你的实现，而是让你看到：同样一个"回调通知任务"，
**教程版（够用、能跑）→ 生产版（抗故障、可观测、不打爆对端）** 之间到底差在哪。

---

## 一、三版对照

| 维度 | 教程版 | 你的版本 | 生产版（本 demo） |
|------|--------|----------|------------------|
| 状态判断 | HTTP success/error/null 一层 | 任务状态 complete/retry/fail/null | **HTTP 状态 + 业务状态 两层**【点1】 |
| 重试节奏 | 立即被下一轮捞到再发 | 同左 | **指数退避 + `next_retry_time` 调度**【点2】 |
| 状态更新幂等 | `updateCount==1` 守卫 | 同左 | **CAS：`UPDATE...WHERE status IN(INIT,RETRY)`**【点3】 |
| 报文安全 | 无 | 无 | **HMAC 签名，对端可验签**【点4】 |
| 多实例并发 | 分布式锁 | 分布式锁 | 分布式锁 + CAS 双保险【点5】 |
| 发送方式 | for 串行 | for 串行 | **有界线程池并发**【点6】 |
| 失败分类 | error→按次数 retry/error | retry/fail | **可重试 vs 死信(DEAD) vs 业务拒绝(FAIL) 三分**【点7】 |
| 触发方式 | 即时 + 单机定时 | 仅定时(还没接即时) | **即时 + 分布式调度兜底(Outbox)**【点8】 |

---

## 二、生产真正多出来的东西（按重要性）

### 【点2】指数退避——教程最大的缺口
教程/你的实现里，重试任务会被**下一个扫描周期立刻再发**。如果对端正在抖动/挂了，
你会以"每个周期一次"的频率持续砸它，对端恢复更难，你的日志也被刷爆。

生产做法：每条任务多一列 `next_retry_time`。失败时不仅 `notify_count+1`，
还写入 `下次允许重试的时间 = now + backoff(次数)`，退避序列如 `2s,4s,8s,16s...` 封顶 5 分钟，
再加随机抖动避免"惊群"。扫描 SQL 加一个条件：`AND next_retry_time <= now`，没到点的不捞。

### 【点1】HTTP 状态 vs 业务状态，两层分开
- 第一层 `response.isSuccessful()`：通信/服务端层面成没成。非 2xx 时 body 是错误页，**不能**拿去解析业务码。
- 第二层：只有 200 了，body 才是对端业务给的回执（ACK/RETRY/REJECT），按约定协议解析。

这正是你上一轮问的"是不是该加个 HTTP 状态"——是，而且就该分这两层。

### 【点7】三种"失败"要分开，别都当 retry
- **可重试**：超时、5xx、对端说稍后再试 → 进退避重试。
- **死信 DEAD**：重试到上限仍不成 → 停手，发告警，人工/对账兜底。**绝不无限重试**。
- **业务拒绝 FAIL**：对端明确"这单永久不接"（如拼团已关闭）→ 直接终态，不浪费重试次数。

### 【点8】Outbox（本地消息表）——为什么"先落库再发"
结算事务里**同事务**写入 `notify_task(INIT)`，事务提交后才触发即时回调。
这样即便即时回调失败、甚至应用立刻宕机，任务已在库里，定时任务必然补发 → **消息不丢**。
`notify_task` 这张表本质就是 Outbox。即时回调只是"提速优化"，定时任务才是"正确性保证"。

### 【点3】CAS 更新——多实例下的幂等
`UPDATE notify_task SET status=SUCCESS WHERE team_id=? AND status IN (INIT,RETRY)`。
若另一实例已抢先改成 SUCCESS，本次 `updateCount=0`，于是不重复计数、不覆盖终态。
锁负责"同时只有一个在发"，CAS 负责"就算锁失效也不会改错状态"，双保险。

### 【点6】并发发送
串行 for 里只要有一条卡到超时（比如 3s），整批就被它拖慢。
丢进有界线程池并发发、统一 `future.get()` 汇总，单条慢不影响其余。注意线程池要**有界**，别无限扩。

---

## 三、要支持生产版，表结构要加的列

教程的 `notify_task` 基础上补两列：

```sql
ALTER TABLE notify_task
    ADD COLUMN next_retry_time DATETIME      NULL COMMENT '下次允许重试时间(退避调度)' AFTER notify_count,
    ADD COLUMN version         BIGINT        NOT NULL DEFAULT 0 COMMENT '乐观锁版本(可选)';

-- 扫描"该发"的任务（定时任务用）
-- SELECT ... FROM notify_task
--  WHERE notify_status IN (0,2)            -- INIT / RETRY
--    AND (next_retry_time IS NULL OR next_retry_time <= NOW())
--  ORDER BY id ASC LIMIT #{limit};

-- 状态机里增加 DEAD(4) 死信态：notify_status 0初始 1完成 2重试 3业务失败 4死信
```

---

## 四、给你的实操建议（在你现有代码上演进，别推倒）

你当前的 domain 循环逻辑已经对了。要往生产靠，按这个顺序补：

1. **先让它能跑**：补 `INotifyTaskPort` 的 4 个方法 + Mapper SQL + 定时触发器（这步教程范围内）。
2. **加退避**【点2】：`notify_task` 加 `next_retry_time` 列；`setRetryAndIncryCount` 里顺手写下次重试时间；扫描 SQL 加 `next_retry_time <= now`。这一步性价比最高。
3. **HTTP 两层判断**【点1】：网关里加 `isSuccessful()`（你已经在考虑了）。
4. 行有余力再上：死信态【点7】、并发发送【点6】、签名【点4】、换分布式调度【点8】。

> 一句话：**教程教你"功能怎么连通"，生产关心"对端挂了/网络抖了/多实例抢了/应用宕了，你还对不对"。**
> 这份 demo 就是把后面这串"还对不对"补齐的样板。
