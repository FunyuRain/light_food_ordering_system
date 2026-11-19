<template>
  <div class="checkout-page">
    <van-nav-bar title="确认订单" left-arrow safe-area-inset-top @click-left="router.back()" />

    <div v-if="loading" class="checkout-loading">
      <van-loading color="#4f7c4b" size="24" vertical>正在准备订单</van-loading>
    </div>

    <template v-else>
      <div class="checkout-intro">
        <p class="eyebrow">FINAL STEP</p>
        <h1>确认这一餐。</h1>
        <p>核对地址和菜品，下一份轻食很快就来。</p>
      </div>

      <section class="checkout-card address-card">
        <div class="card-title">
          <span>收货地址</span>
          <button v-if="addresses.length" type="button" @click="addressPicker = true">切换</button>
        </div>

        <div v-if="selectedAddress" class="selected-address">
          <div class="address-icon"><van-icon name="location" /></div>
          <div class="address-main">
            <strong>{{ selectedAddress.receiverName }} <small>{{ selectedAddress.receiverPhone }}</small></strong>
            <p>{{ selectedAddress.fullAddress }}</p>
            <span v-if="selectedAddress.label">{{ selectedAddress.label }}</span>
          </div>
        </div>

        <button v-else class="add-address" type="button" @click="goAddress">
          <van-icon name="plus" />
          添加收货地址
        </button>
      </section>

      <section class="checkout-card">
        <div class="card-title">
          <span>菜品明细</span>
          <span class="muted">{{ cart.totalQuantity }} 件</span>
        </div>
        <div class="checkout-items">
          <div v-for="item in cart.items" :key="item.id" class="checkout-item">
            <div class="item-thumb">
              <img :src="item.dishImage || '/mock/dish/default.svg'" :alt="item.dishName" @error="onImageError" />
            </div>
            <div class="item-info">
              <strong>{{ item.dishName }}</strong>
              <span>{{ money(item.price) }} × {{ item.quantity }}</span>
            </div>
            <span class="item-amount">{{ money(item.amount) }}</span>
          </div>
        </div>
      </section>

      <section class="checkout-card">
        <div class="card-title">
          <span>订单备注</span>
          <span class="muted">选填</span>
        </div>
        <van-field
          v-model="remark"
          type="textarea"
          rows="2"
          autosize
          maxlength="200"
          show-word-limit
          placeholder="口味偏好、餐具需求等"
          class="remark-field"
        />
      </section>

      <section class="checkout-card summary-card">
        <div class="summary-line">
          <span>商品金额</span>
          <strong>{{ money(cart.totalAmount) }}</strong>
        </div>
        <div class="summary-line">
          <span>配送费</span>
          <strong>免配送费</strong>
        </div>
        <div class="summary-total">
          <span>应付合计</span>
          <strong>{{ money(cart.totalAmount) }}</strong>
        </div>
      </section>

      <p class="checkout-tip">提交后将生成待付款订单，可在订单详情中完成模拟支付。</p>
    </template>

    <div v-if="!loading" class="checkout-actionbar">
      <div class="total-box">
        <span>合计</span>
        <strong>{{ money(cart.totalAmount) }}</strong>
      </div>
      <van-button
        round
        type="primary"
        class="submit-btn"
        :loading="submitting"
        :disabled="!cart.items.length || !selectedAddress || hasUnavailable"
        @click="submitOrder"
      >
        提交订单
      </van-button>
    </div>

    <van-popup v-model:show="addressPicker" position="bottom" round class="address-picker">
      <div class="picker-head">
        <div>
          <p class="eyebrow">DELIVERY TO</p>
          <h2>选择收货地址</h2>
        </div>
        <button type="button" @click="goAddress">新增</button>
      </div>
      <div class="picker-list">
        <button
          v-for="address in addresses"
          :key="address.id"
          type="button"
          class="picker-item"
          :class="{ active: address.id === selectedId }"
          @click="pickAddress(address)"
        >
          <span class="radio-dot"></span>
          <span class="picker-main">
            <strong>{{ address.receiverName }} <small>{{ address.receiverPhone }}</small></strong>
            <span>{{ address.fullAddress }}</span>
          </span>
          <van-tag v-if="address.isDefault === 1" color="#e8f1e2" text-color="#4f7c4b" round>默认</van-tag>
        </button>
      </div>
      <div class="picker-foot">
        <van-button block round plain type="primary" @click="goAddress">管理地址</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { getCart } from '@/api/cart'
import { getAddresses } from '@/api/address'
import { createOrder } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { money } from '@/utils/format'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(true)
const submitting = ref(false)
const addressPicker = ref(false)
const addresses = ref([])
const selectedId = ref(null)
const remark = ref('')
const cart = reactive({
  items: [],
  totalQuantity: 0,
  totalAmount: 0
})

const selectedAddress = computed(() => addresses.value.find((item) => item.id === selectedId.value) || null)
const hasUnavailable = computed(() => cart.items.some((item) => item.status !== 1))

function applyCart(data) {
  cart.items = data?.items || []
  cart.totalQuantity = data?.totalQuantity || 0
  cart.totalAmount = data?.totalAmount || 0
}

async function load() {
  loading.value = true
  try {
    const [cartData, addressData] = await Promise.all([getCart(), getAddresses()])
    applyCart(cartData)
    addresses.value = addressData || []
    const preferred = addresses.value.find((item) => item.isDefault === 1) || addresses.value[0]
    selectedId.value = preferred?.id || null
  } finally {
    loading.value = false
  }
}

function pickAddress(address) {
  selectedId.value = address.id
  addressPicker.value = false
}

function goAddress() {
  addressPicker.value = false
  router.push('/address?redirect=/checkout')
}

async function submitOrder() {
  if (!selectedAddress.value || submitting.value) return
  submitting.value = true
  try {
    const order = await createOrder({
      addressId: selectedAddress.value.id,
      remark: remark.value || null
    })
    cartStore.reset()
    showSuccessToast('下单成功，请完成支付')
    router.replace(`/orders/${order.id}`)
  } finally {
    submitting.value = false
  }
}

function onImageError(event) {
  event.target.onerror = null
  event.target.src = '/mock/dish/default.svg'
}

onMounted(load)
</script>

<style scoped>
.checkout-page {
  min-height: 100vh;
  padding-bottom: 118px;
  background:
    radial-gradient(circle at 96% 1%, rgba(225, 235, 211, 0.7), transparent 27%),
    #f8f7f2;
}

.checkout-page :deep(.van-nav-bar) {
  background: transparent;
}

.checkout-page :deep(.van-nav-bar__title) {
  font-size: 14px;
  font-weight: 600;
  color: #2b422e;
}

.checkout-loading {
  display: grid;
  place-items: center;
  min-height: 50vh;
}

.checkout-intro {
  padding: 20px 20px 17px;
}

.eyebrow {
  margin: 0 0 7px;
  font-size: 8px;
  letter-spacing: 0.22em;
  color: #a0aa99;
}

.checkout-intro h1 {
  margin: 0;
  font-size: 25px;
  font-weight: 500;
  color: #263e29;
}

.checkout-intro > p:last-child {
  margin: 8px 0 0;
  font-size: 11px;
  line-height: 1.7;
  color: #96a18f;
}

.checkout-card {
  margin: 0 16px 12px;
  padding: 17px;
  border: 1px solid rgba(65, 91, 61, 0.07);
  border-radius: 19px;
  background: #fffdfa;
  box-shadow: 0 8px 22px rgba(49, 70, 45, 0.03);
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 13px;
  font-size: 12px;
  font-weight: 600;
  color: #324a35;
}

.card-title button {
  padding: 0;
  border: 0;
  background: transparent;
  color: #5f8657;
  font-size: 10px;
  cursor: pointer;
}

.card-title .muted {
  font-size: 9px;
  font-weight: 400;
  color: #a3ad9d;
}

.selected-address {
  display: flex;
  gap: 11px;
}

.address-icon {
  width: 31px;
  height: 31px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: #edf4e7;
  color: #5e8054;
}

.address-main {
  min-width: 0;
}

.address-main strong {
  font-size: 13px;
  color: #2f4632;
}

.address-main small {
  margin-left: 7px;
  font-size: 10px;
  font-weight: 400;
  color: #96a18f;
}

.address-main p {
  margin: 7px 0 0;
  font-size: 11px;
  line-height: 1.7;
  color: #7d8a76;
}

.address-main > span {
  display: inline-block;
  margin-top: 7px;
  padding: 2px 7px;
  border-radius: 5px;
  background: #f5f4ed;
  font-size: 8px;
  color: #a2a99a;
}

.add-address {
  width: 100%;
  padding: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  border: 1px dashed #cfdbc8;
  border-radius: 13px;
  background: #fafcf7;
  color: #62815a;
  font-size: 11px;
  cursor: pointer;
}

.checkout-items {
  border-top: 1px solid #f1f3ee;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 0;
  border-bottom: 1px solid #f3f4f0;
}

.checkout-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.item-thumb {
  width: 44px;
  height: 38px;
  flex: 0 0 auto;
  overflow: hidden;
  border-radius: 9px;
  background: #edf3e8;
}

.item-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-info strong {
  font-size: 11px;
  color: #374d38;
}

.item-info span {
  font-size: 9px;
  color: #9ba695;
}

.item-amount {
  font-size: 11px;
  color: #4b6648;
}

.remark-field {
  padding: 0 !important;
  background: #f8faf5 !important;
  border-radius: 12px;
}

.remark-field :deep(.van-field__control) {
  padding: 10px;
  font-size: 11px;
  line-height: 1.7;
}

.remark-field :deep(.van-field__word-limit) {
  padding-right: 8px;
  padding-bottom: 4px;
  font-size: 8px;
}

.summary-card {
  padding-bottom: 13px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 10px;
  color: #8f9b89;
}

.summary-line strong {
  font-weight: 500;
  color: #6d7e68;
}

.summary-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f2ed;
  font-size: 11px;
  color: #4f614c;
}

.summary-total strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 24px;
  font-weight: 400;
  color: #355a37;
}

.checkout-tip {
  margin: 4px 22px 0;
  text-align: center;
  font-size: 9px;
  line-height: 1.7;
  color: #a7b0a1;
}

.checkout-actionbar {
  position: fixed;
  left: 50%;
  bottom: 0;
  z-index: 10;
  width: min(100%, 520px);
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 13px;
  padding: 12px 17px calc(12px + env(safe-area-inset-bottom));
  background: rgba(253, 252, 247, 0.96);
  border-top: 1px solid rgba(66, 92, 62, 0.08);
  backdrop-filter: blur(14px);
}

.total-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.total-box span {
  font-size: 9px;
  color: #99a493;
}

.total-box strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 24px;
  font-weight: 400;
  color: #355a37;
}

.submit-btn {
  min-width: 128px;
  height: 44px;
  border: 0;
  font-size: 13px;
  letter-spacing: 0.06em;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
}

.address-picker {
  background: #f8f7f2;
  padding-bottom: env(safe-area-inset-bottom);
  max-height: 72vh;
  display: flex;
  flex-direction: column;
}

.picker-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px 22px 14px;
}

.picker-head h2 {
  margin: 0;
  font-size: 21px;
  font-weight: 500;
  color: #263e29;
}

.picker-head button {
  padding: 3px 0;
  border: 0;
  background: transparent;
  color: #5f8657;
  font-size: 10px;
  cursor: pointer;
}

.picker-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.picker-item {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 9px;
  padding: 14px;
  border: 1px solid #e9ede4;
  border-radius: 15px;
  background: #fffdfa;
  text-align: left;
  cursor: pointer;
}

.picker-item.active {
  border-color: #89a77e;
  background: #f5f9f1;
}

.radio-dot {
  width: 13px;
  height: 13px;
  flex: 0 0 auto;
  margin-top: 3px;
  border: 1px solid #c5d0be;
  border-radius: 50%;
  background: #fff;
}

.picker-item.active .radio-dot {
  border: 4px solid #5d8655;
}

.picker-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.picker-main strong {
  font-size: 12px;
  color: #334b35;
}

.picker-main small {
  margin-left: 5px;
  font-size: 9px;
  font-weight: 400;
  color: #97a392;
}

.picker-main > span {
  font-size: 10px;
  line-height: 1.6;
  color: #87927f;
}

.picker-foot {
  padding: 10px 18px 18px;
}
</style>
