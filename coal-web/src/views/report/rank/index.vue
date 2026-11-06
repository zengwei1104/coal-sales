<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import EChart from '../../../components/EChart.vue'
import { getCoalSalesRank } from '../../../api/report'

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

const option = computed(() => {
  const names = rank.value.map(i => i.productName)
  const qtys = rank.value.map(i => i.totalQty)
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { interval: 0, rotate: names.length > 6 ? 30 : 0 }
    },
    yAxis: { type: 'value', name: '销量(吨)' },
    series: [
      {
        name: '销量(吨)',
        type: 'bar',
        data: qtys,
        barMaxWidth: 48,
        itemStyle: { color: 'oklch(72% 0.16 75)', borderRadius: [4, 4, 0, 0] },
        label: { show: true, position: 'top' }
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
    const res = await getCoalSalesRank(query)
    rank.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载煤种排行失败')
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
        <span class="toolbar-title">煤炭销量排行（按煤种）</span>
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

    <!-- 排行明细表 -->
    <el-card v-if="rank.length > 0" shadow="never" class="table-card">
      <template #header>
        <span class="toolbar-title">销量明细</span>
      </template>
      <el-table :data="rank" size="small" border>
        <el-table-column type="index" label="排名" width="70" align="center" />
        <el-table-column prop="productName" label="煤种" min-width="140" />
        <el-table-column prop="totalQty" label="销量(吨)" width="140" align="right" />
        <el-table-column prop="totalAmount" label="销售额(元)" width="160" align="right" />
      </el-table>
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
.table-card {
  margin-top: 16px;
}
</style>
