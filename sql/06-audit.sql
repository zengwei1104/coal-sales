-- =============================================================
-- 煤炭销售管理系统 · 模块 06：审计
-- 表：sys_log（1 张）
-- 说明：操作日志，只记关键动作（登录、审核、作废、删除、盘点调整）
-- =============================================================

DROP TABLE IF EXISTS sys_log;

-- -------------------------------------------------------------
-- 1. 操作日志表 sys_log
-- -------------------------------------------------------------
CREATE TABLE sys_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       BIGINT       DEFAULT NULL COMMENT '操作用户 id',
    username      VARCHAR(50)  DEFAULT NULL COMMENT '操作用户名（冗余快照）',
    operation     VARCHAR(100) NOT NULL COMMENT '操作内容（如「登录」「审核合同」）',
    method        VARCHAR(200) DEFAULT NULL COMMENT '请求方法（类名.方法名）',
    params        TEXT         COMMENT '请求参数（JSON）',
    ip            VARCHAR(50)  DEFAULT NULL COMMENT '来源 IP',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '结果：1 成功 / 0 失败',
    cost_time     INT          DEFAULT NULL COMMENT '耗时（毫秒）',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
