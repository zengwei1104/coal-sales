<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaymentPage, createPayment, writeOffPayment } from '../../../api/payment'
import { getCustomerPage } from '../../../api/customer'
import { getWriteoffableSettlements } from '../../../api/settlement'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10, customerId: null })

const payTypeMap = {
  1: { text: '银行转账', type: 'primary' },
  2: { text: '现金', type: 'success' },
  3: { text: '承兑汇票', type: 'warning' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPaymentPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// ===== 登记回款 =====
const customerOptions = ref([])
const createVisible = ref(false)
const createForm = ref({
  customerId: null,
  amount: null,
  payType: 1,
  payTime: '',
  remark: ''
})

const loadCustomers = async () => {
  const res = await getCustomerPage({ current: 1, size: 100 })
  customerOptions.value = res.data.records
}

const openCreate = () => {
  createForm.value = {
    customerId: null,
    amount: null,
    payType: 1,
    payTime: '',
    remark: ''
  }
  createVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.value.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
  if (!createForm.value.amount || createForm.value.amount <= 0) {
    ElMessage.warning('请输入正确的回款金额')
    return
  }
  await createPayment({
    customerId: createForm.value.customerId,
    amount: createForm.value.amount,
    payType: createForm.value.payType,
    payTime: createForm.value.payTime || undefined,
    remark: createForm.value.remark
  })
  ElMessage.success('登记回款成功')
  createVisible.value = false
  loadData()
}

// ===== 核销 =====
const writeOffVisible = ref(false)
const writeOffRow = ref(null)
const writeOffItems = ref([])        // 该客户可核销结算单列表
const writeOffInputs = ref({})       // settleId -> 本次核销金额
const writeOffLoading = ref(false)

const openWriteOff = async row => {
  writeOffRow.value = row
  writeOffItems.value = []
  writeOffInputs.value = {}
  writeOffVisible.value = true
  writeOffLoading.value = true
  try {
    const res = await getWriteoffableSettlements(row.customerId)
    writeOffItems.value = res.data || []
  } finally {
    writeOffLoading.value = false
  }
}

const handleWriteOff = async () => {
  const items = writeOffItems.value
    .map(s => ({ settleId: s.id, amount: Number(writeOffInputs.value[s.id] || 0) }))
    .filter(i => i.amount > 0)

  if (!items.length) {
    ElMessage.warning('请至少填写一笔核销金额')
    return
  }

  await writeOffPayment(writeOffRow.value.id, { items })
  ElMessage.success('核销成功')
  writeOffVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
  loadCustomers()
})
</script>

<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-select v-model="query.customerId" placeholder="客户" clearable filterable style="width: 200px" @change="loadData">
          <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openCreate">登记回款</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="paymentNo" label="回款单号" width="180" />
        <el-table-column prop="customerId" label="客户ID" width="90" />
        <el-table-column prop="amount" label="回款金额(元)" width="140" />
        <el-table-column label="付款方式" width="110">
          <template #default="{ row }">
            <el-tag :type="payTypeMap[row.payType]?.type || 'info'">
              {{ payTypeMap[row.payType]?.text || row.payType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="付款时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="登记时间" width="180" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openWriteOff(row)">核销</el-button>
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

    <!-- 登记回款 -->
    <el-dialog v-model="createVisible" title="登记回款" width="480px">
      <el-form label-width="100px">
        <el-form-item label="客户" required>
          <el-select v-model="createForm.customerId" placeholder="选择客户" filterable style="width: 100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="回款金额" required>
          <el-input-number v-model="createForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="createForm.payType" style="width: 100%">
            <el-option label="银行转账" :value="1" />
            <el-option label="现金" :value="2" />
            <el-option label="承兑汇票" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款时间">
          <el-date-picker v-model="createForm.payTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" placeholder="银行回单号等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 核销 -->
    <el-dialog v-model="writeOffVisible" :title="`核销回款 ${writeOffRow?.paymentNo || ''}`" width="720px">
      <el-table :data="writeOffItems" v-loading="writeOffLoading" border stripe max-height="400">
        <el-table-column prop="settleNo" label="结算单号" width="180" />
        <el-table-column prop="totalAmount" label="结算总额(元)" width="130" />
        <el-table-column prop="receivedAmount" label="已核销(元)" width="120" />
        <el-table-column prop="remainingAmount" label="剩余未收(元)" width="130" />
        <el-table-column label="本次核销金额(元)" min-width="160">
          <template #default="{ row }">
            <el-input-number
              v-model="writeOffInputs[row.id]"
              :min="0"
              :max="Number(row.remainingAmount) || undefined"
              :precision="2"
              style="width: 100%"
            />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!writeOffLoading && !writeOffItems.length" description="该客户暂无待核销的结算单" />
      <template #footer>
        <el-button @click="writeOffVisible = false">取消</el-button>
        <el-button type="primary" @click="handleWriteOff">确认核销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 8px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
