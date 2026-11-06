<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStockWarning } from '../../../api/report'

const loading = ref(false)
const list = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStockWarning()
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载库存预警失败')
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
        <span class="toolbar-title">库存预警</span>
        <div class="toolbar-actions">
          <el-tag v-if="list.length > 0" type="danger" effect="dark">
            共 {{ list.length }} 个煤种低于安全库存
          </el-tag>
          <el-button @click="loadData">刷新</el-button>
        </div>
      </div>

      <el-empty
        v-if="!loading && list.length === 0"
        description="库存充足，暂无预警"
        :image-size="100"
      />
      <el-table v-else :data="list" v-loading="loading" border stripe size="medium">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="productName" label="煤种" min-width="140" />
        <el-table-column prop="totalQty" label="当前库存(吨)" width="140" align="right" />
        <el-table-column prop="safeStock" label="安全库存(吨)" width="140" align="right" />
        <el-table-column label="缺口(吨)" width="130" align="right">
          <template #default="{ row }">
            <span class="gap">{{ (Number(row.safeStock) - Number(row.totalQty)).toFixed(3) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default>
            <el-tag type="danger" size="small">库存预警</el-tag>
          </template>
        </el-table-column>
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
  gap: 12px;
}
.gap {
  color: var(--danger);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
</style>
