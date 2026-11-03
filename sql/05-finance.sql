-- =============================================================
-- 煤炭销售管理系统 · 模块 05：财务
-- 表：settlement / settlement_item / payment / write_off（4 张）
-- 说明：结算单是「总账」，结算明细是「分账」；回款与结算单多对多经核销明细
-- =============================================================

DROP TABLE IF EXISTS write_off;
DROP TABLE IF EXISTS settlement_item;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS settlement;

-- -------------------------------------------------------------
-- 1. 结算单表 settlement（总账）
-- -------------------------------------------------------------
CREATE TABLE settlement (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    settle_no     VARCHAR(32)   NOT NULL COMMENT '结算单号（JS+年月日+流水）',
    contract_id   BIGINT        NOT NULL COMMENT '合同 id（结算单挂合同，不挂订单）',
    customer_id   BIGINT        NOT NULL COMMENT '客户 id（冗余快照）',
    total_qty     DECIMAL(12,3) NOT NULL DEFAULT 0 COMMENT '结算总净重（吨）',
    total_amount  DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '结算总额（元，含税）= Σ明细金额',
    tax_amount    DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '税额（元）',
    status        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0 待审核 / 1 已审核 / 2 已作废',
    create_by     BIGINT        DEFAULT NULL COMMENT '创建人（销售员，用户 id）',
    audit_by      BIGINT        DEFAULT NULL COMMENT '审核人（财务，用户 id）',
    audit_time    DATETIME      DEFAULT NULL COMMENT '审核时间',
    remark        VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_settle_no (settle_no),
    KEY idx_contract_id (contract_id),
    KEY idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算单表';

-- -------------------------------------------------------------
-- 2. 结算明细表 settlement_item（分账，一行对应一张磅单）
-- -------------------------------------------------------------
CREATE TABLE settlement_item (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    settle_id     BIGINT        NOT NULL COMMENT '结算单 id',
    ticket_id     BIGINT        NOT NULL COMMENT '磅单 id（一张磅单最多被结算一次）',
    product_id    BIGINT        NOT NULL COMMENT '煤种商品 id',
    net_weight    DECIMAL(12,3) NOT NULL COMMENT '净重（吨），从磅单抄来',
    price         DECIMAL(10,2) NOT NULL COMMENT '单价（元/吨），默认合同价可改',
    amount        DECIMAL(14,2) NOT NULL COMMENT '金额（元）= 净重 × 单价',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ticket_id (ticket_id),
    KEY idx_settle_id (settle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算明细表';

-- -------------------------------------------------------------
-- 3. 回款表 payment
-- -------------------------------------------------------------
CREATE TABLE payment (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    payment_no    VARCHAR(32)   NOT NULL COMMENT '回款单号（HK+年月日+流水）',
    customer_id   BIGINT        NOT NULL COMMENT '客户 id',
    amount        DECIMAL(14,2) NOT NULL COMMENT '回款金额（元）',
    pay_type      TINYINT       NOT NULL DEFAULT 1 COMMENT '付款方式：1 银行转账 / 2 现金 / 3 承兑汇票',
    pay_time      DATETIME      DEFAULT NULL COMMENT '付款时间',
    remark        VARCHAR(200)  DEFAULT NULL COMMENT '备注（银行回单号等）',
    create_by     BIGINT        DEFAULT NULL COMMENT '登记人（财务，用户 id）',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回款表';

-- -------------------------------------------------------------
-- 4. 核销明细表 write_off（回款冲抵结算单，多对多中间表）
-- -------------------------------------------------------------
CREATE TABLE write_off (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    payment_id    BIGINT        NOT NULL COMMENT '回款 id',
    settle_id     BIGINT        NOT NULL COMMENT '结算单 id',
    amount        DECIMAL(14,2) NOT NULL COMMENT '本次冲抵金额（元）',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_payment_id (payment_id),
    KEY idx_settle_id (settle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回款核销明细表';
