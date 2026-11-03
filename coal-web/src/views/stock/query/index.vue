<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStocks, inboundStock } from '../../../api/stock'
import { getProductPage } from '../../../api/product'

const list = ref([])
const loading = ref(false)

// 入库弹窗
const dialogVisible = ref(false)
const formRef = ref()
const submitting = ref(false)
const products = ref([])

const form = ref({
  productId: null,
  changeQty: null,
  remark: ''
})

const rules = {
  productId: [{ required: true, message: '请选择煤种商品', trigger: 'change' }],
  changeQty: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]
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

// 打开入库弹窗前，加载煤种商品下拉选项
const openInbound = async () => {
  Object.assign(form.value, { productId: null, changeQty: null, remark: '' })
  dialogVisible.value = true
  try {
    const res = await getProductPage({ current: 1, size: 100 })
    products.value = (res.data && res.data.records) || []
  } catch (e) {
    ElMessage.error('加载煤种商品失败')
  }
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      await inboundStock(form.value)
      ElMessage.success('入库成功')
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '入库失败')
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
        <el-button type="primary" @click="openInbound">采购入库</el-button>
        <el-button @click="loadData">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="productName" label="煤种/商品名称" min-width="150" />
        <el-table-column prop="totalQty" label="当前库存(吨)" width="130" />
        <el-table-column prop="safeStock" label="安全库存(吨)" width="130" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.warning ? 'danger' : 'success'">
              {{ row.warning ? '库存预警' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无库存数据" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="采购入库" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="煤种商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择煤种商品" filterable style="width: 100%">
            <el-option
              v-for="p in products"
              :key="p.id"
              :label="p.productName"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="changeQty">
          <el-input-number v-model="form.changeQty" :min="0.001" :precision="3" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="选填" />
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
</style>
