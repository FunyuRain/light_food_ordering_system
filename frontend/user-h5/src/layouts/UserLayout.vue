<template>
  <div class="user-frame">
    <router-view />
    <van-tabbar
      v-if="showTabbar"
      route
      fixed
      placeholder
      safe-area-inset-bottom
      class="app-tabbar"
      active-color="#3f6c40"
      inactive-color="#9aa696"
    >
      <van-tabbar-item replace to="/menu" icon="shop-o">点餐</van-tabbar-item>
      <van-tabbar-item replace to="/cart" icon="cart-o" :badge="cart.count || ''">购物车</van-tabbar-item>
      <van-tabbar-item replace to="/orders" icon="orders-o">订单</van-tabbar-item>
      <van-tabbar-item replace to="/address" icon="location-o">地址</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const cart = useCartStore()
const showTabbar = computed(() => Boolean(route.meta.tabbar))

function refreshCart() {
  if (route.meta.tabbar || route.name === 'order-detail' || route.name === 'dish-detail') {
    cart.refresh()
  }
}

onMounted(refreshCart)
watch(() => route.fullPath, refreshCart)
</script>

<style scoped>
.user-frame {
  position: relative;
  width: min(100%, 520px);
  min-height: 100vh;
  margin: 0 auto;
  background: #f8f7f2;
  box-shadow: 0 0 60px rgba(35, 55, 35, 0.1);
}

.app-tabbar {
  left: 0;
  right: 0;
  width: min(100%, 520px);
  margin: 0 auto;
  border-top: 1px solid rgba(71, 96, 67, 0.08);
  background: rgba(252, 251, 247, 0.96);
  backdrop-filter: blur(14px);
}

.app-tabbar :deep(.van-tabbar-item__text) {
  font-size: 10px;
  letter-spacing: 0.04em;
}

.app-tabbar :deep(.van-tabbar-item__icon) {
  font-size: 20px;
}

.app-tabbar :deep(.van-badge) {
  background: #c9856f;
  border-color: #fcfbf7;
}
</style>
