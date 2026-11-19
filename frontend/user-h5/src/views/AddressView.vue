<template>
  <div class="address-page">
    <van-nav-bar
      title="收货地址"
      right-text="新增"
      safe-area-inset-top
      @click-right="openForm"
    />

    <div class="address-intro">
      <p class="eyebrow">DELIVERY DETAILS</p>
      <h1>送到哪里？</h1>
      <p>保存常用地址，下单时更快一步。</p>
    </div>

    <div v-if="loading" class="loading-wrap">
      <van-loading color="#4f7c4b" size="24" vertical>加载地址中</van-loading>
    </div>

    <div v-else-if="addresses.length" class="address-list">
      <article
        v-for="address in addresses"
        :key="address.id"
        class="address-card"
        :class="{ default: address.isDefault === 1 }"
      >
        <div class="card-top">
          <div class="receiver">
            <span class="pin"><van-icon name="location" /></span>
            <strong>{{ address.receiverName }}</strong>
            <span class="phone">{{ address.receiverPhone }}</span>
          </div>
          <span v-if="address.isDefault === 1" class="default-tag">默认</span>
        </div>
        <p class="full-address">{{ address.fullAddress }}</p>
        <div class="card-foot">
          <span class="label">{{ address.label || '常用地址' }}</span>
          <div class="actions">
            <button
              v-if="address.isDefault !== 1"
              type="button"
              @click="setDefault(address)"
            >
              设为默认
            </button>
            <button type="button" class="delete" @click="remove(address)">删除</button>
          </div>
        </div>
      </article>
    </div>

    <van-empty v-else description="还没有收货地址" image="search">
      <van-button round type="primary" class="empty-btn" @click="openForm">新增收货地址</van-button>
    </van-empty>

    <div class="account-section">
      <div class="account-row">
        <span class="account-avatar">{{ initial }}</span>
        <div>
          <strong>{{ auth.displayName }}</strong>
          <p>轻食会员 · 认真吃饭</p>
        </div>
      </div>
      <button class="logout-button" type="button" @click="logout">退出登录</button>
    </div>

    <van-popup
      v-model:show="formVisible"
      position="bottom"
      round
      closeable
      class="address-popup"
      :style="{ height: '86%' }"
    >
      <div class="popup-head">
        <p class="eyebrow">NEW ADDRESS</p>
        <h2>新增收货地址</h2>
        <p>填写后可以直接用于下单。</p>
      </div>

      <van-form class="address-form" @submit="submit">
        <van-cell-group inset>
          <van-field
            v-model="form.receiverName"
            name="receiverName"
            label="收货人"
            placeholder="请输入姓名"
            :rules="[{ required: true, message: '请输入收货人' }]"
          />
          <van-field
            v-model="form.receiverPhone"
            name="receiverPhone"
            type="tel"
            label="手机号"
            placeholder="请输入手机号"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^[0-9+\- ]{6,20}$/, message: '手机号格式不正确' }
            ]"
          />
          <van-field
            v-model="form.province"
            name="province"
            label="省份"
            placeholder="例如：上海市"
            :rules="[{ required: true, message: '请输入省份' }]"
          />
          <van-field
            v-model="form.city"
            name="city"
            label="城市"
            placeholder="例如：上海市"
            :rules="[{ required: true, message: '请输入城市' }]"
          />
          <van-field
            v-model="form.district"
            name="district"
            label="区县"
            placeholder="例如：徐汇区"
            :rules="[{ required: true, message: '请输入区县' }]"
          />
          <van-field
            v-model="form.detailAddress"
            name="detailAddress"
            label="详细地址"
            type="textarea"
            rows="2"
            autosize
            placeholder="街道、门牌号、楼层房间号"
            :rules="[{ required: true, message: '请输入详细地址' }]"
          />
          <van-field v-model="form.label" name="label" label="标签" placeholder="家 / 公司 / 学校" maxlength="20" />
          <van-cell title="设为默认地址" center>
            <template #right-icon>
              <van-switch v-model="form.isDefault" size="20" active-color="#4f7c4b" />
            </template>
          </van-cell>
        </van-cell-group>

        <div class="popup-actions">
          <van-button round block type="primary" native-type="submit" :loading="submitting" class="save-btn">
            保存地址
          </van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { createAddress, deleteAddress, getAddresses, setDefaultAddress } from '@/api/address'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const cart = useCartStore()

const addresses = ref([])
const loading = ref(true)
const formVisible = ref(false)
const submitting = ref(false)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  label: '',
  isDefault: false
})

const initial = computed(() => (auth.displayName || '轻').slice(0, 1))

async function load() {
  loading.value = true
  try {
    addresses.value = await getAddresses()
  } finally {
    loading.value = false
  }
}

function openForm() {
  Object.assign(form, {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    label: '',
    isDefault: addresses.value.length === 0 || typeof route.query.redirect === 'string'
  })
  formVisible.value = true
}

async function submit() {
  submitting.value = true
  try {
    await createAddress({
      ...form,
      label: form.label || null
    })
    formVisible.value = false
    showSuccessToast('地址已保存')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (redirect) {
      router.replace(redirect)
      return
    }
    await load()
  } finally {
    submitting.value = false
  }
}

async function setDefault(address) {
  await setDefaultAddress(address.id)
  showSuccessToast('已设为默认地址')
  await load()
}

async function remove(address) {
  try {
    await showConfirmDialog({
      title: '删除地址',
      message: `确定删除「${address.receiverName}」的收货地址吗？`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteAddress(address.id)
  showSuccessToast('地址已删除')
  await load()
}

async function logout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定退出当前账号吗？',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  auth.logout()
  cart.reset()
  router.replace('/login')
}

onMounted(load)
</script>

<style scoped>
.address-page {
  min-height: 100vh;
  padding-bottom: 32px;
  background:
    radial-gradient(circle at 96% 2%, rgba(224, 234, 210, 0.72), transparent 25%),
    #f8f7f2;
}

.address-page :deep(.van-nav-bar) {
  background: transparent;
}

.address-page :deep(.van-nav-bar__title) {
  font-size: 14px;
  font-weight: 600;
  color: #2b422e;
}

.address-page :deep(.van-nav-bar__text) {
  color: #4f7c4b;
  font-size: 11px;
}

.address-intro {
  padding: 22px 20px 16px;
}

.eyebrow {
  margin: 0 0 7px;
  font-size: 8px;
  letter-spacing: 0.22em;
  color: #a0aa99;
}

.address-intro h1 {
  margin: 0;
  font-size: 25px;
  font-weight: 500;
  color: #263e29;
}

.address-intro > p:last-child {
  margin: 8px 0 0;
  font-size: 11px;
  color: #96a18f;
}

.loading-wrap {
  display: grid;
  place-items: center;
  min-height: 32vh;
}

.address-list {
  padding: 0 16px;
}

.address-card {
  margin-bottom: 11px;
  padding: 16px;
  border: 1px solid rgba(65, 92, 59, 0.08);
  border-radius: 19px;
  background: #fffdfa;
  box-shadow: 0 8px 22px rgba(49, 70, 45, 0.03);
}

.address-card.default {
  border-color: rgba(79, 124, 75, 0.25);
  background:
    linear-gradient(135deg, rgba(240, 246, 234, 0.8), transparent 55%),
    #fffdfa;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.receiver {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
}

.pin {
  width: 27px;
  height: 27px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  border-radius: 9px;
  background: #edf4e7;
  color: #5e8054;
  font-size: 14px;
}

.receiver strong {
  font-size: 13px;
  color: #2d442f;
}

.phone {
  font-size: 10px;
  color: #98a391;
}

.default-tag {
  flex: 0 0 auto;
  padding: 3px 8px;
  border-radius: 999px;
  background: #eaf3e3;
  color: #557b4d;
  font-size: 9px;
}

.full-address {
  margin: 12px 0 0 34px;
  font-size: 11px;
  line-height: 1.7;
  color: #7e8b78;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 13px 0 0 34px;
}

.label {
  padding: 2px 7px;
  border-radius: 5px;
  background: #f5f4ed;
  font-size: 8px;
  color: #a2a99a;
}

.actions {
  display: flex;
  gap: 13px;
}

.actions button {
  padding: 0;
  border: 0;
  background: transparent;
  font-size: 10px;
  color: #698b61;
  cursor: pointer;
}

.actions button.delete {
  color: #b88b7e;
}

.account-section {
  margin: 28px 16px 0;
  padding: 16px;
  border-radius: 19px;
  background: rgba(255, 253, 250, 0.6);
  border: 1px solid rgba(65, 92, 59, 0.06);
}

.account-row {
  display: flex;
  align-items: center;
  gap: 11px;
}

.account-avatar {
  width: 37px;
  height: 37px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(145deg, #d6e8c5, #93b781);
  color: #36553a;
  font-size: 13px;
  font-weight: 600;
}

.account-row strong {
  font-size: 12px;
  color: #3b513c;
}

.account-row p {
  margin: 4px 0 0;
  font-size: 9px;
  color: #a3ad9d;
}

.logout-button {
  width: 100%;
  margin-top: 15px;
  padding: 10px;
  border: 1px solid #eceee7;
  border-radius: 12px;
  background: transparent;
  color: #9b7d70;
  font-size: 11px;
  cursor: pointer;
}

.empty-btn {
  min-width: 132px;
  height: 40px;
  border: 0;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
}

.address-popup {
  background: #f8f7f2;
}

.popup-head {
  padding: 28px 24px 18px;
}

.popup-head h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
  color: #263e29;
}

.popup-head > p:last-child {
  margin: 7px 0 0;
  font-size: 10px;
  color: #99a491;
}

.address-form :deep(.van-cell-group--inset) {
  margin: 0 16px;
  border-radius: 16px;
  overflow: hidden;
  background: #fffdfa;
}

.address-form :deep(.van-field) {
  padding: 12px 15px;
  background: #fffdfa;
}

.address-form :deep(.van-field__label) {
  width: 70px;
  font-size: 12px;
  color: #63745e;
}

.address-form :deep(.van-field__control) {
  font-size: 12px;
  color: #354d37;
}

.address-form :deep(.van-cell) {
  background: #fffdfa;
}

.address-form :deep(.van-cell__title) {
  font-size: 12px;
  color: #63745e;
}

.popup-actions {
  padding: 22px 20px calc(22px + env(safe-area-inset-bottom));
}

.save-btn {
  height: 46px;
  border: 0;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
  font-size: 13px;
  letter-spacing: 0.06em;
}

:deep(.van-empty) {
  padding: 44px 0;
}

:deep(.van-empty__description) {
  font-size: 11px;
  color: #98a491;
}
</style>
