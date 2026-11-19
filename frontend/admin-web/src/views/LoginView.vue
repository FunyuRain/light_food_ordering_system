<template>
  <div class="login-page">
    <section class="story">
      <div class="story-top">
        <div class="brand">
          <div class="brand-mark"><span></span></div>
          <div>
            <p>LIGHT FOOD</p>
            <strong>轻食点单 · 管理台</strong>
          </div>
        </div>
        <span class="edition">EST. 2026</span>
      </div>

      <div class="story-copy">
        <p class="eyebrow">FRESH / SIMPLE / DAILY</p>
        <h1>把每一份<br /><em>轻盈</em>照顾好。</h1>
        <p class="lead">从分类、菜品到订单，用更清爽的方式管理每一餐。</p>
      </div>

      <div class="story-bottom">
        <div class="metric">
          <strong>8</strong>
          <span>核心数据表</span>
        </div>
        <div class="metric">
          <strong>5</strong>
          <span>订单状态</span>
        </div>
        <div class="metric">
          <strong>24h</strong>
          <span>轻食在线</span>
        </div>
      </div>
      <div class="story-ring ring-one"></div>
      <div class="story-ring ring-two"></div>
    </section>

    <section class="form-side">
      <div class="login-card">
        <p class="form-eyebrow">{{ mode === 'login' ? 'ADMIN ACCESS' : 'ADMIN SIGN UP' }}</p>
        <h2>{{ mode === 'login' ? '欢迎回来' : '创建管理员账号' }}</h2>
        <p class="form-sub">
          {{ mode === 'login' ? '登录后管理菜品、分类与今日订单。' : '注册后可管理菜单、分类和订单。' }}
        </p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="onSubmit"
        >
          <el-form-item label="管理员账号" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入管理员账号"
              autocomplete="username"
              :prefix-icon="User"
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-form-item v-if="mode === 'register'" label="管理员姓名（选填）" prop="realName">
            <el-input
              v-model="form.realName"
              maxlength="50"
              placeholder="例如：门店管理员"
              :prefix-icon="UserFilled"
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              :placeholder="mode === 'login' ? '请输入登录密码' : '设置6-64位密码'"
              :autocomplete="mode === 'login' ? 'current-password' : 'new-password'"
              :prefix-icon="Lock"
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-form-item v-if="mode === 'register'" label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入密码"
              autocomplete="new-password"
              :prefix-icon="Lock"
              @keyup.enter="onSubmit"
            />
          </el-form-item>

          <el-button
            class="submit"
            type="primary"
            native-type="submit"
            :loading="loading"
            @click="onSubmit"
          >
            {{ mode === 'login' ? '进入管理台' : '注册并进入管理台' }}
            <el-icon class="submit-arrow"><ArrowRight /></el-icon>
          </el-button>
        </el-form>

        <div v-if="mode === 'login'" class="demo-box">
          <div>
            <span>演示账号</span>
            <strong>admin / admin123</strong>
          </div>
          <el-button text type="primary" @click="fillDemo">一键填入</el-button>
        </div>

        <div class="mode-switch">
          <template v-if="mode === 'login'">
            <span>还没有管理员账号？</span>
            <el-button text type="primary" @click="switchMode('register')">注册管理员账号</el-button>
          </template>
          <template v-else>
            <span>已经有管理员账号？</span>
            <el-button text type="primary" @click="switchMode('login')">返回登录</el-button>
          </template>
        </div>

        <p class="form-foot">LIGHT FOOD · 让健康餐更简单</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Lock, User, UserFilled } from '@element-plus/icons-vue'
import { login, register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref()
const loading = ref(false)
const mode = ref('login')
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: ''
})

const rules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度需为6-64个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }],
  realName: [{ max: 50, message: '不能超过50个字符', trigger: 'blur' }]
}

function fillDemo() {
  form.username = 'admin'
  form.password = 'admin123'
}

function switchMode(next) {
  mode.value = next
  form.password = ''
  form.confirmPassword = ''
  form.realName = ''
  formRef.value?.clearValidate()
}

async function onSubmit() {
  if (loading.value) return
  await formRef.value.validate()
  if (mode.value === 'register' && form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    const data = mode.value === 'register'
      ? await register({
          username: form.username,
          password: form.password,
          confirmPassword: form.confirmPassword,
          realName: form.realName
        })
      : await login({ username: form.username, password: form.password })
    auth.setSession(data)
    ElMessage.success(mode.value === 'register' ? '管理员账号注册成功' : `欢迎回来，${data.name}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/orders'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(460px, 1.08fr) minmax(480px, 0.92fr);
  background: #f6f7f2;
}

.story {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 42px 56px 42px;
  color: #f1f7ea;
  background:
    radial-gradient(circle at 70% 10%, rgba(184, 214, 157, 0.25), transparent 30%),
    linear-gradient(155deg, #1d3324 0%, #2b4b34 58%, #223d2c 100%);
}

.story-top {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.brand {
  display: flex;
  align-items: center;
  gap: 13px;
}

.brand-mark {
  width: 45px;
  height: 45px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #d8ebca, #8eb77c);
}

.brand-mark span {
  width: 18px;
  height: 25px;
  border-radius: 18px 4px 18px 4px;
  background: #27422f;
  transform: rotate(35deg);
}

.brand p {
  margin: 0 0 4px;
  font-size: 9px;
  letter-spacing: 0.26em;
  color: rgba(231, 244, 219, 0.56);
}

.brand strong {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.08em;
}

.edition {
  font-size: 9px;
  letter-spacing: 0.2em;
  color: rgba(236, 246, 226, 0.4);
}

.story-copy {
  position: relative;
  z-index: 2;
  margin: auto 0;
  max-width: 620px;
  padding: 40px 0;
}

.eyebrow {
  margin: 0 0 24px;
  font-size: 10px;
  letter-spacing: 0.3em;
  color: #b5d19f;
}

.story-copy h1 {
  margin: 0;
  font-size: clamp(48px, 5.4vw, 82px);
  line-height: 1.12;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.story-copy h1 em {
  color: #c3dda9;
  font-style: normal;
  position: relative;
}

.story-copy h1 em::after {
  content: '';
  position: absolute;
  left: 2px;
  right: 2px;
  bottom: 4px;
  height: 9px;
  border-radius: 999px;
  background: rgba(163, 199, 132, 0.18);
}

.lead {
  max-width: 390px;
  margin: 28px 0 0;
  font-size: 15px;
  line-height: 2;
  color: rgba(236, 246, 227, 0.64);
}

.story-bottom {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 42px;
  padding-top: 26px;
  border-top: 1px solid rgba(224, 241, 210, 0.12);
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.metric strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 22px;
  font-weight: 400;
  color: #d5e8c3;
}

.metric span {
  font-size: 10px;
  letter-spacing: 0.12em;
  color: rgba(236, 246, 227, 0.44);
}

.story-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(214, 236, 195, 0.13);
  pointer-events: none;
}

.ring-one {
  width: 540px;
  height: 540px;
  right: -180px;
  bottom: -190px;
}

.ring-two {
  width: 330px;
  height: 330px;
  right: -80px;
  bottom: -85px;
  background: rgba(201, 229, 175, 0.04);
}

.form-side {
  display: grid;
  place-items: center;
  padding: 42px;
  background:
    radial-gradient(circle at 78% 16%, rgba(219, 232, 206, 0.6), transparent 32%),
    #f6f7f2;
}

.login-card {
  width: min(420px, 100%);
}

.form-eyebrow {
  margin: 0 0 14px;
  font-size: 9px;
  letter-spacing: 0.28em;
  color: #95a28f;
}

.login-card h2 {
  margin: 0;
  font-size: 36px;
  font-weight: 500;
  letter-spacing: 0.02em;
  color: #203423;
}

.form-sub {
  margin: 12px 0 32px;
  font-size: 13px;
  line-height: 1.8;
  color: #83907e;
}

:deep(.el-form-item__label) {
  padding-bottom: 7px;
  font-size: 12px;
  color: #5e7059;
}

:deep(.el-input__wrapper) {
  min-height: 50px;
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 1px rgba(57, 81, 55, 0.1) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #6c9261 inset, 0 8px 22px rgba(73, 106, 68, 0.08);
}

.submit {
  width: 100%;
  height: 50px;
  margin-top: 10px;
  border: 0;
  border-radius: 13px;
  font-size: 14px;
  letter-spacing: 0.06em;
  background: linear-gradient(100deg, #4b7448, #345a39);
  box-shadow: 0 14px 28px rgba(53, 90, 57, 0.2);
}

.submit:hover {
  background: linear-gradient(100deg, #557f50, #3d6542);
}

.submit-arrow {
  margin-left: 8px;
  transition: transform 0.2s ease;
}

.submit:hover .submit-arrow {
  transform: translateX(3px);
}

.demo-box {
  margin-top: 30px;
  padding: 13px 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 13px;
  background: #eef3e9;
  border: 1px solid rgba(91, 125, 82, 0.1);
}

.demo-box div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.demo-box span {
  font-size: 10px;
  letter-spacing: 0.1em;
  color: #8b9985;
}

.demo-box strong {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 12px;
  font-weight: 500;
  color: #4b6647;
}

.mode-switch {
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 11px;
  color: #8e9a88;
}

.mode-switch .el-button {
  padding: 0;
  font-size: 11px;
}

.form-foot {
  margin: 28px 0 0;
  font-size: 9px;
  letter-spacing: 0.2em;
  text-align: center;
  color: #a3aea0;
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .story {
    display: none;
  }

  .form-side {
    padding: 28px;
  }
}
</style>
