<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCustomerPage,
  addCustomer,
  updateCustomer,
  deleteCustomer
} from '../../../api/customer'

// ===== 表格数据 =====
const list = ref([])          // 当前页数据
const total = ref(0)          // 总条数
const loading = ref(false)

// 查询条件 + 分页
const query = reactive({
  current: 1,
  size: 10
})

// ===== 弹窗表单（和登录页的 form 一模一样）=====
const dialogVisible = ref(false)
const dialogTitle = ref('新增客户')
const formRef = ref()
const submitting = ref(false)

const form = ref({
  id: null,
  customerName: '',
  contactName: '',
  phone: '',
  address: '',
  invoiceInfo: '',
  creditLimit: 0,
  region: '',
  remark: ''
})

// 校验规则（对齐后端 DTO 的 @NotBlank / @Pattern）
const rules = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

// ===== 加载列表 =====
const loadData = async () => {
  loading.value = true
  try {
    const res = await getCustomerPage(query)
    list.value = res.data.records   // 注意：字段是 records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// ===== 打开新增 =====
const openAdd = () => {
  dialogTitle.value = '新增客户'
  Object.assign(form.value, {
    id: null, customerName: '', contactName: '', phone: '',
    address: '', invoiceInfo: '', creditLimit: 0, region: '', remark: ''
  })
  dialogVisible.value = true
}

// ===== 打开编辑 =====
const openEdit = row => {
  dialogTitle.value = '编辑客户'
  Object.assign(form.value, row)   // 直接把表格行数据塞进表单回显
  dialogVisible.value = true
}

// ===== 提交 =====
const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      if (form.value.id) {
        await updateCustomer(form.value.id, form.value)
        ElMessage.success('修改成功')
      } else {
        await addCustomer(form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

// ===== 删除 =====
const handleDelete = row => {
  ElMessageBox.confirm(`确定删除客户「${row.customerName}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteCustomer(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <!-- 顶部：搜索 + 新增 -->
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openAdd">新增客户</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="customerName" label="客户名称" min-width="160" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="收货地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="creditLimit" label="信用额度(元)" width="130" />
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗（表单部分和登录页一样） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="开票信息">
          <el-input v-model="form.invoiceInfo" type="textarea" />
        </el-form-item>
        <el-form-item label="信用额度">
          <el-input-number v-model="form.creditLimit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="form.region" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
.toolbar {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
