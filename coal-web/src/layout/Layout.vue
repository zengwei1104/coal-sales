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
    <el-aside width="220px" class="aside">
      <div class="logo">煤炭销售管理系统</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#ffffff"
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
        <div class="header-title">煤炭销售管理系统</div>
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
  background-color: #001529;
  overflow-x: hidden;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #002140;
  white-space: nowrap;
}
.menu {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  color: #333;
}
.main {
  background-color: #f5f7fa;
  padding: 16px;
}
</style>
