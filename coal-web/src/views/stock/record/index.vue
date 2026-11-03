<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockRecordPage } from '../../../api/stock'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10 })

// 来源类型 → 中文 + 标签颜色
const sourceTypeMap = {
  INBOUND: { text: '入库', type: 'success' },
  TICKET: { text: '磅单出库', type: 'warning' },
  CHECK: { text: '盘点调整', type: 'primary' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStockRecordPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <el-card>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="productName" label="煤种/商品名称" min-width="150" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="(sourceTypeMap[row.sourceType] || {}).type || 'info'">
              {{ (sourceTypeMap[row.sourceType] || {}).text || row.sourceType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeQty" label="变动数量(吨)" width="130">
          <template #default="{ row }">
            <span :style="{ color: row.changeQty > 0 ? '#e6a23c' : '#67c23a', fontWeight: 600 }">
              {{ row.changeQty > 0 ? '+' : '' }}{{ row.changeQty }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQty" label="变动前(吨)" width="120" />
        <el-table-column prop="afterQty" label="变动后(吨)" width="120" />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" width="180" />
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
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
