<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderPage, addOrder, updateOrder, deleteOrder, cancelOrder
} from '../../../api/order'
import { getContractPage } from '../../../api/contract'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10, status: null, keyword: '' })

// 合同下拉（选合同下订单）
const contractOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增订单')
const formRef = ref()
const submitting = ref(false)

const form = ref({
  id: null,
  contractId: null,
  planQty: null,
  price: null,
  priceRemark: '',
  deliveryDate: '',
  plateNo: ''
})

const rules = {
  contractId: [{ required: true, message: '请选择合同', trigger: 'change' }],
  planQty: [{ required: true, message: '请输入计划数量', trigger: 'blur' }]
}

const statusMap = {
  1: { text: '待发运', type: 'warning' },
  2: { text: '部分发运', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOrderPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadContracts = async () => {
  const res = await getContractPage({ current: 1, size: 100, status: 2 })
  contractOptions.value = res.data.records
}

const openAdd = () => {
  dialogTitle.value = '新增订单'
  Object.assign(form.value, {
    id: null, contractId: null, planQty: null, price: null,
    priceRemark: '', deliveryDate: '', plateNo: ''
  })
  dialogVisible.value = true
}

const openEdit = row => {
  dialogTitle.value = '编辑订单'
  Object.assign(form.value, {
    id: row.id, contractId: row.contractId, planQty: row.planQty,
    price: row.price, priceRemark: row.priceRemark,
    deliveryDate: row.deliveryDate, plateNo: row.plateNo
  })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      if (form.value.id) {
        await updateOrder(form.value.id, form.value)
        ElMessage.success('修改成功')
      } else {
        await addOrder(form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = row => {
  ElMessageBox.confirm(`确定取消订单「${row.orderNo}」吗？取消后合同余量将回退。`, '提示', { type: 'warning' })
    .then(async () => {
      await cancelOrder(row.id)
      ElMessage.success('订单已取消')
      loadData()
    })
    .catch(() => {})
}

const handleDelete = row => {
  ElMessageBox.confirm(`确定删除订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteOrder(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
  loadContracts()
})
</script>

<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="loadData">
          <el-option label="待发运" :value="1" />
          <el-option label="部分发运" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="订单编号" clearable style="width: 200px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openAdd">新增订单</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单编号" width="170" />
        <el-table-column prop="contractId" label="合同ID" width="90" />
        <el-table-column prop="customerId" label="客户ID" width="90" />
        <el-table-column prop="productId" label="煤种ID" width="90" />
        <el-table-column prop="planQty" label="计划数量(吨)" width="120" />
        <el-table-column prop="shippedQty" label="已发运(吨)" width="110" />
        <el-table-column prop="settledQty" label="已结算(吨)" width="110" />
        <el-table-column prop="price" label="价格(元/吨)" width="120" />
        <el-table-column prop="deliveryDate" label="发货日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" link type="danger" @click="handleCancel(row)">取消</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="选择合同" prop="contractId">
          <el-select v-model="form.contractId" placeholder="选择执行中的合同" style="width: 100%":disabled="!!form.id">
            <el-option v-for="c in contractOptions" :key="c.id" :label="c.contractNo" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划数量(吨)" prop="planQty">
          <el-input-number v-model="form.planQty" :min="0" :precision="2" style="width: 100%"  />
        </el-form-item>
        <el-form-item label="价格(元/吨)">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" placeholder="不填默认取合同价" />
        </el-form-item>
        <el-form-item label="改价理由">
          <el-input v-model="form.priceRemark" placeholder="手动改价时填写理由" />
        </el-form-item>
        <el-form-item label="发货日期">
          <el-date-picker v-model="form.deliveryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="指定车牌">
          <el-input v-model="form.plateNo" />
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
.page { padding: 16px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 8px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
