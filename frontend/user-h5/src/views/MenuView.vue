<template>
  <div class="menu-page">
    <header class="menu-hero">
      <div class="hero-nav">
        <div class="brand">
          <span class="brand-mark"><i></i></span>
          <span>LIGHT FOOD</span>
        </div>
        <button class="profile-chip" type="button" @click="router.push('/address')">
          <span class="avatar">{{ initial }}</span>
          <span>{{ auth.displayName }}</span>
        </button>
      </div>

      <div class="hero-copy">
        <p class="eyebrow">FRESH FOR TODAY · 2026</p>
        <h1>今天，吃点<br /><em>轻</em>的。</h1>
        <p>新鲜蔬菜、优质蛋白和刚刚好的饱腹感，认真对待每一餐。</p>
      </div>

      <div class="hero-badges">
        <span><i></i> 新鲜现做</span>
        <span><i></i> 低负担</span>
        <span><i></i> 每日限定</span>
      </div>
      <div class="hero-circle circle-one"></div>
      <div class="hero-circle circle-two"></div>
      <div class="hero-leaf"></div>
    </header>

    <main class="menu-body">
      <div class="menu-heading">
        <div>
          <p class="eyebrow">DAILY MENU</p>
          <h2>今日菜单</h2>
        </div>
        <span class="dish-count">{{ visibleDishes.length }} 道</span>
      </div>

      <div class="category-strip">
        <button
          type="button"
          :class="['category-chip', { active: activeCategory === null }]"
          @click="activeCategory = null"
        >
          全部
        </button>
        <button
          v-for="category in categories"
          :key="category.id"
          type="button"
          :class="['category-chip', { active: activeCategory === category.id }]"
          @click="activeCategory = category.id"
        >
          {{ category.name }}
        </button>
      </div>

      <section v-if="hotDishes.length" class="hot-section">
        <div class="hot-heading">
          <h2>本周人气</h2>
          <span>MOST LOVED</span>
        </div>
        <div class="hot-strip">
          <button
            v-for="dish in hotDishes.slice(0, 4)"
            :key="`hot-${dish.id}`"
            type="button"
            class="hot-card"
            @click="router.push(`/dishes/${dish.id}`)"
          >
            <img :src="dish.imageUrl || '/mock/dish/default.svg'" :alt="dish.name" @error="onImageError" />
            <span class="hot-copy">
              <strong>{{ dish.name }}</strong>
              <small>{{ money(dish.price) }}</small>
            </span>
          </button>
        </div>
      </section>

      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <div v-if="loading" class="skeleton-grid">
          <div v-for="item in 4" :key="item" class="skeleton-card">
            <div class="skeleton-image"></div>
            <div class="skeleton-line wide"></div>
            <div class="skeleton-line"></div>
          </div>
        </div>

        <div v-else-if="visibleDishes.length" class="dish-grid">
          <article
            v-for="dish in visibleDishes"
            :key="dish.id"
            class="dish-card"
            @click="router.push(`/dishes/${dish.id}`)"
          >
            <div class="dish-image">
              <img :src="dish.imageUrl || '/mock/dish/default.svg'" :alt="dish.name" @error="onImageError" />
              <span v-if="dish.sort <= 10" class="fresh-tag">推荐</span>
            </div>
            <div class="dish-body">
              <div class="dish-title">
                <h3>{{ dish.name }}</h3>
                <span class="dish-price">{{ money(dish.price) }}</span>
              </div>
              <p>{{ dish.description || '清爽搭配，刚刚好。' }}</p>
              <div class="dish-foot">
                <span class="category-label">{{ dish.categoryName }}</span>
                <button
                  class="add-button"
                  type="button"
                  :disabled="addingId === dish.id"
                  :aria-label="`加入购物车：${dish.name}`"
                  @click.stop="add(dish)"
                >
                  <van-loading v-if="addingId === dish.id" size="12" color="#fff" />
                  <van-icon v-else name="plus" />
                </button>
              </div>
            </div>
          </article>
        </div>

        <van-empty v-else description="这个分类暂时还没有上架菜品" image="search">
          <van-button round size="small" type="primary" @click="activeCategory = null">看看全部</van-button>
        </van-empty>
      </van-pull-refresh>

      <p class="menu-foot">认真吃饭，是今天最小单位的自律。</p>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { getCategories, getDishes, getHotDishes } from '@/api/menu'
import { addToCart } from '@/api/cart'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { money } from '@/utils/format'

const router = useRouter()
const auth = useAuthStore()
const cartStore = useCartStore()

const categories = ref([])
const dishes = ref([])
const hotDishes = ref([])
const activeCategory = ref(null)
const loading = ref(true)
const refreshing = ref(false)
const addingId = ref(null)

const initial = computed(() => (auth.displayName || '轻').slice(0, 1))
const visibleDishes = computed(() => {
  if (activeCategory.value === null) return dishes.value
  return dishes.value.filter((dish) => dish.categoryId === activeCategory.value)
})

async function fetchData() {
  const [categoryData, dishData, hotData] = await Promise.all([
    getCategories(),
    getDishes(),
    getHotDishes()
  ])
  categories.value = categoryData || []
  dishes.value = dishData || []
  hotDishes.value = hotData || []
  if (activeCategory.value !== null && !categories.value.some((item) => item.id === activeCategory.value)) {
    activeCategory.value = null
  }
}

async function onRefresh() {
  try {
    await fetchData()
    showSuccessToast('菜单已更新')
  } finally {
    refreshing.value = false
  }
}

async function add(dish) {
  if (addingId.value) return
  addingId.value = dish.id
  try {
    const data = await addToCart(dish.id, 1)
    cartStore.setFromCart(data)
    showSuccessToast('已加入购物车')
  } finally {
    addingId.value = null
  }
}

function onImageError(event) {
  event.target.onerror = null
  event.target.src = '/mock/dish/default.svg'
}

onMounted(async () => {
  try {
    await fetchData()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.menu-page {
  min-height: 100vh;
  padding-bottom: 24px;
}

.menu-hero {
  position: relative;
  overflow: hidden;
  padding: 24px 22px 32px;
  color: #f4f8ed;
  border-radius: 0 0 30px 30px;
  background:
    radial-gradient(circle at 82% 16%, rgba(195, 222, 164, 0.22), transparent 30%),
    linear-gradient(150deg, #223c29 0%, #31543a 62%, #27462f 100%);
}

.hero-nav {
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
  color: rgba(241, 249, 232, 0.8);
}

.brand-mark {
  width: 29px;
  height: 29px;
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

.profile-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 5px 9px 5px 5px;
  max-width: 140px;
  border: 1px solid rgba(226, 243, 212, 0.14);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(241, 249, 233, 0.8);
  font-size: 10px;
  cursor: pointer;
}

.profile-chip > span:last-child {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.avatar {
  width: 24px;
  height: 24px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #c9dfb3;
  color: #36523a;
  font-size: 10px;
  font-weight: 600;
}

.hero-copy {
  position: relative;
  z-index: 2;
  margin-top: 42px;
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 8px;
  letter-spacing: 0.24em;
  color: #b7d39e;
}

.hero-copy h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.16;
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
  background: rgba(190, 221, 158, 0.16);
}

.hero-copy > p:last-child {
  max-width: 290px;
  margin: 15px 0 0;
  font-size: 11px;
  line-height: 1.8;
  color: rgba(237, 248, 227, 0.58);
}

.hero-badges {
  position: relative;
  z-index: 2;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 24px;
}

.hero-badges span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 9px;
  border: 1px solid rgba(232, 245, 219, 0.1);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.045);
  font-size: 9px;
  color: rgba(235, 247, 224, 0.62);
}

.hero-badges i {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #b6d49a;
}

.hero-circle {
  position: absolute;
  border: 1px solid rgba(213, 236, 192, 0.1);
  border-radius: 50%;
}

.circle-one {
  width: 280px;
  height: 280px;
  right: -125px;
  top: 46px;
}

.circle-two {
  width: 165px;
  height: 165px;
  right: -62px;
  top: 104px;
  background: rgba(210, 236, 183, 0.035);
}

.hero-leaf {
  position: absolute;
  width: 50px;
  height: 74px;
  right: 40px;
  bottom: 34px;
  border-radius: 70% 8% 70% 8%;
  background: linear-gradient(145deg, rgba(198, 225, 169, 0.26), rgba(124, 166, 107, 0.08));
  transform: rotate(28deg);
}

.menu-body {
  padding: 23px 18px 20px;
}

.menu-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 4px;
}

.menu-heading .eyebrow {
  color: #9da995;
}

.menu-heading h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
  color: #253d28;
}

.dish-count {
  padding-bottom: 2px;
  font-size: 10px;
  color: #9ca795;
}

.category-strip {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  gap: 7px;
  overflow-x: auto;
  margin: 16px -18px 16px;
  padding: 10px 18px;
  background: rgba(248, 247, 242, 0.94);
  backdrop-filter: blur(12px);
  scrollbar-width: none;
}

.category-strip::-webkit-scrollbar {
  display: none;
}

.category-chip {
  flex: 0 0 auto;
  padding: 7px 14px;
  border: 1px solid #e6ebe0;
  border-radius: 999px;
  background: #fff;
  color: #7b8975;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-chip.active {
  border-color: #4f7c4b;
  background: #4f7c4b;
  color: #f4f8ed;
  box-shadow: 0 5px 14px rgba(68, 102, 61, 0.16);
}

.hot-section {
  margin: 2px 0 18px;
  padding: 15px 14px 13px;
  border-radius: 19px;
  background:
    radial-gradient(circle at 95% 10%, rgba(218, 234, 200, 0.9), transparent 36%),
    #f1f6eb;
}

.hot-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 11px;
}

.hot-heading h2 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #365338;
}

.hot-heading span {
  font-size: 7px;
  letter-spacing: 0.16em;
  color: #a3b09a;
}

.hot-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
  scrollbar-width: none;
}

.hot-strip::-webkit-scrollbar {
  display: none;
}

.hot-card {
  flex: 0 0 145px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px;
  border: 1px solid rgba(80, 110, 72, 0.08);
  border-radius: 13px;
  background: rgba(255, 253, 249, 0.86);
  text-align: left;
  cursor: pointer;
}

.hot-card img {
  width: 39px;
  height: 39px;
  flex: 0 0 auto;
  object-fit: cover;
  border-radius: 9px;
}

.hot-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hot-copy strong {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 10px;
  font-weight: 600;
  color: #3b513b;
}

.hot-copy small {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 11px;
  color: #52724d;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dish-card {
  overflow: hidden;
  border-radius: 19px;
  background: #fffdfa;
  border: 1px solid rgba(67, 91, 62, 0.07);
  box-shadow: 0 8px 24px rgba(51, 73, 47, 0.035);
  cursor: pointer;
}

.dish-image {
  position: relative;
  aspect-ratio: 1.22 / 1;
  overflow: hidden;
  background: #edf3e8;
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.dish-card:hover .dish-image img {
  transform: scale(1.035);
}

.fresh-tag {
  position: absolute;
  left: 9px;
  top: 9px;
  padding: 3px 7px;
  border-radius: 999px;
  background: rgba(248, 252, 243, 0.9);
  color: #55784d;
  font-size: 8px;
  letter-spacing: 0.06em;
  backdrop-filter: blur(5px);
}

.dish-body {
  padding: 12px 12px 11px;
}

.dish-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 6px;
}

.dish-title h3 {
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
  font-weight: 600;
  color: #2b422e;
}

.dish-price {
  flex: 0 0 auto;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 15px;
  color: #426a41;
}

.dish-body > p {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 30px;
  margin: 6px 0 10px;
  font-size: 10px;
  line-height: 1.55;
  color: #94a08f;
}

.dish-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.category-label {
  font-size: 9px;
  color: #a3ad9d;
}

.add-button {
  width: 29px;
  height: 29px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 50%;
  background: #4e7c4b;
  color: white;
  font-size: 15px;
  cursor: pointer;
  box-shadow: 0 6px 12px rgba(68, 105, 61, 0.18);
  transition: transform 0.18s ease, background 0.18s ease;
}

.add-button:active {
  transform: scale(0.92);
}

.add-button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.skeleton-card {
  padding-bottom: 14px;
  border-radius: 19px;
  background: #fffdfa;
  overflow: hidden;
}

.skeleton-image,
.skeleton-line {
  background: linear-gradient(90deg, #eef2e9 25%, #f6f8f2 50%, #eef2e9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-image {
  aspect-ratio: 1.22 / 1;
}

.skeleton-line {
  height: 9px;
  margin: 12px 12px 0;
  border-radius: 999px;
}

.skeleton-line.wide {
  width: 60%;
}

@keyframes shimmer {
  from {
    background-position: 200% 0;
  }
  to {
    background-position: -200% 0;
  }
}

.menu-foot {
  margin: 28px 0 6px;
  text-align: center;
  font-size: 9px;
  letter-spacing: 0.08em;
  color: #b0b9a9;
}

:deep(.van-empty) {
  padding: 38px 0;
}

:deep(.van-empty__description) {
  font-size: 11px;
  color: #98a491;
}

@media (min-width: 560px) {
  .menu-body {
    padding-left: 24px;
    padding-right: 24px;
  }

  .category-strip {
    margin-left: -24px;
    margin-right: -24px;
    padding-left: 24px;
    padding-right: 24px;
  }
}
</style>
