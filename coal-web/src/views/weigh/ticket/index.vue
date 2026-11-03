<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWeighTicketPage, addWeighTicket, addTareWeight, addGrossWeight, cancelWeighTicket
} from '../../../api/weighTicket'
import { getAvailableOrders, getOrderPage } from '../../../api/order'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10, status: null, orderId: null, plateNo: '' })

// 订单下拉：筛选用（全量订单） + 开单用（可发运订单）
const orderOptions = ref([])
const openOrderOptions = ref([])

const statusMap = {
  0: { text: '待称皮重', type: 'info' },
  1: { text: '已称皮重', type: 'warning' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已作废', type: 'danger' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWeighTicketPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadOrders = async () => {
  // 订单筛选下拉用全量订单（前 100 条），避免 available 只含待发运/部分发运
  const res = await getOrderPage({ current: 1, size: 100 })
  orderOptions.value = res.data.records
  // 开单弹窗的订单下拉用可发运订单
  const avail = await getAvailableOrders()
  openOrderOptions.value = avail.data
}

// ===== 开单弹窗（录皮重）=====
const openVisible = ref(false)
const openForm = ref({
  orderId: null,
  plateNo: '',
  driver: ''
})

const openNew = () => {
  Object.assign(openForm.value, { orderId: null, plateNo: '', driver: ''})
  openVisible.value = true
}

const handleOpen = async () => {
  if (!openForm.value.orderId) {
    ElMessage.warning('请选择订单')
    return
  }
  if (!openForm.value.plateNo) {
    ElMessage.warning('请输入车牌号')
    return
  }
  await addWeighTicket({
    orderId: openForm.value.orderId,
    plateNo: openForm.value.plateNo,
    driver: openForm.value.driver
  })
  ElMessage.success('开单成功')
  openVisible.value = false
  loadData()
}

// ===== 录皮重弹窗（针对待称皮重的磅单）=====
const tareVisible = ref(false)
const tareForm = ref({ id: null, tareWeight: null, tareSource: 1 })

const openTare = row => {
  tareForm.value = { id: row.id, tareWeight: null, tareSource: 1 }
  tareVisible.value = true
}

const handleTare = async () => {
  if (tareForm.value.tareWeight == null) {
    ElMessage.warning('请输入皮重')
    return
  }
  await addTareWeight(tareForm.value.id, {
    tareWeight: tareForm.value.tareWeight,
    tareSource: tareForm.value.tareSource
  })
  ElMessage.success('皮重录入成功')
  tareVisible.value = false
  loadData()
}

// ===== 录毛重弹窗（针对已称皮重的磅单）=====
const grossVisible = ref(false)
const grossForm = ref({ id: null, grossWeight: null })

const openGross = row => {
  grossForm.value = { id: row.id, grossWeight: null }
  grossVisible.value = true
}

const handleGross = async () => {
  if (grossForm.value.grossWeight == null) {
    ElMessage.warning('请输入毛重')
    return
  }
  await addGrossWeight(grossForm.value.id, { grossWeight: grossForm.value.grossWeight })
  ElMessage.success('毛重录入成功，磅单已完成')
  grossVisible.value = false
  loadData()
}

// ===== 作废 =====
const handleCancel = row => {
  ElMessageBox.prompt('请输入作废原因', '作废磅单', {
    type: 'warning',
    inputValidator: v => (v && v.trim() ? true : '作废原因不能为空')
  })
    .then(async ({ value }) => {
      await cancelWeighTicket(row.id, { cancelReason: value })
      ElMessage.success('磅单已作废')
      loadData()
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
  loadOrders()
})
</script>

<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="loadData">
          <el-option label="待称皮重" :value="0" />
          <el-option label="已称皮重" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已作废" :value="3" />
        </el-select>
        <el-select v-model="query.orderId" placeholder="订单" clearable filterable style="width: 180px" @change="loadData">
          <el-option v-for="o in orderOptions" :key="o.id" :label="o.orderNo" :value="o.id" />
        </el-select>
        <el-input v-model="query.plateNo" placeholder="车牌号" clearable style="width: 160px" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" size="large" @click="openNew">开单（录皮重）</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="weightNo" label="磅单号" width="170" />
        <el-table-column prop="orderId" label="订单ID" width="90" />
        <el-table-column prop="plateNo" label="车牌号" width="120" />
        <el-table-column prop="driver" label="司机" width="100" />
        <el-table-column prop="tareWeight" label="皮重(吨)" width="100" />
        <el-table-column prop="grossWeight" label="毛重(吨)" width="100" />
        <el-table-column prop="netWeight" label="净重(吨)" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grossTime" label="重车过磅时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="openTare(row)">录皮重</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="openGross(row)">录毛重</el-button>
            <el-button v-if="row.status !== 3" link type="danger" @click="handleCancel(row)">作废</el-button>
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

    <!-- 开单弹窗 -->
    <el-dialog v-model="openVisible" title="开单" width="520px">
      <el-form label-width="100px">
        <el-form-item label="选择订单" required>
          <el-select v-model="openForm.orderId" placeholder="选择可发运订单" style="width: 100%">
            <el-option v-for="o in openOrderOptions" :key="o.id" :label="o.orderNo" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车牌号" required>
          <el-input v-model="openForm.plateNo" placeholder="如 赣A88888" />
        </el-form-item>
        <el-form-item label="司机">
          <el-input v-model="openForm.driver" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOpen">确定开单</el-button>
      </template>
    </el-dialog>

    <!-- 录皮重弹窗 -->
    <el-dialog v-model="tareVisible" title="录皮重" width="420px">
      <el-form label-width="100px">
        <el-form-item label="皮重(吨)" required>
          <el-input-number v-model="tareForm.tareWeight" :min="0" :precision="3" style="width: 100%" />
        </el-form-item>
        <el-form-item label="皮重来源">
          <el-radio-group v-model="tareForm.tareSource">
            <el-radio :value="1">现场称重</el-radio>
            <el-radio :value="2">备案套用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tareVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTare">确定</el-button>
      </template>
    </el-dialog>

    <!-- 录毛重弹窗 -->
    <el-dialog v-model="grossVisible" title="录毛重" width="420px">
      <el-form label-width="100px">
        <el-form-item label="毛重(吨)" required>
          <el-input-number v-model="grossForm.grossWeight" :min="0" :precision="3" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grossVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGross">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 8px; align-items: center; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
