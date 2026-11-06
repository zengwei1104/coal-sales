<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import EChart from '../../../components/EChart.vue'
import { getSalesTrend } from '../../../api/report'

const loading = ref(false)

// 默认查询最近 30 天
const defaultEnd = new Date()
const defaultStart = new Date()
defaultStart.setDate(defaultStart.getDate() - 29)

const fmt = d =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const query = reactive({
  startDate: fmt(defaultStart),
  endDate: fmt(defaultEnd)
})

const trend = ref([])

const option = computed(() => {
  const dates = trend.value.map(i => i.date)
  const qtys = trend.value.map(i => i.settleQty)
  const amounts = trend.value.map(i => i.settleAmount)
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['结算净重(吨)', '结算金额(元)'], top: 0 },
    grid: { left: 60, right: 60, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '吨' },
      { type: 'value', name: '元' }
    ],
    series: [
      {
        name: '结算净重(吨)',
        type: 'line',
        smooth: true,
        data: qtys,
        itemStyle: { color: 'oklch(72% 0.16 75)' }
      },
      {
        name: '结算金额(元)',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: amounts,
        itemStyle: { color: 'oklch(58% 0.2 25)' }
      }
    ]
  }
})

const loadData = async () => {
  if (!query.startDate || !query.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }
  loading.value = true
  try {
    const res = await getSalesTrend(query)
    trend.value = res.data || []
    if (trend.value.length === 0) {
      ElMessage.info('该时间段内暂无已审核结算数据')
    }
  } catch (e) {
    ElMessage.error(e.message || '加载销售趋势失败')
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
        <span class="toolbar-title">销售趋势（按结算审核日聚合）</span>
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
        <el-empty v-if="!loading && trend.length === 0" description="暂无数据" :image-size="90" />
        <EChart v-else :option="option" height="420px" />
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
</style>
