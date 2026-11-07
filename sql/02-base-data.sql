-- =============================================================
SET NAMES utf8mb4;

-- 煤炭销售管理系统 · 模块 02：基础数据
-- 表：customer / coal_product / sys_dict（3 张）
-- 含：示例客户、示例煤种、业务参数字典的初始化数据
-- =============================================================

DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS coal_product;
DROP TABLE IF EXISTS sys_dict;

-- -------------------------------------------------------------
-- 1. 客户表 customer
-- -------------------------------------------------------------
CREATE TABLE customer (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    customer_name   VARCHAR(100)  NOT NULL COMMENT '客户单位名称',
    contact_name    VARCHAR(50)   DEFAULT NULL COMMENT '联系人',
    phone           VARCHAR(20)   DEFAULT NULL COMMENT '联系电话',
    address         VARCHAR(200)  DEFAULT NULL COMMENT '收货地址',
    invoice_info    VARCHAR(200)  DEFAULT NULL COMMENT '开票信息（税号/开户行等）',
    credit_limit    DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '信用额度（元）',
    region          VARCHAR(50)   DEFAULT NULL COMMENT '地区',
    remark          VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    create_by       BIGINT        DEFAULT NULL COMMENT '创建人（用户 id）',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删',
    PRIMARY KEY (id),
    KEY idx_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- -------------------------------------------------------------
-- 2. 煤种商品表 coal_product
-- -------------------------------------------------------------
CREATE TABLE coal_product (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_name  VARCHAR(100)  NOT NULL COMMENT '煤种/商品名称（如无烟煤、烟煤）',
    spec          VARCHAR(100)  DEFAULT NULL COMMENT '规格（如 3-6 块、粉煤）',
    qnet_ar       DECIMAL(8,0)  DEFAULT NULL COMMENT '收到基低位发热量（kcal/kg）',
    sulfur        DECIMAL(6,2)  DEFAULT NULL COMMENT '硫分（%）',
    ash           DECIMAL(6,2)  DEFAULT NULL COMMENT '灰分（%）',
    origin        VARCHAR(100)  DEFAULT NULL COMMENT '产地',
    base_price    DECIMAL(14,2) DEFAULT NULL COMMENT '基准价（元/吨）',
    safe_stock    DECIMAL(12,3) NOT NULL DEFAULT 0 COMMENT '安全库存（吨），触发预警线',
    status        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1 上架 / 0 下架',
    remark        VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='煤种商品表';

-- -------------------------------------------------------------
-- 3. 数据字典表 sys_dict
-- -------------------------------------------------------------
CREATE TABLE sys_dict (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    dict_type   VARCHAR(50)  NOT NULL COMMENT '字典类型（如 biz_param）',
    dict_key    VARCHAR(50)  NOT NULL COMMENT '字典键',
    dict_value  VARCHAR(200) NOT NULL COMMENT '字典值',
    remark      VARCHAR(200) DEFAULT NULL COMMENT '说明',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict (dict_type, dict_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- =============================================================
-- 初始化数据：客户（示例）
-- =============================================================
INSERT INTO customer (id, customer_name, contact_name, phone, address, region, credit_limit) VALUES
(1, '华能某电厂', '王经理', '13800000001', '内蒙古鄂尔多斯市', '内蒙古', 500000.00),
(2, '某钢铁集团', '李部长', '13800000002', '河北省唐山市',     '河北',   300000.00),
(3, '某水泥厂',   '张主任', '13800000003', '山西省大同市',     '山西',   200000.00);

-- =============================================================
-- 初始化数据：煤种商品（示例）
-- =============================================================
INSERT INTO coal_product (id, product_name, spec, qnet_ar, sulfur, ash, origin, base_price, safe_stock) VALUES
(1, '无烟煤', '3-6 块', 6500, 0.50, 12.00, '山西晋城',       780.00, 1000.000),
(2, '烟煤',   '末煤',   5500, 1.20, 18.00, '内蒙古鄂尔多斯', 520.00, 1500.000),
(3, '褐煤',   '末煤',   3500, 0.80, 25.00, '内蒙古霍林河',   320.00, 800.000);

-- =============================================================
-- 初始化数据：业务参数字典
-- =============================================================
INSERT INTO sys_dict (dict_type, dict_key, dict_value, remark) VALUES
('biz_param', 'order_over_delivery_rate', '0.03',   '超发审批阈值（比例），默认 3%'),
('biz_param', 'net_weight_min',           '20',     '磅单净重下限（吨）'),
('biz_param', 'net_weight_max',           '40',     '磅单净重上限（吨）'),
('biz_param', 'stock_warn_threshold',     '0',      '库存预警阈值（吨），0 表示关闭'),
('biz_param', 'pwd_error_lock_count',     '5',      '密码错误锁定次数'),
('biz_param', 'pwd_error_lock_minutes',   '15',     '锁定时长（分钟）'),
('biz_param', 'jwt_expire_hours',         '2',      'JWT 过期时长（小时）'),
('biz_param', 'tare_deviation_limit',     '0.5',    '皮重异常波动阈值（吨）'),
('biz_param', 'default_tax_rate',         '13.00',  '默认税率（%）');
