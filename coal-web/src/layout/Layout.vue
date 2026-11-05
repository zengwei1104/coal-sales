<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  OfficeBuilding,
  Document,
  ScaleToOriginal,
  Box,
  Money,
  DataLine,
  Setting
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ============ 菜单配置（前端写死，path 必须和后端 sys_menu.path 完全一致） ============
// 按后端 sys_menu 表初始化数据整理，含目录分组
const menuTree = [
  {
    title: '工作台',
    icon: HomeFilled,
    children: [{ title: '工作台', path: '/dashboard' }]
  },
  {
    title: '客户与合同',
    icon: OfficeBuilding,
    children: [
      { title: '客户管理', path: '/biz/customer' },
      { title: '合同管理', path: '/biz/contract' }
    ]
  },
  {
    title: '销售订单',
    icon: Document,
    children: [{ title: '订单管理', path: '/order/list' }]
  },
  {
    title: '称重发运',
    icon: ScaleToOriginal,
    children: [{ title: '磅单录入', path: '/weigh/ticket' }]
  },
  {
    title: '产品与库存',
    icon: Box,
    children: [
      { title: '产品管理', path: '/stock/product' },
      { title: '库存查询', path: '/stock/query' },
      { title: '库存流水', path: '/stock/record' },
      { title: '库存盘点', path: '/stock/check' }
    ]
  },
  {
    title: '结算与财务',
    icon: Money,
    children: [
      { title: '结算管理', path: '/finance/settle' },
      { title: '回款管理', path: '/finance/payment' }
    ]
  },
  {
    title: '统计报表',
    icon: DataLine,
    children: [
      { title: '销售趋势', path: '/report/trend' },
      { title: '煤种排行', path: '/report/rank' },
      { title: '客户排行', path: '/report/customer' }
    ]
  },
  {
    title: '系统管理',
    icon: Setting,
    children: [
      { title: '用户管理', path: '/system/user' },
      { title: '角色管理', path: '/system/role' },
      { title: '菜单管理', path: '/system/menu' },
      { title: '数据字典', path: '/system/dict' },
      { title: '操作日志', path: '/system/log' }
    ]
  }
]

// 按后端返回的 menus 列表过滤：只显示有权限的菜单
const visibleMenuTree = computed(() => {
  const allowed = userStore.menus || []
  return menuTree
    .map(group => ({
      ...group,
      children: group.children.filter(item => allowed.includes(item.path))
    }))
    .filter(group => group.children.length > 0)
})

// 用户名（优先真实姓名，其次登录名）
const username = computed(() => {
  const info = userStore.userInfo || {}
  return info.realName || info.username || '用户'
})

// 当前选中的菜单（高亮用）
const activeMenu = computed(() => route.path)

// 顶栏标题：从菜单树中匹配当前路径
const activeTitle = computed(() => {
  for (const group of menuTree) {
    const hit = group.children.find(i => i.path === route.path)
    if (hit) return hit.title
  }
  return '煤炭销售管理系统'
})

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    .then(() => {
      userStore.logout()
      router.push('/login')
    })
    .catch(() => {})
}
</script>

<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="232px" class="aside">
      <div class="logo">
        <span class="logo-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M6 17l3-4 2.5 2.5L15 8l3 6" stroke="currentColor"
              stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round" />
          </svg>
        </span>
        <span class="logo-text">煤炭销售</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="transparent"
        text-color="#9aa2ad"
        active-text-color="#f5b942"
        class="menu"
      >
        <template v-for="group in visibleMenuTree" :key="group.title">
          <el-sub-menu :index="group.title">
            <template #title>
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.title }}</span>
            </template>
            <el-menu-item
              v-for="item in group.children"
              :key="item.path"
              :index="item.path"
            >
              {{ item.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-title">{{ activeTitle }}</div>
        <div class="header-right">
          <span class="username">{{ username }}</span>
          <el-button link type="primary" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background-color: var(--ink-950);
  overflow-x: hidden;
  border-right: 1px solid oklch(28% 0.008 60);
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0 var(--space-5);
  border-bottom: 1px solid oklch(28% 0.008 60);
  color: var(--ink-50);
  font-size: 16px;
  font-weight: 700;
  white-space: nowrap;
}
.logo-mark {
  color: var(--color-amber);
  display: flex;
  align-items: center;
}
.logo-text {
  letter-spacing: 0.04em;
}
.menu {
  border-right: none;
  padding: var(--space-2) var(--space-3);
}
/* 菜单项：工业风，选中态用琥珀金左侧竖条 */
.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  border-radius: var(--radius-sm);
  margin-bottom: 2px;
}
.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  background-color: oklch(28% 0.008 60);
}
.menu :deep(.el-menu-item.is-active) {
  background-color: oklch(72% 0.16 75 / 0.12);
  color: var(--color-amber);
  font-weight: 600;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--ink-200);
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-900);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  color: var(--ink-700);
}
.main {
  background-color: var(--color-bg);
  padding: 16px;
}
</style>
