<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getContractPage, addContract, updateContract, deleteContract,
  submitContract, auditContract
} from '../../../api/contract'
import { getCustomerPage } from '../../../api/customer'
import { getProductPage } from '../../../api/product'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10, status: null, keyword: '' })

// 下拉数据源
const customerOptions = ref([])
const productOptions = ref([])

// 弹窗表单
const dialogVisible = ref(false)
const dialogTitle = ref('新增合同')
const formRef = ref()
const submitting = ref(false)

const form = ref({
  id: null,
  customerId: null,
  productId: null,
  signDate: '',
  startDate: '',
  endDate: '',
  totalQty: null,
  price: null,
  taxRate: null,
  qnetAr: null,
  shortRate: null,
  payType: 3,
  creditDays: 0,
  transportBear: 1
})

const rules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  productId: [{ required: true, message: '请选择煤种商品', trigger: 'change' }],
  totalQty: [{ required: true, message: '请输入约定总量', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }]
}

// 状态映射
const statusMap = {
  0: { text: '草稿', type: 'info' },
  1: { text: '待审核', type: 'warning' },
  2: { text: '执行中', type: 'success' },
  3: { text: '已完成', type: 'primary' },
  4: { text: '已终止', type: 'danger' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getContractPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  // 客户和商品下拉，各拉前 100 条足够用
  const [c, p] = await Promise.all([
    getCustomerPage({ current: 1, size: 100 }),
    getProductPage({ current: 1, size: 100 })
  ])
  customerOptions.value = c.data.records
  productOptions.value = p.data.records
}

const openAdd = () => {
  dialogTitle.value = '新增合同'
  Object.assign(form.value, {
    id: null, customerId: null, productId: null, signDate: '',
    startDate: '', endDate: '', totalQty: null, price: null,
    taxRate: null, qnetAr: null, shortRate: null, payType: 3,
    creditDays: 0, transportBear: 1
  })
  dialogVisible.value = true
}

const openEdit = row => {
  dialogTitle.value = '编辑合同'
  Object.assign(form.value, {
    id: row.id, customerId: row.customerId, productId: row.productId,
    signDate: row.signDate, startDate: row.startDate, endDate: row.endDate,
    totalQty: row.totalQty, price: row.price, taxRate: row.taxRate,
    qnetAr: row.qnetAr, shortRate: row.shortRate, payType: row.payType,
    creditDays: row.creditDays, transportBear: row.transportBear
  })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      if (form.value.id) {
        await updateContract(form.value.id, form.value)
        ElMessage.success('修改成功')
      } else {
        await addContract(form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = row => {
  ElMessageBox.confirm(`确定删除合同「${row.contractNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteContract(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

const handleSubmitAudit = row => {
  ElMessageBox.confirm(`确定提交合同「${row.contractNo}」审核吗？`, '提示', { type: 'info' })
    .then(async () => {
      await submitContract(row.id)
      ElMessage.success('提交成功')
      loadData()
    })
    .catch(() => {})
}

// 审核弹窗
const auditVisible = ref(false)
const auditForm = ref({ id: null, pass: true, opinion: '' })

const openAudit = row => {
  auditForm.value = { id: row.id, pass: true, opinion: '' }
  auditVisible.value = true
}

const handleAudit = async () => {
  await auditContract(auditForm.value.id, {
    pass: auditForm.value.pass,
    opinion: auditForm.value.opinion
  })
  ElMessage.success('审核完成')
  auditVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
  loadOptions()
})
</script>

<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="loadData">
          <el-option label="草稿" :value="0" />
          <el-option label="待审核" :value="1" />
          <el-option label="执行中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已终止" :value="4" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="合同编号/客户" clearable style="width: 200px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openAdd">新增合同</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="contractNo" label="合同编号" width="170" />
        <el-table-column prop="customerId" label="客户ID" width="90" />
        <el-table-column prop="productId" label="煤种ID" width="90" />
        <el-table-column prop="totalQty" label="约定总量(吨)" width="120" />
        <el-table-column prop="price" label="单价(元/吨)" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" width="120" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="handleSubmitAudit(row)">提交审核</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="openAudit(row)">审核</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 新增/编辑合同 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="选择客户" style="width: 100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="煤种商品" prop="productId">
          <el-select v-model="form.productId" placeholder="选择煤种商品" style="width: 100%">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.productName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="签订日期">
          <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期起">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期止">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="约定总量(吨)" prop="totalQty">
          <el-input-number v-model="form.totalQty" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价(元/吨)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="税率(%)">
          <el-input-number v-model="form.taxRate" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="约定热值">
          <el-input-number v-model="form.qnetAr" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="溢短装(%)">
          <el-input-number v-model="form.shortRate" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="form.payType" style="width: 100%">
            <el-option label="预付" :value="1" />
            <el-option label="货到付款" :value="2" />
            <el-option label="账期" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="账期天数">
          <el-input-number v-model="form.creditDays" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="运费承担">
          <el-select v-model="form.transportBear" style="width: 100%">
            <el-option label="客户自提" :value="1" />
            <el-option label="供方送货" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" title="审核合同" width="480px">
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.pass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.opinion" type="textarea" placeholder="驳回时请填写意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 8px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
