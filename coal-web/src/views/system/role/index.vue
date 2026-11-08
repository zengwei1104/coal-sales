<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Key, RefreshRight } from '@element-plus/icons-vue'
import { getRoles, getRoleMenuIds, assignRoleMenus } from '../../../api/role'
import { getMenuTree } from '../../../api/menu'
import { useUserStore } from '../../../store/user'

const userStore = useUserStore()

/* ==================== 角色列表 ==================== */
const roles = ref([])
const loading = ref(false)
const menuCount = ref({})   // roleId -> 已授权菜单条数

const ROLE_TAG = {
  ADMIN: 'danger',
  MANAGER: 'warning',
  SALES: 'primary',
  WEIGHMAN: 'info',
  STOCK: 'success',
  FINANCE: 'warning'
}
const tagType = role => ROLE_TAG[role.roleCode] || 'info'

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await getRoles()
    roles.value = res.data || []

    // 并发统计各角色已授权的菜单条数（单个失败不影响整体）
    const results = await Promise.allSettled(
      roles.value.map(r => getRoleMenuIds(r.id))
    )
    const map = {}
    results.forEach((item, index) => {
      const roleId = roles.value[index].id
      map[roleId] = item.status === 'fulfilled' ? (item.value.data || []).length : null
    })
    menuCount.value = map
  } finally {
    loading.value = false
  }
}

/* ==================== 权限抽屉 ==================== */
const drawerVisible = ref(false)
const currentRole = ref(null)
const treeData = ref([])
const treeRef = ref()
const treeLoading = ref(false)
const saving = ref(false)
const checkedCount = ref(0)

// 后端叶子节点的 children 是 null，统一成 [] 交给 el-tree，避免判空异常
const normalizeMenus = nodes =>
  (nodes || []).map(node => ({
    ...node,
    children: Array.isArray(node.children) ? normalizeMenus(node.children) : []
  }))

// 递归收集叶子菜单 id：回显时只勾叶子，父目录由 el-tree 自动推导，避免"勾父带子"
const collectLeafIds = (nodes, acc = []) => {
  nodes.forEach(node => {
    if (node.children && node.children.length) {
      collectLeafIds(node.children, acc)
    } else {
      acc.push(node.id)
    }
  })
  return acc
}

const updateCount = () => {
  checkedCount.value = treeRef.value ? treeRef.value.getCheckedKeys().length : 0
}

const openAssign = async row => {
  currentRole.value = row
  treeData.value = []
  drawerVisible.value = true
  treeLoading.value = true
  try {
    const [treeRes, idsRes] = await Promise.all([
      getMenuTree(),
      getRoleMenuIds(row.id)
    ])
    treeData.value = normalizeMenus(treeRes.data)
    const assigned = idsRes.data || []

    await nextTick()
    const leafSet = new Set(collectLeafIds(treeData.value))
    treeRef.value?.setCheckedKeys(assigned.filter(id => leafSet.has(id)))
    updateCount()
  } finally {
    treeLoading.value = false
  }
}

const checkAll = () => {
  treeRef.value?.setCheckedKeys(collectLeafIds(treeData.value))
  updateCount()
}

const clearAll = () => {
  treeRef.value?.setCheckedKeys([])
  updateCount()
}

const handleSave = async () => {
  if (!treeRef.value) return
  // 叶子 + 半选父目录一起提交，后端按全量重设保存
  const menuIds = [
    ...treeRef.value.getCheckedKeys(),
    ...treeRef.value.getHalfCheckedKeys()
  ]
  saving.value = true
  try {
    await assignRoleMenus(currentRole.value.id, menuIds)
    ElMessage.success('权限分配成功')
    menuCount.value[currentRole.value.id] = menuIds.length
    drawerVisible.value = false

    // 改的是自己所属角色时，登录态里的菜单还是旧的，需要重新登录
    if (currentRole.value.roleCode === userStore.userInfo?.roleCode) {
      ElMessage.warning('当前账号属于该角色，退出重新登录后菜单变更生效')
    }
  } finally {
    saving.value = false
  }
}

onMounted(loadRoles)
</script>

<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <div class="toolbar">
        <div class="toolbar-title">
          <div class="title">角色与权限</div>
          <div class="subtitle">
            系统内置 6 个角色，角色本身不可增删，可调整每个角色能访问的菜单
          </div>
        </div>
        <el-button :icon="RefreshRight" @click="loadRoles">刷新</el-button>
      </div>

      <div v-loading="loading" class="role-grid">
        <div v-for="role in roles" :key="role.id" class="role-card">
          <div class="role-head">
            <span class="role-badge">{{ role.roleName?.slice(0, 1) }}</span>
            <div class="role-name-wrap">
              <div class="role-name">{{ role.roleName }}</div>
              <el-tag :type="tagType(role)" effect="light" size="small">
                {{ role.roleCode }}
              </el-tag>
            </div>
          </div>

          <div class="role-remark">{{ role.remark || '暂无说明' }}</div>

          <div class="role-foot">
            <span class="menu-count">
              已授权
              <b>{{ menuCount[role.id] ?? '—' }}</b>
              项菜单
            </span>
            <el-button link type="primary" :icon="Key" @click="openAssign(role)">
              分配权限
            </el-button>
          </div>
        </div>

        <el-empty v-if="!loading && roles.length === 0" description="暂无角色数据" />
      </div>
    </el-card>

    <!-- 分配权限抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="`分配权限 · ${currentRole?.roleName || ''}`"
      size="420px"
      destroy-on-close
    >
      <div v-loading="treeLoading" class="drawer-body">
        <div class="drawer-ops">
          <el-button size="small" @click="checkAll">全选</el-button>
          <el-button size="small" @click="clearAll">清空</el-button>
          <span class="drawer-count">已选 {{ checkedCount }} 项</span>
        </div>

        <el-tree
          v-if="treeData.length"
          ref="treeRef"
          :data="treeData"
          :props="{ label: 'menuName', children: 'children' }"
          node-key="id"
          show-checkbox
          default-expand-all
          @check="updateCount"
        >
          <template #default="{ data }">
            <span class="tree-node">
              <span class="tree-label">{{ data.menuName }}</span>
              <span class="tree-path">{{ data.path }}</span>
            </span>
          </template>
        </el-tree>

        <el-empty v-else-if="!treeLoading" description="暂无菜单数据" :image-size="80" />
      </div>

      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.page {
  padding: 16px;
}
.card {
  border-radius: var(--radius-md);
}
.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}
.title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink-900);
}
.subtitle {
  margin-top: 2px;
  font-size: 13px;
  color: var(--ink-600);
}

/* ---------- 角色卡片网格 ---------- */
.role-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
  min-height: 120px;
}
.role-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
  background-color: var(--color-surface);
  border: 1px solid var(--ink-200);
  border-radius: var(--radius-md);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}
.role-card:hover {
  border-color: var(--ink-300);
  box-shadow: var(--shadow-md);
}
.role-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.role-badge {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background-color: var(--color-amber-soft);
  color: oklch(45% 0.12 70);
  font-size: 16px;
  font-weight: 700;
}
.role-name-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  min-width: 0;
}
.role-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-900);
}
.role-remark {
  font-size: 13px;
  color: var(--ink-600);
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.role-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-3);
  border-top: 1px solid var(--ink-150);
}
.menu-count {
  font-size: 12px;
  color: var(--ink-600);
}
.menu-count b {
  color: var(--ink-900);
  font-variant-numeric: tabular-nums;
}

/* ---------- 抽屉 ---------- */
.drawer-body {
  height: 100%;
}
.drawer-ops {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding-bottom: var(--space-3);
  margin-bottom: var(--space-2);
  border-bottom: 1px solid var(--ink-150);
}
.drawer-count {
  margin-left: auto;
  font-size: 12px;
  color: var(--ink-600);
  font-variant-numeric: tabular-nums;
}
.tree-node {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}
.tree-label {
  color: var(--ink-900);
}
.tree-path {
  font-size: 11px;
  color: var(--ink-500);
  font-variant-numeric: tabular-nums;
}
</style>
