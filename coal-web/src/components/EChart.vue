<script setup>
import { ref, shallowRef, onMounted, onBeforeUnmount, watch } from 'vue'
// 按需引入 ECharts 核心与所需图表/组件，避免全量打包
import * as echarts from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
])

const props = defineProps({
  // ECharts 配置项（option 对象）
  option: {
    type: Object,
    required: true
  },
  // 图表高度，默认 360px
  height: {
    type: String,
    default: '360px'
  }
})

const chartRef = ref(null)
const chart = shallowRef(null)

const render = () => {
  if (!chart.value) return
  chart.value.setOption(props.option, true)
}

// 监听 option 变化自动重绘
watch(() => props.option, render, { deep: true })

const resize = () => {
  chart.value && chart.value.resize()
}

onMounted(() => {
  chart.value = echarts.init(chartRef.value)
  render()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart.value && chart.value.dispose()
  chart.value = null
})
</script>

<template>
  <div ref="chartRef" class="echart" :style="{ height }"></div>
</template>

<style scoped>
.echart {
  width: 100%;
}
</style>
