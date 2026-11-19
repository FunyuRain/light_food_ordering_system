<template>
  <div class="dish-page">
    <van-nav-bar title="菜品详情" left-arrow safe-area-inset-top @click-left="router.back()" />

    <div v-if="loading" class="detail-loading">
      <van-loading color="#4f7c4b" size="24" vertical>加载菜品中</van-loading>
    </div>

    <template v-else-if="dish">
      <div class="dish-visual">
        <img :src="dish.imageUrl || '/mock/dish/default.svg'" :alt="dish.name" @error="onImageError" />
        <span class="visual-tag">{{ dish.categoryName }}</span>
        <div class="visual-glow"></div>
      </div>

      <main class="dish-content">
        <p class="eyebrow">FRESH PLATE · DAILY MENU</p>
        <div class="title-row">
          <h1>{{ dish.name }}</h1>
          <span class="price">{{ money(dish.price) }}</span>
        </div>
        <p class="description">{{ dish.description || '新鲜食材，简单搭配，给今天一份轻盈。' }}</p>

        <div class="feature-row">
          <span><i></i> 现点现做</span>
          <span><i></i> 低负担</span>
          <span><i></i> 每日新鲜</span>
        </div>

        <section class="note-card">
          <div class="note-mark"><van-icon name="like-o" /></div>
          <div>
            <strong>轻食小提示</strong>
            <p>菜品图片为模拟路径；实际餐食以门店当日食材和出餐为准。下单后可在订单详情完成模拟支付。</p>
          </div>
        </section>
      </main>

      <div class="dish-actions">
        <button class="cart-shortcut" type="button" @click="router.push('/cart')">
          <van-icon name="cart-o" />
          <span>购物车</span>
          <i v-if="cart.count" class="cart-badge">{{ cart.count }}</i>
        </button>
        <van-button round type="primary" class="add-button" :loading="adding" @click="add">
          加入购物车
        </van-button>
      </div>
    </template>

    <van-empty v-else description="菜品不存在或已下架">
      <van-button round size="small" type="primary" @click="router.replace('/menu')">返回菜单</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { addToCart } from '@/api/cart'
import { getDish } from '@/api/menu'
import { useCartStore } from '@/stores/cart'
import { money } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()

const loading = ref(true)
const adding = ref(false)
const dish = ref(null)

async function load() {
  loading.value = true
  try {
    dish.value = await getDish(route.params.id)
  } catch {
    dish.value = null
  } finally {
    loading.value = false
  }
}

async function add() {
  if (!dish.value || adding.value) return
  adding.value = true
  try {
    const data = await addToCart(dish.value.id, 1)
    cart.setFromCart(data)
    showSuccessToast('已加入购物车')
  } finally {
    adding.value = false
  }
}

function onImageError(event) {
  event.target.onerror = null
  event.target.src = '/mock/dish/default.svg'
}

onMounted(load)
</script>

<style scoped>
.dish-page {
  min-height: 100vh;
  padding-bottom: 96px;
  background:
    radial-gradient(circle at 95% 0%, rgba(224, 235, 209, 0.7), transparent 27%),
    #f8f7f2;
}

.dish-page :deep(.van-nav-bar) {
  background: transparent;
}

.dish-page :deep(.van-nav-bar__title) {
  font-size: 14px;
  font-weight: 600;
  color: #2b422e;
}

.detail-loading {
  display: grid;
  place-items: center;
  min-height: 55vh;
}

.dish-visual {
  position: relative;
  height: 300px;
  margin: 4px 16px 0;
  overflow: hidden;
  border-radius: 25px;
  background: #eaf1e4;
}

.dish-visual img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.visual-tag {
  position: absolute;
  left: 14px;
  top: 14px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(250, 253, 246, 0.88);
  color: #4f7149;
  font-size: 9px;
  letter-spacing: 0.05em;
  backdrop-filter: blur(6px);
}

.visual-glow {
  position: absolute;
  inset: auto 0 0;
  height: 40%;
  background: linear-gradient(transparent, rgba(30, 50, 31, 0.08));
  pointer-events: none;
}

.dish-content {
  padding: 24px 23px 12px;
}

.eyebrow {
  margin: 0 0 9px;
  font-size: 8px;
  letter-spacing: 0.22em;
  color: #9caa95;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.title-row h1 {
  margin: 0;
  font-size: 27px;
  line-height: 1.35;
  font-weight: 500;
  color: #263e29;
}

.price {
  flex: 0 0 auto;
  padding-top: 4px;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 25px;
  color: #406640;
}

.description {
  margin: 15px 0 0;
  font-size: 12px;
  line-height: 1.9;
  color: #7f8d79;
}

.feature-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 18px;
}

.feature-row span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f0f5eb;
  color: #6e8568;
  font-size: 9px;
}

.feature-row i {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #91b27f;
}

.note-card {
  display: flex;
  gap: 11px;
  margin-top: 22px;
  padding: 15px;
  border: 1px solid rgba(78, 108, 70, 0.07);
  border-radius: 16px;
  background: #fffdfa;
}

.note-mark {
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: #eef4e7;
  color: #64845a;
  font-size: 15px;
}

.note-card strong {
  font-size: 11px;
  color: #405640;
}

.note-card p {
  margin: 5px 0 0;
  font-size: 9px;
  line-height: 1.8;
  color: #98a391;
}

.dish-actions {
  position: fixed;
  left: 50%;
  bottom: 0;
  z-index: 10;
  width: min(100%, 520px);
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 16px calc(11px + env(safe-area-inset-bottom));
  background: rgba(253, 252, 247, 0.96);
  border-top: 1px solid rgba(66, 92, 62, 0.08);
  backdrop-filter: blur(14px);
}

.cart-shortcut {
  position: relative;
  flex: 0 0 auto;
  width: 62px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 4px 0;
  border: 0;
  background: transparent;
  color: #73836d;
  font-size: 9px;
  cursor: pointer;
}

.cart-shortcut .van-icon {
  font-size: 16px;
}

.cart-badge {
  position: absolute;
  top: -1px;
  right: 9px;
  min-width: 14px;
  padding: 1px 4px;
  border-radius: 999px;
  background: #c9856f;
  color: white;
  font-size: 8px;
  font-style: normal;
  line-height: 1.4;
}

.add-button {
  flex: 1;
  height: 45px;
  border: 0;
  font-size: 13px;
  letter-spacing: 0.06em;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
}

:deep(.van-empty) {
  padding: 50px 0;
}
</style>
