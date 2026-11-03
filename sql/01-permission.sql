-- =============================================================
-- 煤炭销售管理系统 · 模块 01：权限
-- 表：sys_user / sys_role / sys_menu / sys_role_menu（4 张）
-- 含：角色、菜单、角色菜单映射的初始化数据
-- =============================================================

DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

-- -------------------------------------------------------------
-- 1. 用户表 sys_user
-- -------------------------------------------------------------
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(50)  NOT NULL COMMENT '登录名',
    password      VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 密文）',
    real_name     VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role_id       BIGINT       NOT NULL COMMENT '所属角色（一对一）',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 禁用',
    last_login_time DATETIME   DEFAULT NULL COMMENT '最后登录时间',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------------------------
-- 2. 角色表 sys_role
-- -------------------------------------------------------------
CREATE TABLE sys_role (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(30) NOT NULL COMMENT '角色编码（ADMIN/MANAGER/SALES/WEIGHMAN/STOCK/FINANCE）',
    remark      VARCHAR(200) DEFAULT NULL COMMENT '备注',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- -------------------------------------------------------------
-- 3. 菜单表 sys_menu
-- -------------------------------------------------------------
CREATE TABLE sys_menu (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT      NOT NULL DEFAULT 0 COMMENT '父菜单 id，0 表示顶级',
    menu_name   VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type   TINYINT     NOT NULL DEFAULT 1 COMMENT '类型：1 目录 / 2 菜单',
    path        VARCHAR(100) DEFAULT NULL COMMENT '前端路由路径',
    component   VARCHAR(100) DEFAULT NULL COMMENT '前端组件路径',
    icon        VARCHAR(50)  DEFAULT NULL COMMENT '图标',
    sort_order  INT         NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 禁用',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- -------------------------------------------------------------
-- 4. 角色菜单表 sys_role_menu
-- -------------------------------------------------------------
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色 id',
    menu_id BIGINT NOT NULL COMMENT '菜单 id',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- =============================================================
-- 初始化数据：角色（6 个）
-- =============================================================
INSERT INTO sys_role (id, role_name, role_code, remark) VALUES
(1, '系统管理员', 'ADMIN',    'IT 角色，管用户、权限、字典、日志，不碰业务'),
(2, '销售经理',   'MANAGER',  '审批合同、超发、信用额度'),
(3, '销售员',     'SALES',    '客户、合同、订单、结算'),
(4, '磅房人员',   'WEIGHMAN', '只做计量录入'),
(5, '库管员',     'STOCK',    '库存、盘点'),
(6, '财务人员',   'FINANCE',  '结算审核、回款');

-- =============================================================
-- 初始化数据：菜单（目录 + 页面，id 手动指定）
-- =============================================================
-- 顶级目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, sort_order) VALUES
(100, 0,   '工作台',    1, '/dashboard',      'dashboard/index',     'HomeFilled', 1),
(200, 0,   '客户与合同', 1, '/biz',            NULL,                  'OfficeBuilding', 2),
(300, 0,   '销售订单',   1, '/order',          NULL,                  'Document', 3),
(400, 0,   '称重发运',   1, '/weigh',          NULL,                  'ScaleToOriginal', 4),
(500, 0,   '产品与库存', 1, '/stock',          NULL,                  'Box', 5),
(600, 0,   '结算与财务', 1, '/finance',        NULL,                  'Money', 6),
(700, 0,   '统计报表',   1, '/report',         NULL,                  'DataLine', 7),
(800, 0,   '系统管理',   1, '/system',         NULL,                  'Setting', 8);

-- 二级页面
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, sort_order) VALUES
(201, 200, '客户管理', 2, '/biz/customer',    'biz/customer/index',    'User', 1),
(202, 200, '合同管理', 2, '/biz/contract',    'biz/contract/index',    'Tickets', 2),
(301, 300, '订单管理', 2, '/order/list',      'order/list/index',      'Document', 1),
(401, 400, '磅单录入', 2, '/weigh/ticket',    'weigh/ticket/index',    'ScaleToOriginal', 1),
(501, 500, '产品管理', 2, '/stock/product',   'stock/product/index',   'Goods', 1),
(502, 500, '库存查询', 2, '/stock/query',     'stock/query/index',     'Search', 2),
(503, 500, '库存流水', 2, '/stock/record',    'stock/record/index',    'List', 3),
(504, 500, '库存盘点', 2, '/stock/check',     'stock/check/index',     'EditPen', 4),
(601, 600, '结算管理', 2, '/finance/settle',  'finance/settle/index',  'Money', 1),
(602, 600, '回款管理', 2, '/finance/payment', 'finance/payment/index', 'Coin', 2),
(701, 700, '销售趋势', 2, '/report/trend',    'report/trend/index',    'TrendCharts', 1),
(702, 700, '煤种排行', 2, '/report/rank',     'report/rank/index',     'Histogram', 2),
(703, 700, '客户排行', 2, '/report/customer', 'report/customer/index', 'DataAnalysis', 3),
(801, 800, '用户管理', 2, '/system/user',     'system/user/index',     'UserFilled', 1),
(802, 800, '角色管理', 2, '/system/role',     'system/role/index',     'Avatar', 2),
(803, 800, '菜单管理', 2, '/system/menu',     'system/menu/index',     'Menu', 3),
(804, 800, '数据字典', 2, '/system/dict',     'system/dict/index',     'Notebook', 4),
(805, 800, '操作日志', 2, '/system/log',      'system/log/index',      'Document', 5);

-- =============================================================
-- 初始化数据：角色菜单映射（权限矩阵落地）
-- =============================================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 管理员（ADMIN）：全部
(1,100),(1,200),(1,201),(1,202),(1,300),(1,301),(1,400),(1,401),
(1,500),(1,501),(1,502),(1,503),(1,504),(1,600),(1,601),(1,602),
(1,700),(1,701),(1,702),(1,703),(1,800),(1,801),(1,802),(1,803),(1,804),(1,805),
-- 销售经理（MANAGER）：工作台 + 客户合同 + 订单 + 报表（用于审批）
(2,100),(2,200),(2,201),(2,202),(2,300),(2,301),(2,700),(2,701),(2,702),(2,703),
-- 销售员（SALES）：工作台 + 客户合同 + 订单 + 磅单查询 + 结算 + 报表
(3,100),(3,200),(3,201),(3,202),(3,300),(3,301),(3,400),(3,401),
(3,600),(3,601),(3,700),(3,701),(3,702),(3,703),
-- 磅房人员（WEIGHMAN）：工作台 + 称重发运
(4,100),(4,400),(4,401),
-- 库管员（STOCK）：工作台 + 产品与库存
(5,100),(5,500),(5,501),(5,502),(5,503),(5,504),
-- 财务人员（FINANCE）：工作台 + 结算与财务 + 报表
(6,100),(6,600),(6,601),(6,602),(6,700),(6,701),(6,702),(6,703);

-- =============================================================
-- 初始化数据：用户（6 个，密码统一为 123456 的 BCrypt 密文）
-- =============================================================
INSERT INTO sys_user (id, username, password, real_name, phone, role_id, status) VALUES
(1, 'admin',    '$2a$10$hijwrWNWpdOhnzUJ2VGLt.TjoOwiiSIy/PgbvaK6RlFV0mubKcqTa', '系统管理员', '13800000001', 1, 1),
(2, 'manager',  '$2a$10$hijwrWNWpdOhnzUJ2VGLt.TjoOwiiSIy/PgbvaK6RlFV0mubKcqTa', '销售经理',   '13800000002', 2, 1),
(3, 'sales',    '$2a$10$hijwrWNWpdOhnzUJ2VGLt.TjoOwiiSIy/PgbvaK6RlFV0mubKcqTa', '销售员',     '13800000003', 3, 1),
(4, 'weighman', '$2a$10$hijwrWNWpdOhnzUJ2VGLt.TjoOwiiSIy/PgbvaK6RlFV0mubKcqTa', '磅房人员',   '13800000004', 4, 1),
(5, 'stock',    '$2a$10$hijwrWNWpdOhnzUJ2VGLt.TjoOwiiSIy/PgbvaK6RlFV0mubKcqTa', '库管员',     '13800000005', 5, 1),
(6, 'finance',  '$2a$10$hijwrWNWpdOhnzUJ2VGLt.TjoOwiiSIy/PgbvaK6RlFV0mubKcqTa', '财务人员',   '13800000006', 6, 1);
