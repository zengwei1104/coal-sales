-- =============================================================
-- 煤炭销售管理系统 · 数据库初始化总入口
-- 版本：v2.0
-- 引擎：InnoDB  字符集：utf8mb4
-- 说明：本脚本负责建库，并按依赖顺序执行 6 个模块脚本
--
-- 执行方式（二选一）：
--   1) 命令行：在 sql 目录下执行  mysql -u root -p < init.sql
--   2) Navicat：若 SOURCE 命令不可用，请按 01→06 顺序手动执行各文件
--
-- 执行顺序（不可颠倒，存在引用依赖）：
--   01 权限 → 02 基础数据 → 03 销售 → 04 库存 → 05 财务 → 06 审计
-- =============================================================

CREATE DATABASE IF NOT EXISTS coal_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE coal_sales;

SOURCE 01-permission.sql;
SOURCE 02-base-data.sql;
SOURCE 03-sales.sql;
SOURCE 04-stock.sql;
SOURCE 05-finance.sql;
SOURCE 06-audit.sql;
