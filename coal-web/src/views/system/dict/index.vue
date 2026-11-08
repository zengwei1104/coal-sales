<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import {
  getDictPage, getDictByType, addDict, updateDict, deleteDict
} from '../../../api/dict'
import { formatDateTime } from '../../../utils/format'

/* ==================== 列表 ==================== */
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, dictType: '' })

// 已出现过的类型：用于筛选下拉选项，筛选后也不会丢
const knownTypes = ref(['biz_param'])
const rememberTypes = rows => {
  const set = new Set(knownTypes.value)
  rows.forEach(row => row.dictType && set.add(row.dictType))
  knownTypes.value = [...set]
}

/* ==================== 加载 ==================== */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getDictPage({
      current: query.current,
      size: query.size,
      dictType: query.dictType || undefined
    })
    list.value = res.data.records
    total.value = res.data.total
    rememberTypes(list.value)
  } finally {
    loading.value = false
  }
}

/* ==================== 弹窗表单 ==================== */
const dialogVisible = ref(false)
const formRef = ref()
const submitting = ref(false)

const emptyForm = {
  id: null,
  dictType: '',
  dictKey: '',
  dictValue: '',
  remark: '',
  sortOrder: 0
}
const form = ref({ ...emptyForm })

const rules = {
  dictType: [{ required: true, message: '请选择或输入字典类型', trigger: 'change' }],
  dictKey: [{ required: true, message: '请输入字典键', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const openAdd = () => {
  form.value = { ...emptyForm, dictType: query.dictType || 'biz_param' }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

const openEdit = row => {
  form.value = {
    id: row.id,
    dictType: row.dictType,
    dictKey: row.dictKey,
    dictValue: row.dictValue,
    remark: row.remark || '',
    sortOrder: row.sortOrder ?? 0
  }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

/**
 * 提交前做唯一性校验：表上 uk_dict(dict_type, dict_key) 是唯一索引，
 * 撞了会走全局兜底异常，前端只能看到"系统繁忙"，所以这里先拦一道。
 */
const isDuplicate = async () => {
  const res = await getDictByType(form.value.dictType.trim())
  const rows = res.data || []
  return rows.some(
    item => item.dictKey === form.value.dictKey.trim() && item.id !== form.value.id
  )
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      if (await isDuplicate()) {
        ElMessage.error(`「${form.value.dictType}」下已存在键 ${form.value.dictKey.trim()}`)
        return
      }
      const payload = {
        dictType: form.value.dictType.trim(),
        dictKey: form.value.dictKey.trim(),
        dictValue: form.value.dictValue.trim(),
        remark: form.value.remark.trim() || null,
        sortOrder: form.value.sortOrder ?? 0
      }
      if (form.value.id) {
        await updateDict(form.value.id, payload)
        ElMessage.success('修改成功，配置已即时生效')
      } else {
        await addDict(payload)
        ElMessage.success('新增成功，配置已即时生效')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

/* ==================== 删除 ==================== */
const handleDelete = row => {
  ElMessageBox.confirm(
    `确定删除「${row.dictType} / ${row.dictKey}」吗？删除后读取该参数的业务会走默认值。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
  ).then(async () => {
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.current > 1) query.current -= 1
    loadData()
  }).catch(() => {})
}

/* ==================== 分页 / 筛选 ==================== */
const handleTypeChange = () => {
  query.current = 1
  loadData()
}

const handleSizeChange = size => {
  query.size = size
  query.current = 1
  loadData()
}

const handleCurrentChange = page => {
  query.current = page
  loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <el-card shadow="never" class="card">
      <div class="toolbar">
        <div class="toolbar-title">
          <div class="title">数据字典</div>
          <div class="subtitle">
            业务参数（超发阈值、净重区间、锁定策略等）统一维护在此，保存后内存缓存立即刷新，无需重启
          </div>
        </div>
        <div class="toolbar-right">
          <el-select
            v-model="query.dictType"
            placeholder="全部类型"
            clearable
            filterable
            allow-create
            default-first-option
            class="type-filter"
            @change="handleTypeChange"
          >
            <el-option
              v-for="type in knownTypes"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
          <el-button :icon="RefreshRight" @click="loadData">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openAdd">新增字典项</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe row-key="id">
        <el-table-column label="字典类型" width="140">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.dictType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="字典键" width="230">
          <template #default="{ row }">
            <span class="code">{{ row.dictKey }}</span>
          </template>
        </el-table-column>
        <el-table-column label="字典值" width="150">
          <template #default="{ row }">
            <span class="value">{{ row.dictValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="remark">{{ row.remark || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">
            <span class="muted">{{ formatDateTime(row.updateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty">
            {{ query.dictType ? `类型「${query.dictType}」下暂无字典项` : '暂无字典数据' }}
          </div>
        </template>
      </el-table>

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
      :title="form.id ? '编辑字典项' : '新增字典项'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="字典类型" prop="dictType">
          <el-select
            v-model="form.dictType"
            placeholder="选择或输入类型"
            filterable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option
              v-for="type in knownTypes"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="字典键" prop="dictKey">
          <el-input v-model="form.dictKey" placeholder="如 net_weight_min" maxlength="50" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="统一按字符串存储，如 20 / 0.03" maxlength="200" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="写清楚这个参数干什么用的，方便后续维护"
          />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 160px" />
          <span class="form-tip">数字越小越靠前</span>
        </el-form-item>
      </el-form>
      <div class="form-notice">
        同一类型下的「字典键」不能重复（数据库唯一索引 uk_dict 约束）。
      </div>
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
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}
.toolbar-title {
  min-width: 0;
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
.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}
.type-filter {
  width: 180px;
}
.code {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  color: var(--ink-900);
}
.value {
  font-weight: 700;
  color: var(--ink-900);
  font-variant-numeric: tabular-nums;
}
.remark {
  color: var(--ink-700);
}
.muted {
  color: var(--ink-600);
  font-variant-numeric: tabular-nums;
}
.form-tip {
  margin-left: var(--space-3);
  font-size: 12px;
  color: var(--ink-500);
}
.form-notice {
  margin: 0 var(--space-4) var(--space-2) 90px;
  font-size: 12px;
  color: var(--ink-500);
  line-height: 1.5;
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
