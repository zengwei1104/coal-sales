<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductPage, addProduct, updateProduct, deleteProduct } from '../../../api/product'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增煤种商品')
const formRef = ref()
const submitting = ref(false)

const form = ref({
  id: null,
  productName: '',
  spec: '',
  qnetAr: null,
  sulfur: null,
  ash: null,
  origin: '',
  basePrice: null,
  safeStock: 0,
  status: 1,
  remark: ''
})

const rules = {
  productName: [{ required: true, message: '请输入煤种/商品名称', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基准价', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductPage(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  dialogTitle.value = '新增煤种商品'
  Object.assign(form.value, {
    id: null, productName: '', spec: '', qnetAr: null, sulfur: null,
    ash: null, origin: '', basePrice: null, safeStock: 0, status: 1, remark: ''
  })
  dialogVisible.value = true
}

const openEdit = row => {
  dialogTitle.value = '编辑煤种商品'
  Object.assign(form.value, {
    id: row.id, productName: row.productName, spec: row.spec,
    qnetAr: row.qnetAr, sulfur: row.sulfur, ash: row.ash,
    origin: row.origin, basePrice: row.basePrice, safeStock: row.safeStock,
    status: row.status, remark: row.remark
  })
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    submitting.value = true
    try {
      if (form.value.id) {
        await updateProduct(form.value.id, form.value)
        ElMessage.success('修改成功')
      } else {
        await addProduct(form.value)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = row => {
  ElMessageBox.confirm(`确定删除「${row.productName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteProduct(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openAdd">新增煤种商品</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="productName" label="煤种/商品名称" min-width="150" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="qnetAr" label="发热量(kcal/kg)" width="140" />
        <el-table-column prop="sulfur" label="硫分(%)" width="100" />
        <el-table-column prop="ash" label="灰分(%)" width="100" />
        <el-table-column prop="origin" label="产地" width="120" />
        <el-table-column prop="basePrice" label="基准价(元/吨)" width="130" />
        <el-table-column prop="safeStock" label="安全库存(吨)" width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="煤种/商品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" placeholder="如 3-6 块、粉煤" />
        </el-form-item>
        <el-form-item label="发热量(kcal/kg)">
          <el-input-number v-model="form.qnetAr" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="硫分(%)">
          <el-input-number v-model="form.sulfur" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="灰分(%)">
          <el-input-number v-model="form.ash" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="产地">
          <el-input v-model="form.origin" />
        </el-form-item>
        <el-form-item label="基准价(元/吨)" prop="basePrice">
          <el-input-number v-model="form.basePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存(吨)">
          <el-input-number v-model="form.safeStock" :min="0" :precision="3" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
