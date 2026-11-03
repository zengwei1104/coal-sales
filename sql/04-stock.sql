-- =============================================================
-- 煤炭销售管理系统 · 模块 04：库存
-- 表：stock / stock_record（2 张）
-- 说明：库存是「结果」，流水是「过程」。库存 = 期初 + 入库 − 出库 ± 盘点
--       流水来源用「通用指针」（source_type + source_id），方案 A
-- =============================================================

DROP TABLE IF EXISTS stock_record;
DROP TABLE IF EXISTS stock;

-- -------------------------------------------------------------
-- 1. 库存表 stock（每种煤当前还剩多少，一煤一条）
-- -------------------------------------------------------------
CREATE TABLE stock (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_id    BIGINT        NOT NULL COMMENT '煤种商品 id',
    total_qty     DECIMAL(12,3) NOT NULL DEFAULT 0 COMMENT '当前库存（吨）',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- -------------------------------------------------------------
-- 2. 库存流水表 stock_record（每次变动一笔，只增不改）
-- -------------------------------------------------------------
CREATE TABLE stock_record (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_id    BIGINT        NOT NULL COMMENT '煤种商品 id',
    source_type   VARCHAR(30)   NOT NULL COMMENT '来源类型：INBOUND 入库 / TICKET 磅单出库 / CHECK 盘点调整',
    source_id     BIGINT        DEFAULT NULL COMMENT '来源单据 id（对应上面类型）',
    change_qty    DECIMAL(12,3) NOT NULL COMMENT '变动数量（吨），正=入库，负=出库',
    before_qty    DECIMAL(12,3) NOT NULL DEFAULT 0 COMMENT '变动前库存（吨）',
    after_qty     DECIMAL(12,3) NOT NULL DEFAULT 0 COMMENT '变动后库存（吨）',
    remark        VARCHAR(200)  DEFAULT NULL COMMENT '备注（盘点原因等）',
    operator_id   BIGINT        DEFAULT NULL COMMENT '操作人（用户 id）',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（流水一般不做逻辑删除，保留字段以备）',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_source (source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';
