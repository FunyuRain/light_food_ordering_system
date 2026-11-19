<template>
  <div class="cart-page">
    <van-nav-bar
      title="购物车"
      :right-text="cart.items.length ? '清空' : ''"
      safe-area-inset-top
      @click-right="clearAll"
    />

    <div v-if="loading" class="cart-loading">
      <van-loading color="#4f7c4b" size="24" vertical>正在整理购物车</van-loading>
    </div>

    <template v-else-if="cart.items.length">
      <div class="cart-intro">
        <p class="eyebrow">YOUR PICKS</p>
        <div class="intro-row">
          <h1>已选 {{ cart.totalQuantity }} 件</h1>
          <span>轻食正在等你</span>
        </div>
      </div>

      <div class="cart-list">
        <article v-for="item in cart.items" :key="item.id" class="cart-item">
          <div class="item-image">
            <img :src="item.dishImage || '/mock/dish/default.svg'" :alt="item.dishName" @error="onImageError" />
          </div>
          <div class="item-main">
            <div class="item-head">
              <h3>{{ item.dishName }}</h3>
              <button type="button" class="remove-btn" aria-label="移除" @click="remove(item)">
                <van-icon name="delete-o" />
              </button>
            </div>
            <div class="item-meta">
              <span>{{ money(item.price) }}</span>
              <span v-if="item.status !== 1" class="off-shelf">已下架</span>
            </div>
            <div class="item-foot">
              <span class="item-amount">{{ money(item.amount) }}</span>
              <van-stepper
                v-model="item.quantity"
                :min="1"
                :max="99"
                integer
                button-size="24"
                input-width="30"
                @change="(value) => changeQuantity(item, value)"
              />
            </div>
          </div>
        </article>
      </div>

      <div class="cart-note">
        <van-icon name="info-o" />
        <span>所有菜品按当前售价结算，下单时以服务端价格为准。</span>
      </div>

      <div class="cart-actionbar">
        <div class="total-box">
          <span>合计</span>
          <strong>{{ money(cart.totalAmount) }}</strong>
        </div>
        <van-button
          round
          type="primary"
          class="checkout-btn"
          :disabled="hasUnavailable"
          @click="goCheckout"
        >
          去结算
        </van-button>
      </div>
      <p v-if="hasUnavailable" class="unavailable-tip">请先移除已下架菜品，再继续结算。</p>
    </template>

    <van-empty v-else description="购物车还是空的，去挑一份轻食吧" image="search">
      <van-button round type="primary" class="empty-btn" @click="router.replace('/menu')">去点餐</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { clearCart, getCart, removeCartItem, updateCartQuantity } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { money } from '@/utils/format'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(true)
const cart = reactive({
  items: [],
  totalQuantity: 0,
  totalAmount: 0
})

const hasUnavailable = computed(() => cart.items.some((item) => item.status !== 1))

function applyCart(data) {
  cart.items = data?.items || []
  cart.totalQuantity = data?.totalQuantity || 0
  cart.totalAmount = data?.totalAmount || 0
  cartStore.setFromCart(data)
}

async function load() {
  loading.value = true
  try {
    applyCart(await getCart())
  } finally {
    loading.value = false
  }
}

async function changeQuantity(item, value) {
  if (!value || value < 1) return
  try {
    applyCart(await updateCartQuantity(item.id, value))
  } catch {
    await load()
  }
}

async function remove(item) {
  try {
    await showConfirmDialog({
      title: '移除菜品',
      message: `要从购物车移除「${item.dishName}」吗？`,
      confirmButtonText: '移除',
      cancelButtonText: '保留'
    })
  } catch {
    return
  }
  applyCart(await removeCartItem(item.id))
  showSuccessToast('已移除')
}

async function clearAll() {
  try {
    await showConfirmDialog({
      title: '清空购物车',
      message: '确定清空当前购物车吗？',
      confirmButtonText: '清空',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  applyCart(await clearCart())
  showSuccessToast('购物车已清空')
}

function goCheckout() {
  if (hasUnavailable.value) return
  router.push('/checkout')
}

function onImageError(event) {
  event.target.onerror = null
  event.target.src = '/mock/dish/default.svg'
}

onMounted(load)
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  padding-bottom: 28px;
  background:
    radial-gradient(circle at 100% 0%, rgba(224, 233, 211, 0.7), transparent 28%),
    #f8f7f2;
}

.cart-page :deep(.van-nav-bar) {
  background: transparent;
}

.cart-page :deep(.van-nav-bar__title) {
  font-size: 14px;
  font-weight: 600;
  color: #2b422e;
}

.cart-page :deep(.van-nav-bar__text) {
  color: #8b9a85;
  font-size: 11px;
}

.cart-loading {
  display: grid;
  place-items: center;
  min-height: 45vh;
}

.cart-intro {
  padding: 20px 20px 13px;
}

.eyebrow {
  margin: 0 0 7px;
  font-size: 8px;
  letter-spacing: 0.22em;
  color: #a0aa99;
}

.intro-row {
  display: flex;
  align-items: baseline;
  gap: 9px;
}

.intro-row h1 {
  margin: 0;
  font-size: 23px;
  font-weight: 500;
  color: #263e29;
}

.intro-row span {
  font-size: 10px;
  color: #a0aa9a;
}

.cart-list {
  padding: 0 16px;
}

.cart-item {
  display: flex;
  gap: 12px;
  margin-bottom: 11px;
  padding: 12px;
  border: 1px solid rgba(65, 91, 61, 0.07);
  border-radius: 19px;
  background: #fffdfa;
  box-shadow: 0 8px 20px rgba(49, 70, 45, 0.03);
}

.item-image {
  width: 82px;
  height: 82px;
  flex: 0 0 auto;
  overflow: hidden;
  border-radius: 14px;
  background: #edf3e8;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.item-head {
  display: flex;
  justify-content: space-between;
  gap: 6px;
}

.item-head h3 {
  margin: 1px 0 0;
  font-size: 13px;
  line-height: 1.4;
  font-weight: 600;
  color: #2c422f;
}

.remove-btn {
  flex: 0 0 auto;
  padding: 2px;
  border: 0;
  background: transparent;
  color: #b4bcae;
  font-size: 15px;
  cursor: pointer;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 5px;
  font-size: 10px;
  color: #8b9985;
}

.off-shelf {
  padding: 2px 6px;
  border-radius: 999px;
  background: #fbefec;
  color: #b77a6b;
  font-size: 8px;
}

.item-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 8px;
}

.item-amount {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 17px;
  color: #3e633f;
}

.item-foot :deep(.van-stepper__button) {
  border-color: #edf0e8;
  background: #f6f8f2;
}

.item-foot :deep(.van-stepper__input) {
  background: transparent;
  color: #4d604b;
  font-size: 11px;
}

.cart-note {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  margin: 15px 20px 0;
  font-size: 9px;
  line-height: 1.6;
  color: #a4aea0;
}

.cart-note .van-icon {
  margin-top: 1px;
  color: #b1bca9;
}

.cart-actionbar {
  position: sticky;
  bottom: 58px;
  z-index: 6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 22px 16px 0;
  padding: 12px 13px 12px 18px;
  border: 1px solid rgba(66, 92, 62, 0.09);
  border-radius: 20px;
  background: rgba(253, 252, 247, 0.95);
  backdrop-filter: blur(14px);
  box-shadow: 0 12px 30px rgba(48, 69, 44, 0.1);
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

.checkout-btn {
  min-width: 122px;
  height: 43px;
  border: 0;
  font-size: 13px;
  letter-spacing: 0.06em;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
}

.unavailable-tip {
  margin: 9px 20px 0;
  text-align: center;
  font-size: 9px;
  color: #b07b6d;
}

.empty-btn {
  min-width: 120px;
  height: 40px;
  border: 0;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
}

:deep(.van-empty) {
  padding: 52px 0;
}

:deep(.van-empty__description) {
  font-size: 11px;
  color: #98a491;
}
</style>
