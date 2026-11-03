<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStocks, checkStock } from '../../../api/stock'

const list = ref([])
const loading = ref(false)

// 盘点弹窗
const dialogVisible = ref(false)
const formRef = ref()
const submitting = ref(false)
const currentRow = ref(null)

// changeQty 可正可负：正=盘盈，负=盘亏
const form = ref({
  changeQty: null,
  remark: ''
})

const rules = {
  changeQty: [{ required: true, message: '请输入调整数量', trigger: 'blur' }],
  remark: [{ required: true, message: '请填写调整原因', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStocks()
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载库存失败')
  } finally {
    loading.value = false
  }
}

const openCheck = row => {
  currentRow.value = row
  Object.assign(form.value, { changeQty: null, remark: '' })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      await checkStock({
        productId: currentRow.value.productId,
        changeQty: form.value.changeQty,
        remark: form.value.remark
      })
      ElMessage.success('盘点调整成功')
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '盘点失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-button @click="loadData">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="productName" label="煤种/商品名称" min-width="150" />
        <el-table-column prop="totalQty" label="当前库存(吨)" width="130" />
        <el-table-column prop="safeStock" label="安全库存(吨)" width="130" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCheck(row)">盘点调整</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无库存数据" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="盘点调整" width="480px">
      <el-alert
        v-if="currentRow"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #title>
          煤种：{{ currentRow.productName }}，当前库存 {{ currentRow.totalQty }} 吨
        </template>
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="调整数量" prop="changeQty">
          <el-input-number v-model="form.changeQty" :precision="3" style="width: 100%" />
          <div class="tip">正数 = 盘盈（库存增加），负数 = 盘亏（库存减少）</div>
        </el-form-item>
        <el-form-item label="调整原因" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="必填，如：煤堆自然损耗" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 16px; }
.tip { font-size: 12px; color: #909399; line-height: 1.6; }
</style>
