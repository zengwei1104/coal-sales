-- =============================================================
SET NAMES utf8mb4;

-- 煤炭销售管理系统 · 工作台权限收窄（仅 ADMIN + MANAGER 可见）
-- 用途：已初始化数据库的场景下，同步删除非管理角色的工作台菜单权限
-- 说明：工作台菜单 id = 100，此处删除角色 3/4/5/6 与其关联
-- 执行方式：对已运行的项目数据库执行本脚本即可
-- =============================================================

-- 删除 销售员(3)/磅房(4)/库管员(5)/财务(6) 与工作台菜单(100) 的关联
DELETE FROM sys_role_menu WHERE menu_id = 100 AND role_id IN (3, 4, 5, 6);

-- 校验：确认角色 1、2 仍保留工作台权限，角色 3/4/5/6 已无工作台权限
-- SELECT rm.role_id, r.role_name, m.menu_name
-- FROM sys_role_menu rm
-- JOIN sys_role r ON rm.role_id = r.id
-- JOIN sys_menu m ON rm.menu_id = m.id
-- WHERE rm.menu_id = 100;
