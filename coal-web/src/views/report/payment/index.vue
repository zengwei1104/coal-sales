<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import EChart from '../../../components/EChart.vue'
import { getPaymentRate } from '../../../api/report'

const loading = ref(false)

const defaultEnd = new Date()
const defaultStart = new Date()
defaultStart.setDate(defaultStart.getDate() - 29)
const fmt = d =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const query = reactive({
  startDate: fmt(defaultStart),
  endDate: fmt(defaultEnd)
})

const data = reactive({
  receivable: 0,
  received: 0,
  rate: 0
})

// 环形图：实收 vs 未收（应收 - 实收）
const option = computed(() => {
  const received = Number(data.received) || 0
  const receivable = Number(data.receivable) || 0
  const outstanding = Math.max(receivable - received, 0)
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 元 ({d}%)' },
    legend: { bottom: 0 },
    series: [
      {
        name: '回款构成',
        type: 'pie',
        radius: ['52%', '74%'],
        avoidLabelOverlap: false,
        label: { show: false },
        itemStyle: { borderRadius: 6, borderColor: 'oklch(98% 0.003 60)', borderWidth: 2 },
        data: [
          { value: received, name: '已回款', itemStyle: { color: 'oklch(72% 0.16 75)' } },
          { value: outstanding, name: '未回款', itemStyle: { color: 'oklch(58% 0.2 25)' } }
        ]
      }
    ]
  }
})

// 金额千分位格式化
const formatMoney = v => {
  const n = Number(v) || 0
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const loadData = async () => {
  if (!query.startDate || !query.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }
  loading.value = true
  try {
    const res = await getPaymentRate(query)
    const d = res.data || {}
    data.receivable = d.receivable ?? 0
    data.received = d.received ?? 0
    data.rate = d.rate ?? 0
  } catch (e) {
    ElMessage.error(e.message || '加载回款率失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="toolbar-title">回款率统计</span>
        <div class="toolbar-actions">
          <el-date-picker
            v-model="query.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
          <span class="sep">至</span>
          <el-date-picker
            v-model="query.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
      </div>

      <div v-loading="loading">
        <!-- 指标卡 -->
        <div class="metric-grid">
          <div class="metric-card">
            <div class="metric-label">应收金额（已审核）</div>
            <div class="metric-num">¥{{ formatMoney(data.receivable) }}</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">实收金额</div>
            <div class="metric-num">¥{{ formatMoney(data.received) }}</div>
          </div>
          <div class="metric-card highlight">
            <div class="metric-label">回款率</div>
            <div class="metric-num">{{ Number(data.rate).toFixed(2) }}%</div>
          </div>
        </div>

        <!-- 环形图 -->
        <el-empty v-if="!loading && Number(data.receivable) === 0" description="暂无应收数据" :image-size="90" />
        <EChart v-else :option="option" height="360px" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page { padding: 0; }
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-900);
}
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sep {
  color: var(--ink-500);
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.metric-card {
  padding: 20px;
  border: 1px solid var(--ink-200);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}
.metric-card.highlight {
  background: var(--color-amber-tint);
  border-color: var(--color-amber);
}
.metric-label {
  font-size: 13px;
  color: var(--ink-600);
  margin-bottom: 8px;
}
.metric-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--ink-950);
  font-variant-numeric: tabular-nums;
}
.metric-card.highlight .metric-num {
  color: var(--color-amber-strong);
}

@media (max-width: 768px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
