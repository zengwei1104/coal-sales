<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import EChart from '../../../components/EChart.vue'
import { getCustomerRank } from '../../../api/report'

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

const rank = ref([])

// 横向条形图：金额从高到低，客户多的场景更易读
const option = computed(() => {
  const data = [...rank.value].sort((a, b) => a.amount - b.amount)
  const names = data.map(i => i.customerName)
  const amounts = data.map(i => i.amount)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 140, right: 40, top: 20, bottom: 30 },
    xAxis: { type: 'value', name: '销售额(元)' },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { width: 120, overflow: 'truncate' }
    },
    series: [
      {
        name: '销售额(元)',
        type: 'bar',
        data: amounts,
        barMaxWidth: 28,
        itemStyle: { color: 'oklch(72% 0.16 75)', borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right' }
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
    const res = await getCustomerRank(query)
    rank.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载客户排行失败')
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
        <span class="toolbar-title">客户销售额排行</span>
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
        <el-empty v-if="!loading && rank.length === 0" description="暂无数据" :image-size="90" />
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
