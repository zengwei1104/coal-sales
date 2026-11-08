<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, RefreshRight } from '@element-plus/icons-vue'
import { getUserPage, addUser, updateUser, deleteUser } from '../../../api/user'
import { getRoles } from '../../../api/role'
import { formatDateTime } from '../../../utils/format'

/* ==================== 列表 ==================== */
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '' })

/* ==================== 角色字典 ==================== */
const roles = ref([])
const roleMap = computed(() => {
  const map = {}
  roles.value.forEach(r => { map[r.id] = r })
  return map
})

const ROLE_TAG = {
  ADMIN: 'danger',
  MANAGER: 'warning',
  SALES: 'primary',
  WEIGHMAN: 'info',
  STOCK: 'success',
  FINANCE: 'warning'
}

const roleName = row => roleMap.value[row.roleId]?.roleName || '未分配'
const roleTagType = row => ROLE_TAG[roleMap.value[row.roleId]?.roleCode] || 'info'

/* ==================== 关键字搜索（后端模糊匹配，带 300ms 防抖） ==================== */
let searchTimer = null
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    query.current = 1
    loadData()
  }, 300)
}

// 兜底：若后端还没重启（旧代码不认 keyword），前端对返回结果再过滤一次，
// 保证搜索框不会"点了没反应"。后端生效后这层过滤结果与后端一致，无副作用。
const displayList = computed(() => {
  const kw = query.keyword.trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter(u =>
    (u.username || '').toLowerCase().includes(kw) ||
    (u.realName || '').toLowerCase().includes(kw) ||
    (u.phone || '').includes(kw)
  )
})

/* ==================== 弹窗表单 ==================== */
const dialogVisible = ref(false)
const formRef = ref()
const submitting = ref(false)

const emptyForm = {
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  roleId: null,
  status: 1
}
const form = ref({ ...emptyForm })

// 校验规则：与后端 UserCreateDTO / UpdateUserDTO 保持一致
const rules = {
  username: [
    { required: true, message: '请输入登录名', trigger: 'blur' },
    { min: 3, max: 20, message: '登录名长度需在 3~20 位之间', trigger: 'blur' }
  ],
  password: [
    {
      validator: (rule, value, callback) => {
        if (form.value.id) return callback()          // 编辑时不校验密码
        if (!value) return callback(new Error('请输入初始密码'))
        if (value.length < 6 || value.length > 32) {
          return callback(new Error('密码长度需在 6~32 位之间'))
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择所属角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

/* ==================== 数据加载 ==================== */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage({
      current: query.current,
      size: query.size,
      keyword: query.keyword.trim() || undefined
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  const res = await getRoles()
  roles.value = res.data || []
}

/* ==================== 新增 / 编辑 ==================== */
const openAdd = () => {
  form.value = { ...emptyForm }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

const openEdit = row => {
  form.value = {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    phone: row.phone || '',
    roleId: row.roleId,
    status: row.status
  }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      // 后端 @Pattern 对 null 放行，空字符串会校验失败，这里统一转 null
      const payload = {
        realName: form.value.realName.trim(),
        phone: form.value.phone.trim() || null,
        roleId: form.value.roleId,
        status: form.value.status
      }
      if (form.value.id) {
        await updateUser(form.value.id, payload)
        ElMessage.success('修改成功')
      } else {
        await addUser({
          ...payload,
          username: form.value.username.trim(),
          password: form.value.password
        })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

/* ==================== 启用 / 禁用 ==================== */
const handleStatusChange = async (row, val) => {
  if (val === 0 && row.username === 'admin') {
    row.status = 1
    ElMessage.warning('系统内置管理员不可禁用')
    return
  }
  try {
    // 后端 UpdateUserDTO 要求 realName / roleId / status 必填，整行带过去
    await updateUser(row.id, {
      realName: row.realName,
      phone: row.phone || null,
      roleId: row.roleId,
      status: val
    })
    ElMessage.success(val === 1 ? '已启用' : '已禁用')
  } catch {
    row.status = val === 1 ? 0 : 1   // 失败回滚开关状态
  }
}

/* ==================== 删除 ==================== */
const handleDelete = row => {
  ElMessageBox.confirm(
    `确定删除用户「${row.realName}（${row.username}）」吗？`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
  ).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    // 当前页只剩这一条时回退一页
    if (list.value.length === 1 && query.current > 1) query.current -= 1
    loadData()
  }).catch(() => {})
}

/* ==================== 分页 ==================== */
const handleSizeChange = size => {
  query.size = size
  query.current = 1
  loadData()
}
const handleCurrentChange = page => {
  query.current = page
  loadData()
}

onMounted(() => {
  loadRoles()
  loadData()
})
</script>

<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="query.keyword"
            :prefix-icon="Search"
            placeholder="搜索登录名 / 真实姓名 / 手机号"
            clearable
            class="search"
            @input="handleSearch"
            @clear="handleSearch"
          />
        </div>
        <div class="toolbar-right">
          <el-button :icon="RefreshRight" @click="loadData">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openAdd">新增用户</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="displayList" v-loading="loading" border stripe row-key="id">
        <el-table-column prop="username" label="登录名" width="150">
          <template #default="{ row }">
            <span class="account">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="130" />
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            <span class="numeric">{{ row.phone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属角色" width="130">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row)" effect="light" size="small">
              {{ roleName(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="val => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="170">
          <template #default="{ row }">
            <span class="muted">{{ formatDateTime(row.lastLoginTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            <span class="muted">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-tooltip
              :disabled="row.username !== 'admin'"
              content="系统内置管理员不可删除"
              placement="top"
            >
              <span class="op-wrap">
                <el-button
                  link
                  type="danger"
                  :disabled="row.username === 'admin'"
                  @click="handleDelete(row)"
                >删除</el-button>
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty">
            {{ query.keyword ? '没有匹配的用户' : '暂无用户数据' }}
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑用户' : '新增用户'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="登录名" prop="username">
          <el-input
            v-model="form.username"
            :disabled="!!form.id"
            placeholder="3~20 位字母或数字"
          />
          <div v-if="form.id" class="form-tip">登录名创建后不可修改</div>
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6~32 位" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="选填，11 位手机号" />
        </el-form-item>
        <el-form-item label="所属角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="r in roles"
              :key="r.id"
              :label="r.roleName"
              :value="r.id"
            >
              <span class="option-name">{{ r.roleName }}</span>
              <span class="option-code">{{ r.roleCode }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
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
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}
.toolbar-left {
  flex: 1;
  min-width: 0;
}
.toolbar-right {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}
.search {
  max-width: 320px;
}
.account {
  font-weight: 600;
  color: var(--ink-900);
}
.numeric {
  font-variant-numeric: tabular-nums;
}
.muted {
  color: var(--ink-600);
  font-variant-numeric: tabular-nums;
}
.op-wrap {
  display: inline-block;
}
.form-tip {
  font-size: 12px;
  color: var(--ink-500);
  line-height: 1.4;
}
.option-name {
  margin-right: var(--space-3);
}
.option-code {
  font-size: 12px;
  color: var(--ink-500);
  font-variant-numeric: tabular-nums;
}
.pagination {
  margin-top: var(--space-4);
  display: flex;
  justify-content: flex-end;
}
.empty {
  padding: var(--space-6) 0;
  color: var(--ink-500);
}
</style>
