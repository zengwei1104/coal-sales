<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSettlementPage, getSettlementDetail, createSettlement, auditSettlement, cancelSettlement
} from '../../../api/settlement'
import { getContractPage } from '../../../api/contract'
import { getSettleableWeighTickets } from '../../../api/weighTicket'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10, status: null })

// 合同下拉 + 可结算磅单
const contractOptions = ref([])
const settleableTickets = ref([])

const statusMap = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已审核', type: 'success' },
  2: { text: '已作废', type: 'danger' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSettlementPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const c = await getContractPage({ current: 1, size: 100})
  contractOptions.value = c.data.records
}

// 选合同后，拉取该合同下可结算磅单
const onContractChange = async contractId => {
  createForm.value.ticketIds = []
  settleableTickets.value = []
  if (!contractId) return
  const res = await getSettleableWeighTickets(contractId)
  settleableTickets.value = res.data
}

// ===== 生成结算单 =====
const createVisible = ref(false)
const createForm = ref({ contractId: null, ticketIds: [] })

const openCreate = () => {
  createForm.value = { contractId: null, ticketIds: [] }
  createVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.value.contractId) {
    ElMessage.warning('请选择合同')
    return
  }
  if (!createForm.value.ticketIds.length) {
    ElMessage.warning('请至少勾选一张磅单')
    return
  }
  await createSettlement({
    contractId: createForm.value.contractId,
    ticketIds: createForm.value.ticketIds
  })
  ElMessage.success('生成结算单成功')
  createVisible.value = false
  loadData()
  loadOptions()
}

// ===== 审核 =====
const auditVisible = ref(false)
const auditForm = ref({ id: null, pass: true, opinion: '' })

const openAudit = row => {
  auditForm.value = { id: row.id, pass: true, opinion: '' }
  auditVisible.value = true
}

const handleAudit = async () => {
  await auditSettlement(auditForm.value.id, {
    pass: auditForm.value.pass,
    opinion: auditForm.value.opinion
  })
  ElMessage.success('审核完成')
  auditVisible.value = false
  loadData()
}

// ===== 作废 =====
const handleCancel = row => {
  ElMessageBox.confirm(`确定作废结算单「${row.settleNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await cancelSettlement(row.id)
      ElMessage.success('作废成功')
      loadData()
    })
    .catch(() => {})
}

// ===== 查看明细 =====
const detailVisible = ref(false)
const detailItems = ref([])
const detailInfo = ref({})

const openDetail = async row => {
  const res = await getSettlementDetail(row.id)
  detailInfo.value = res.data.settlement
  detailItems.value = res.data.items
  detailVisible.value = true
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
          <el-option label="待审核" :value="0" />
          <el-option label="已审核" :value="1" />
          <el-option label="已作废" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button type="success" @click="openCreate">生成结算单</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="settleNo" label="结算单号" width="170" />
        <el-table-column prop="contractId" label="合同ID" width="90" />
        <el-table-column prop="customerId" label="客户ID" width="90" />
        <el-table-column prop="totalQty" label="结算净重(吨)" width="120" />
        <el-table-column prop="totalAmount" label="结算总额(元)" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">明细</el-button>
            <el-button v-if="row.status === 0" link type="warning" @click="openAudit(row)">审核</el-button>
            <el-button v-if="row.status !== 2" link type="danger" @click="handleCancel(row)">作废</el-button>
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

    <!-- 生成结算单 -->
    <el-dialog v-model="createVisible" title="生成结算单" width="640px">
      <el-form label-width="100px">
        <el-form-item label="选择合同" required>
          <el-select v-model="createForm.contractId" placeholder="选择执行中的合同" style="width: 100%" @change="onContractChange">
            <el-option v-for="c in contractOptions" :key="c.id" :label="c.contractNo" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择磅单" required>
          <el-table :data="settleableTickets" border max-height="300" @selection-change="v => createForm.ticketIds = v.map(r => r.id)">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="weightNo" label="磅单号" width="170" />
            <el-table-column prop="orderId" label="订单ID" width="90" />
            <el-table-column prop="plateNo" label="车牌" width="120" />
            <el-table-column prop="netWeight" label="净重(吨)" width="100" />
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 审核 -->
    <el-dialog v-model="auditVisible" title="审核结算单" width="480px">
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

    <!-- 明细 -->
    <el-dialog v-model="detailVisible" title="结算明细" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 16px">
        <el-descriptions-item label="结算单号">{{ detailInfo.settleNo }}</el-descriptions-item>
        <el-descriptions-item label="结算总额">{{ detailInfo.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="结算净重">{{ detailInfo.totalQty }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[detailInfo.status]?.text }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" border stripe>
        <el-table-column prop="ticketId" label="磅单ID" width="90" />
        <el-table-column prop="productId" label="煤种ID" width="90" />
        <el-table-column prop="netWeight" label="净重(吨)" width="110" />
        <el-table-column prop="price" label="单价(元/吨)" width="120" />
        <el-table-column prop="amount" label="金额(元)" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 8px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
