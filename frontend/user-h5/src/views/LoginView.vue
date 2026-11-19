<template>
  <div class="login-page">
    <div class="hero">
      <div class="hero-top">
        <div class="brand">
          <span class="brand-mark"><i></i></span>
          <span>LIGHT FOOD</span>
        </div>
        <span class="hero-note">EAT LIGHT · LIVE WELL</span>
      </div>

      <div class="hero-copy">
        <p class="eyebrow">DAILY FRESH BOWL</p>
        <h1>好好吃饭，<br /><em>轻盈</em>生活。</h1>
        <p>新鲜食材、低负担搭配，给忙碌日常留一点清爽。</p>
      </div>

      <div class="hero-orbit orbit-one"></div>
      <div class="hero-orbit orbit-two"></div>
      <div class="hero-leaf leaf-one"></div>
      <div class="hero-leaf leaf-two"></div>
    </div>

    <div class="login-panel">
      <div class="panel-head">
        <p class="eyebrow">{{ mode === 'login' ? 'WELCOME BACK' : 'JOIN LIGHT FOOD' }}</p>
        <h2>{{ mode === 'login' ? '登录轻食点单' : '创建轻食账号' }}</h2>
        <p>{{ mode === 'login' ? '使用手机号或账号开始今天的轻食之旅。' : '注册后直接进入点餐页，无需验证码。' }}</p>
      </div>

      <van-form ref="formRef" class="login-form" @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            :label="mode === 'login' ? '手机号' : '用户名'"
            :placeholder="mode === 'login' ? '请输入手机号或账号' : '设置3-50位用户名'"
            left-icon="user-o"
            autocomplete="username"
            :rules="[{ required: true, message: mode === 'login' ? '请输入手机号或账号' : '请输入用户名' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            :placeholder="mode === 'login' ? '请输入密码' : '设置6-64位密码'"
            left-icon="lock"
            :autocomplete="mode === 'login' ? 'current-password' : 'new-password'"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
          <van-field
            v-if="mode === 'register'"
            v-model="form.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            left-icon="lock"
            autocomplete="new-password"
            :rules="[{ required: true, message: '请再次输入密码' }]"
          />
        </van-cell-group>

        <div class="form-actions">
          <van-button round block type="primary" native-type="submit" :loading="loading" class="login-btn">
            {{ mode === 'login' ? '开始点餐' : '注册并开始点餐' }}
          </van-button>
        </div>
      </van-form>

      <div v-if="mode === 'login'" class="demo-card">
        <div>
          <span>测试手机号</span>
          <strong>13800000000 / admin123</strong>
        </div>
        <button type="button" @click="fillDemo">一键填入</button>
      </div>

      <div class="mode-switch">
        <template v-if="mode === 'login'">
          <span>还没有账号？</span>
          <button type="button" @click="switchMode('register')">去注册</button>
        </template>
        <template v-else>
          <span>已经有账号？</span>
          <button type="button" @click="switchMode('login')">返回登录</button>
        </template>
      </div>

      <p class="login-foot">密码使用 BCrypt 加密存储 · 无需验证码</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { login, register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { showTopError } from '@/utils/errorToast'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const cart = useCartStore()

const loading = ref(false)
const mode = ref('login')
const formRef = ref(null)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

function fillDemo() {
  form.username = '13800000000'
  form.password = 'admin123'
}

function switchMode(next) {
  mode.value = next
  form.password = ''
  form.confirmPassword = ''
  formRef.value?.resetValidation()
}

async function onSubmit() {
  if (loading.value) return
  if (mode.value === 'register') {
    if (form.username.trim().length < 3) {
      showTopError('用户名至少3个字符')
      return
    }
    if (form.password.length < 6) {
      showTopError('密码至少6个字符')
      return
    }
    if (form.password !== form.confirmPassword) {
      showTopError('两次输入的密码不一致')
      return
    }
  }
  loading.value = true
  try {
    const data = mode.value === 'register'
      ? await register({ ...form, username: form.username.trim() })
      : await login({ username: form.username, password: form.password })
    auth.setSession(data)
    await cart.refresh()
    showSuccessToast(mode.value === 'register' ? '注册成功，欢迎加入' : `欢迎，${data.name}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/menu'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 90% 4%, rgba(219, 230, 203, 0.8), transparent 30%),
    #f8f7f2;
}

.hero {
  position: relative;
  overflow: hidden;
  min-height: 335px;
  padding: 28px 26px 40px;
  color: #f1f7e9;
  background:
    radial-gradient(circle at 74% 20%, rgba(192, 220, 164, 0.25), transparent 32%),
    linear-gradient(152deg, #223c29 0%, #31533a 62%, #24442e 100%);
  border-radius: 0 0 34px 34px;
}

.hero-top {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 10px;
  letter-spacing: 0.22em;
  color: rgba(239, 249, 230, 0.78);
}

.brand-mark {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: linear-gradient(145deg, #d9eacb, #8fb77d);
}

.brand-mark i {
  width: 10px;
  height: 15px;
  border-radius: 10px 2px 10px 2px;
  background: #2a442f;
  transform: rotate(35deg);
}

.hero-note {
  font-size: 7px;
  letter-spacing: 0.18em;
  color: rgba(236, 248, 225, 0.38);
}

.hero-copy {
  position: relative;
  z-index: 2;
  margin-top: 64px;
  max-width: 340px;
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 8px;
  letter-spacing: 0.26em;
  color: #b8d39f;
}

.hero-copy h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.18;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.hero-copy h1 em {
  position: relative;
  color: #c7dfad;
  font-style: normal;
}

.hero-copy h1 em::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 4px;
  height: 6px;
  border-radius: 999px;
  background: rgba(189, 220, 157, 0.16);
}

.hero-copy > p:last-child {
  margin: 16px 0 0;
  max-width: 280px;
  font-size: 12px;
  line-height: 1.8;
  color: rgba(236, 247, 226, 0.6);
}

.hero-orbit {
  position: absolute;
  border: 1px solid rgba(214, 236, 196, 0.12);
  border-radius: 50%;
}

.orbit-one {
  width: 310px;
  height: 310px;
  right: -115px;
  top: 30px;
}

.orbit-two {
  width: 190px;
  height: 190px;
  right: -55px;
  top: 90px;
  background: rgba(204, 230, 179, 0.04);
}

.hero-leaf {
  position: absolute;
  width: 52px;
  height: 76px;
  border-radius: 70% 8% 70% 8%;
  background: linear-gradient(145deg, rgba(196, 222, 168, 0.32), rgba(124, 165, 108, 0.1));
  transform: rotate(28deg);
}

.leaf-one {
  right: 36px;
  bottom: 40px;
}

.leaf-two {
  right: 88px;
  bottom: 18px;
  width: 28px;
  height: 44px;
  transform: rotate(-18deg);
  opacity: 0.55;
}

.login-panel {
  position: relative;
  z-index: 3;
  margin: -24px 18px 0;
  padding: 26px 18px 24px;
  border-radius: 26px;
  background: #fdfcf8;
  box-shadow: 0 18px 40px rgba(46, 68, 43, 0.08);
  border: 1px solid rgba(66, 91, 62, 0.05);
}

.panel-head {
  padding: 0 6px;
}

.panel-head h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: #253d28;
}

.panel-head > p:last-child {
  margin: 8px 0 18px;
  font-size: 11px;
  color: #929e8c;
}

.login-form :deep(.van-cell-group--inset) {
  margin: 0;
  border-radius: 16px;
  overflow: hidden;
  background: #f6f8f2;
}

.login-form :deep(.van-field) {
  padding: 13px 14px;
  background: #f6f8f2;
}

.login-form :deep(.van-field__label) {
  width: 42px;
  color: #6e7f69;
  font-size: 12px;
}

.login-form :deep(.van-field__control) {
  color: #304833;
  font-size: 13px;
}

.login-form :deep(.van-icon) {
  color: #8ca482;
}

.form-actions {
  margin-top: 18px;
}

.login-btn {
  height: 48px;
  border: 0;
  font-size: 14px;
  letter-spacing: 0.08em;
  background: linear-gradient(100deg, #507b4b, #345b3a);
  box-shadow: 0 12px 24px rgba(59, 91, 53, 0.18);
}

.demo-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f0f4eb;
}

.demo-card div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.demo-card span {
  font-size: 9px;
  letter-spacing: 0.1em;
  color: #97a28f;
}

.demo-card strong {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 11px;
  font-weight: 500;
  color: #567351;
}

.demo-card button {
  padding: 4px 0;
  border: 0;
  background: transparent;
  color: #4b7448;
  font-size: 11px;
  cursor: pointer;
}

.mode-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 18px;
  font-size: 10px;
  color: #8c9986;
}

.mode-switch button {
  padding: 2px 0;
  border: 0;
  background: transparent;
  color: #4f7c4b;
  font-size: 10px;
  cursor: pointer;
}

.login-foot {
  margin: 18px 0 0;
  text-align: center;
  font-size: 9px;
  color: #adb5a7;
  letter-spacing: 0.04em;
}
</style>
