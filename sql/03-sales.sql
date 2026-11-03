-- =============================================================
-- 煤炭销售管理系统 · 模块 03：销售
-- 表：contract / sale_order / weigh_ticket（3 张）
-- 说明：核心单据链。合同约定总量与价格，订单计划量，磅单实发量
-- =============================================================

DROP TABLE IF EXISTS weigh_ticket;
DROP TABLE IF EXISTS sale_order;
DROP TABLE IF EXISTS contract;

-- -------------------------------------------------------------
-- 1. 合同表 contract
-- -------------------------------------------------------------
CREATE TABLE contract (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    contract_no   VARCHAR(32)   NOT NULL COMMENT '合同编号（HT+年月日+流水）',
    customer_id   BIGINT        NOT NULL COMMENT '客户 id',
    product_id    BIGINT        NOT NULL COMMENT '煤种商品 id',
    sign_date     DATE          DEFAULT NULL COMMENT '签订日期',
    start_date    DATE          DEFAULT NULL COMMENT '有效期起',
    end_date      DATE          DEFAULT NULL COMMENT '有效期止',
    total_qty     DECIMAL(12,3) NOT NULL COMMENT '约定总量（吨）',
    price         DECIMAL(10,2) NOT NULL COMMENT '含税单价（元/吨）',
    tax_rate      DECIMAL(4,2)  NOT NULL DEFAULT 13.00 COMMENT '税率（%）',
    qnet_ar       DECIMAL(8,0)  DEFAULT NULL COMMENT '约定热值（kcal/kg）',
    short_rate    DECIMAL(4,2)  NOT NULL DEFAULT 5.00 COMMENT '溢短装比例（%）',
    pay_type      TINYINT       NOT NULL DEFAULT 3 COMMENT '付款方式：1 预付 / 2 货到付款 / 3 账期',
    credit_days   INT           NOT NULL DEFAULT 0 COMMENT '账期天数（pay_type=3 时有效）',
    transport_bear TINYINT      NOT NULL DEFAULT 1 COMMENT '运费承担：1 客户自提 / 2 供方送货',
    status        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0 草稿 / 1 待审核 / 2 执行中 / 3 已完成 / 4 已终止',
    create_by     BIGINT        DEFAULT NULL COMMENT '创建人（用户 id）',
    audit_by      BIGINT        DEFAULT NULL COMMENT '审核人（用户 id）',
    audit_time    DATETIME      DEFAULT NULL COMMENT '审核时间',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_contract_no (contract_no),
    KEY idx_customer_id (customer_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- -------------------------------------------------------------
-- 2. 订单表 sale_order
-- -------------------------------------------------------------
CREATE TABLE sale_order (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no      VARCHAR(32)   NOT NULL COMMENT '订单编号（DD+年月日+流水）',
    contract_id   BIGINT        NOT NULL COMMENT '合同 id（必填，挂合同）',
    customer_id   BIGINT        NOT NULL COMMENT '客户 id（冗余快照）',
    product_id    BIGINT        NOT NULL COMMENT '煤种商品 id（从合同带出）',
    plan_qty      DECIMAL(12,3) NOT NULL COMMENT '计划数量（吨）',
    shipped_qty   DECIMAL(12,3) NOT NULL DEFAULT 0 COMMENT '已发运量（吨），磅单完成时累加',
    settled_qty   DECIMAL(12,3) NOT NULL DEFAULT 0 COMMENT '已结算量（吨），结算单审核后累加',
    price         DECIMAL(10,2) NOT NULL COMMENT '价格快照（元/吨），默认取合同价',
    price_remark  VARCHAR(200)  DEFAULT NULL COMMENT '手动改价的理由',
    delivery_date DATE          DEFAULT NULL COMMENT '要求发货日期',
    plate_no      VARCHAR(20)   DEFAULT NULL COMMENT '指定车牌，可空',
    status        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1 待发运 / 2 部分发运 / 3 已完成 / 4 已取消',
    create_by     BIGINT        DEFAULT NULL COMMENT '创建人（用户 id）',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_contract_id (contract_id),
    KEY idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';

-- -------------------------------------------------------------
-- 3. 磅单表 weigh_ticket
-- -------------------------------------------------------------
CREATE TABLE weigh_ticket (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    weight_no     VARCHAR(32)   NOT NULL COMMENT '磅单号（BD+年月日+流水）',
    order_id      BIGINT        NOT NULL COMMENT '订单 id（必填，磅单必须挂订单）',
    product_id    BIGINT        NOT NULL COMMENT '煤种商品 id（从订单带出）',
    plate_no      VARCHAR(20)   NOT NULL COMMENT '车牌号',
    driver        VARCHAR(50)   DEFAULT NULL COMMENT '司机',
    tare_weight   DECIMAL(12,3) DEFAULT NULL COMMENT '皮重（吨），空车过磅',
    tare_time     DATETIME      DEFAULT NULL COMMENT '空车过磅时间',
    tare_source   TINYINT       NOT NULL DEFAULT 1 COMMENT '皮重来源：1 现场称重 / 2 备案套用',
    gross_weight  DECIMAL(12,3) DEFAULT NULL COMMENT '毛重（吨），重车过磅',
    gross_time    DATETIME      DEFAULT NULL COMMENT '重车过磅时间',
    net_weight    DECIMAL(12,3) DEFAULT NULL COMMENT '净重（吨）= 毛重 − 皮重',
    status        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0 待称皮重 / 1 已称皮重 / 2 已完成 / 3 已作废',
    weighman_id   BIGINT        DEFAULT NULL COMMENT '司磅员（用户 id）',
    cancel_reason VARCHAR(200)  DEFAULT NULL COMMENT '作废原因（作废时必填）',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_weight_no (weight_no),
    KEY idx_order_id (order_id),
    KEY idx_plate_no (plate_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='磅单表（汽车衡计量单）';
