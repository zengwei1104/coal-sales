<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value.validate(async valid => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(form.value)
      userStore.setLoginInfo(res.data)
      ElMessage.success('登录成功')
      // 按角色落地：管理员/销售经理进工作台，其余角色进各自默认模块
      const roleCode = res.data.userInfo?.roleCode
      const roleDefaultPath = {
        SALES: '/biz/customer',
        WEIGHMAN: '/weigh/ticket',
        STOCK: '/stock/query',
        FINANCE: '/finance/settle'
      }
      router.push(roleDefaultPath[roleCode] || '/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="login-page">
    <!-- 左侧品牌区（工业风煤黑面板） -->
    <div class="brand-panel">
      <div class="brand-mark" aria-hidden="true">
        <svg viewBox="0 0 48 48" width="44" height="44" fill="none">
          <!-- 煤块 + 火焰，呼应「煤 + 能源」 -->
          <path d="M12 34l6-8 5 5 7-9 6 12" stroke="currentColor"
            stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round" />
          <path d="M18 26l4-6 4 5" stroke="currentColor" stroke-width="2.5"
            stroke-linejoin="round" stroke-linecap="round" opacity="0.6" />
        </svg>
      </div>
      <h1 class="brand-title">煤炭销售<br />管理系统</h1>
      <p class="brand-sub">
        覆盖客户、合同、订单、称重、<br />库存与结算的一体化作业平台
      </p>
      <div class="brand-foot">
        <span class="dot"></span>
        工业级 · 数据可信 · 高效作业
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-panel">
      <div class="form-wrap">
        <p class="eyebrow">COAL SALES SYSTEM</p>
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-sub">登录后进入工作台</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: grid;
  grid-template-columns: 44% 56%;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  background: var(--ink-950);
  color: var(--ink-50);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: var(--space-12);
  position: relative;
  overflow: hidden;
}
/* 工业质感：琥珀金的细网格线装饰 */
.brand-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(
      oklch(72% 0.16 75 / 0.06) 1px,
      transparent 1px
    ),
    linear-gradient(90deg, oklch(72% 0.16 75 / 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  pointer-events: none;
}
.brand-mark {
  color: var(--color-amber);
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid oklch(72% 0.16 75 / 0.4);
  border-radius: var(--radius-md);
}
.brand-title {
  font-size: 44px;
  line-height: 1.15;
  font-weight: 700;
  letter-spacing: 0.02em;
  margin: 0;
  margin-top: auto;
}
.brand-sub {
  color: oklch(74% 0.006 60);
  line-height: 1.9;
  margin: var(--space-6) 0 0;
  font-size: 15px;
}
.brand-foot {
  margin-top: var(--space-8);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: oklch(74% 0.006 60);
  font-size: 13px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-amber);
  box-shadow: 0 0 0 4px oklch(72% 0.16 75 / 0.18);
}

/* ===== 右侧表单区 ===== */
.form-panel {
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}
.form-wrap {
  width: 100%;
  max-width: 360px;
}
.eyebrow {
  color: var(--color-amber-strong);
  font-size: 12px;
  letter-spacing: 0.18em;
  font-weight: 600;
  margin: 0 0 var(--space-3);
}
.form-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  color: var(--ink-950);
}
.form-sub {
  color: var(--ink-600);
  margin: var(--space-2) 0 var(--space-8);
  font-size: 14px;
}
.login-form :deep(.el-input__wrapper) {
  padding: 4px 15px;
}
.login-btn {
  width: 100%;
  height: 44px;
  letter-spacing: 0.1em;
}

/* ===== 响应式：窄屏隐藏品牌区 ===== */
@media (max-width: 768px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .brand-panel {
    display: none;
  }
}
</style>
