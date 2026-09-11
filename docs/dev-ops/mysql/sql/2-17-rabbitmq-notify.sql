-- RabbitMQ 拼团成功通知字段升级脚本。
-- 该脚本用于从原 HTTP-only 表结构升级；请在目标数据库上执行一次。

ALTER TABLE `group_buy_order`
    ADD COLUMN `notify_type` varchar(8) NOT NULL DEFAULT 'HTTP'
        COMMENT '通知类型（HTTP、MQ）' AFTER `valid_end_time`;

ALTER TABLE `notify_task`
    ADD COLUMN `notify_type` varchar(8) NOT NULL DEFAULT 'HTTP'
        COMMENT '通知类型（HTTP、MQ）' AFTER `team_id`,
    ADD COLUMN `notify_mq` varchar(64) DEFAULT NULL
        COMMENT 'MQ 路由键' AFTER `notify_type`;

