<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  User,
  Document,
  Tickets,
  Box,
  Warning,
  Van
} from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import {
  getCustomerTotal,
  getContractPage,
  getOrderPage,
  getStocks,
  getWeighTicketPage,
  getSettlementPage,
  getPaymentPage
} from '../../api/dashboard'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => {
  const info = userStore.userInfo || {}
  return info.realName || info.username || '用户'
})

// 今天的日期 + 问候语
const today = new Date()
const dateText = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`
const greeting = computed(() => {
  const h = today.getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const loading = ref(true)

// ===== 统计数据 =====
const stats = reactive({
  customer: 0,
  contract: 0,
  weigh: 0,
  settle: 0
})

// ===== 订单状态分布 =====
const orderStatus = reactive({
  pending: 0,     // 待发运
  partial: 0,     // 部分发运
  done: 0,        // 已完成
  cancelled: 0    // 已取消
})
const orderStatusTotal = computed(() =>
  orderStatus.pending + orderStatus.partial + orderStatus.done + orderStatus.cancelled
)

// ===== 库存数据 =====
const stocks = ref([])
const warningStocks = computed(() => stocks.value.filter(s => s.warning))

// ===== 待发运订单 =====
const pendingOrders = ref([])

// 订单状态文案
const orderStatusMap = { 1: '待发运', 2: '部分发运', 3: '已完成', 4: '已取消' }
const orderStatusTag = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' }

// 取分页接口的总数（size=1，只关心 total 字段）
const fetchTotal = async fn => {
  try {
    const res = await fn({ current: 1, size: 1 })
    return res?.data?.total ?? 0
  } catch (e) {
    return 0
  }
}

// 统计各订单状态数量（分状态各取一次 total）
const fetchOrderStatus = async () => {
  const keys = [
    ['pending', 1],
    ['partial', 2],
    ['done', 3],
    ['cancelled', 4]
  ]
  await Promise.all(
    keys.map(async ([key, status]) => {
      try {
        const res = await getOrderPage({ current: 1, size: 1, status })
        orderStatus[key] = res?.data?.total ?? 0
      } catch (e) {
        orderStatus[key] = 0
      }
    })
  )
}

const loadData = async () => {
  loading.value = true
  try {
    // 并行拉取所有数据，单个失败不影响整体
    const [customerTotal, contractTotal, weighTotal, settleTotal, stockRes] =
      await Promise.all([
        fetchTotal(getCustomerTotal),
        fetchTotal(getContractPage),
        fetchTotal(getWeighTicketPage),
        fetchTotal(getSettlementPage),
        getStocks().catch(() => ({ data: [] }))
      ])

    stats.customer = customerTotal
    stats.contract = contractTotal
    stats.weigh = weighTotal
    stats.settle = settleTotal

    stocks.value = stockRes?.data ?? []

    // 待发运订单：取 status=1 的前 5 条
    try {
      const pendingRes = await getOrderPage({ current: 1, size: 5, status: 1 })
      pendingOrders.value = pendingRes?.data?.records ?? []
      orderStatus.pending = pendingRes?.data?.total ?? 0
    } catch (e) {
      pendingOrders.value = []
    }

    // 其余订单状态分布
    await fetchOrderStatus()
  } finally {
    loading.value = false
  }
}

// 跳转到对应模块
const goTo = path => router.push(path)

onMounted(loadData)
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <!-- 顶部欢迎区 -->
    <div class="welcome">
      <div>
        <div class="welcome-title">{{ greeting }}，{{ username }}</div>
        <div class="welcome-sub">今天是 {{ dateText }}，祝你工作顺利</div>
      </div>
      <el-button type="primary" @click="goTo('/order/list')">+ 新建订单</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <el-card shadow="never" class="stat-card" @click="goTo('/biz/customer')">
        <div class="stat-inner">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div>
            <div class="stat-num">{{ stats.customer }}</div>
            <div class="stat-label">客户总数</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card" @click="goTo('/biz/contract')">
        <div class="stat-inner">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div>
            <div class="stat-num">{{ stats.contract }}</div>
            <div class="stat-label">合同总数</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card" @click="goTo('/weigh/ticket')">
        <div class="stat-inner">
          <div class="stat-icon"><el-icon><Tickets /></el-icon></div>
          <div>
            <div class="stat-num">{{ stats.weigh }}</div>
            <div class="stat-label">磅单总数</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card" @click="goTo('/finance/settle')">
        <div class="stat-inner">
          <div class="stat-icon"><el-icon><Box /></el-icon></div>
          <div>
            <div class="stat-num">{{ stats.settle }}</div>
            <div class="stat-label">结算单数</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 订单状态分布 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="section-header">
          <span>订单状态分布</span>
          <el-button link type="primary" @click="goTo('/order/list')">查看全部</el-button>
        </div>
      </template>
      <div class="status-row">
        <div class="status-item">
          <div class="status-num warn">{{ orderStatus.pending }}</div>
          <div class="status-label">待发运</div>
        </div>
        <div class="status-item">
          <div class="status-num primary">{{ orderStatus.partial }}</div>
          <div class="status-label">部分发运</div>
        </div>
        <div class="status-item">
          <div class="status-num success">{{ orderStatus.done }}</div>
          <div class="status-label">已完成</div>
        </div>
        <div class="status-item">
          <div class="status-num info">{{ orderStatus.cancelled }}</div>
          <div class="status-label">已取消</div>
        </div>
        <div class="status-item">
          <div class="status-num total">{{ orderStatusTotal }}</div>
          <div class="status-label">合计</div>
        </div>
      </div>
    </el-card>

    <div class="two-col">
      <!-- 库存预警 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="section-header">
            <span><el-icon color="#f56c6c"><Warning /></el-icon> 库存预警</span>
            <el-button link type="primary" @click="goTo('/stock/query')">查看库存</el-button>
          </div>
        </template>
        <el-empty
          v-if="warningStocks.length === 0"
          description="暂无库存预警"
          :image-size="80"
        />
        <el-table v-else :data="warningStocks" size="small">
          <el-table-column prop="productName" label="煤种" min-width="120" />
          <el-table-column prop="totalQty" label="当前库存(吨)" width="120" />
          <el-table-column prop="safeStock" label="安全库存(吨)" width="120" />
          <el-table-column label="状态" width="90">
            <template #default>
              <el-tag type="danger" size="small">预警</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 待发运订单 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="section-header">
            <span><el-icon color="#409eff"><Van /></el-icon> 待发运订单</span>
            <el-button link type="primary" @click="goTo('/order/list')">查看全部</el-button>
          </div>
        </template>
        <el-empty
          v-if="pendingOrders.length === 0"
          description="暂无待发运订单"
          :image-size="80"
        />
        <el-table v-else :data="pendingOrders" size="small">
          <el-table-column prop="orderNo" label="订单编号" min-width="140" />
          <el-table-column prop="planQty" label="计划量(吨)" width="100" />
          <el-table-column prop="deliveryDate" label="要求发货" width="110" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="orderStatusTag[row.status]" size="small">
                {{ orderStatusMap[row.status] }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100%;
}

.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px 20px;
}
.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-950);
}
.welcome-sub {
  margin-top: 6px;
  font-size: 14px;
  color: var(--ink-600);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  cursor: pointer;
  border: 1px solid var(--ink-200);
  transition: border-color 0.2s ease;
}
.stat-card:hover {
  border-color: var(--color-amber);
}
.stat-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  background: var(--color-amber-tint);
  color: var(--color-amber-strong);
}
.stat-num {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
  color: var(--ink-950);
  font-variant-numeric: tabular-nums;
}
.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: var(--ink-600);
}

.section-card {
  margin-bottom: 16px;
  border: 1px solid var(--ink-200);
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: var(--ink-950);
}
.section-header .el-icon {
  vertical-align: -2px;
}

.status-row {
  display: flex;
  justify-content: space-around;
  text-align: center;
}
.status-item {
  flex: 1;
}
.status-num {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}
.status-num.warn { color: var(--warning); }
.status-num.primary { color: var(--color-amber-strong); }
.status-num.success { color: var(--success); }
.status-num.info { color: var(--ink-500); }
.status-num.total { color: var(--ink-950); }
.status-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-600);
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* 移动端：单列堆叠 */
@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .two-col {
    grid-template-columns: 1fr;
  }
  .welcome {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
