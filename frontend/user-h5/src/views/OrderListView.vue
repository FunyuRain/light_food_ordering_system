<template>
  <div class="orders-page">
    <van-nav-bar title="我的订单" right-text="刷新" safe-area-inset-top @click-right="load(true)" />

    <div class="orders-intro">
      <p class="eyebrow">YOUR ORDERS</p>
      <div class="intro-row">
        <h1>每一餐的记录。</h1>
        <span v-if="total">{{ total }} 笔</span>
      </div>
      <p>查看状态、完成支付，或者确认这一餐已经好好吃完。</p>
    </div>

    <div class="status-strip">
      <button
        v-for="tab in statusTabs"
        :key="tab.label"
        type="button"
        :class="['status-chip', { active: activeStatus === tab.value }]"
        @click="selectStatus(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading && !orders.length" class="orders-loading">
      <van-loading color="#4f7c4b" size="24" vertical>加载订单中</van-loading>
    </div>

    <div v-else-if="orders.length" class="order-list">
      <article
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="router.push(`/orders/${order.id}`)"
      >
        <header class="order-head">
          <div>
            <span class="order-no">{{ order.orderNo }}</span>
            <span class="order-time">{{ dateText(order.createTime) }}</span>
          </div>
          <span :class="['status-badge', orderStatus(order.status).tone]">
            {{ orderStatus(order.status).label }}
          </span>
        </header>

        <div class="order-body">
          <div class="order-total">
            <span>订单金额</span>
            <strong>{{ money(order.totalAmount) }}</strong>
          </div>
          <div class="order-meta">
            <span><van-icon name="location-o" /> {{ order.receiverName }}</span>
            <span class="hint">{{ orderStatus(order.status).hint }}</span>
          </div>
        </div>

        <footer class="order-foot" @click.stop>
          <button class="detail-link" type="button" @click="router.push(`/orders/${order.id}`)">
            查看详情
          </button>
          <div class="order-actions">
            <template v-if="order.status === 0">
              <van-button size="small" round plain class="plain-action" @click="cancel(order)">取消订单</van-button>
              <van-button size="small" round type="primary" class="primary-action" @click="pay(order)">去支付</van-button>
            </template>
            <template v-else-if="order.status === 2">
              <van-button size="small" round type="primary" class="primary-action" @click="complete(order)">
                确认完成
              </van-button>
            </template>
            <template v-else>
              <van-button size="small" round plain class="plain-action" @click="router.push('/menu')">
                去点餐
              </van-button>
            </template>
          </div>
        </footer>
      </article>

      <div class="load-more">
        <van-button
          v-if="hasMore"
          size="small"
          round
          plain
          :loading="loading"
          @click="load(false)"
        >
          加载更多
        </van-button>
        <span v-else>已经看到全部订单了</span>
      </div>
    </div>

    <van-empty v-else description="还没有订单，去点一份轻食吧" image="search">
      <van-button round type="primary" class="empty-btn" @click="router.replace('/menu')">去点餐</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { cancelOrder, completeOrder, getOrders, payOrder } from '@/api/order'
import { dateText, money, orderStatus } from '@/utils/format'

const router = useRouter()

const statusTabs = [
  { label: '全部', value: null },
  { label: '待付款', value: 0 },
  { label: '待接单', value: 1 },
  { label: '已接单', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const activeStatus = ref(null)
const orders = ref([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)

async function load(reset = true) {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  try {
    if (reset) {
      pageNum.value = 1
      orders.value = []
      hasMore.value = true
    }
    const data = await getOrders({
      status: activeStatus.value === null ? undefined : activeStatus.value,
      pageNum: pageNum.value,
      pageSize
    })
    const records = data.records || []
    orders.value = reset ? records : [...orders.value, ...records]
    total.value = data.total || 0
    hasMore.value = orders.value.length < total.value
    if (records.length) pageNum.value += 1
  } finally {
    loading.value = false
  }
}

function selectStatus(value) {
  if (activeStatus.value === value) return
  activeStatus.value = value
  load(true)
}

async function pay(order) {
  try {
    await showConfirmDialog({
      title: '模拟支付',
      message: `确认支付 ${money(order.totalAmount)} 吗？本功能仅模拟支付，不产生真实扣款。`,
      confirmButtonText: '确认支付',
      cancelButtonText: '稍后'
    })
  } catch {
    return
  }
  await payOrder(order.id)
  showSuccessToast('支付成功，等待商家接单')
  await load(true)
}

async function cancel(order) {
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: '确定取消这笔待付款订单吗？',
      confirmButtonText: '确认取消',
      cancelButtonText: '保留订单'
    })
  } catch {
    return
  }
  await cancelOrder(order.id, '用户取消')
  showSuccessToast('订单已取消')
  await load(true)
}

async function complete(order) {
  try {
    await showConfirmDialog({
      title: '确认完成',
      message: '确认已经收到并享用这份轻食了吗？',
      confirmButtonText: '确认完成',
      cancelButtonText: '还没'
    })
  } catch {
    return
  }
  await completeOrder(order.id)
  showSuccessToast('订单已完成，感谢你的选择')
  await load(true)
}

onMounted(() => load(true))
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  padding-bottom: 30px;
  background:
    radial-gradient(circle at 98% 1%, rgba(225, 235, 211, 0.7), transparent 27%),
    #f8f7f2;
}

.orders-page :deep(.van-nav-bar) {
  background: transparent;
}

.orders-page :deep(.van-nav-bar__title) {
  font-size: 14px;
  font-weight: 600;
  color: #2b422e;
}

.orders-page :deep(.van-nav-bar__text) {
  color: #6f8d66;
  font-size: 11px;
}

.orders-intro {
  padding: 20px 20px 17px;
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
  justify-content: space-between;
  gap: 10px;
}

.intro-row h1 {
  margin: 0;
  font-size: 25px;
  font-weight: 500;
  color: #263e29;
}

.intro-row span {
  font-size: 10px;
  color: #9ba695;
}

.orders-intro > p:last-child {
  margin: 8px 0 0;
  font-size: 11px;
  line-height: 1.7;
  color: #96a18f;
}

.status-strip {
  display: flex;
  gap: 7px;
  overflow-x: auto;
  padding: 3px 18px 10px;
  scrollbar-width: none;
}

.status-strip::-webkit-scrollbar {
  display: none;
}

.status-chip {
  flex: 0 0 auto;
  padding: 7px 13px;
  border: 1px solid #e4e9df;
  border-radius: 999px;
  background: #fff;
  color: #7f8c79;
  font-size: 10px;
  cursor: pointer;
}

.status-chip.active {
  border-color: #4f7c4b;
  background: #4f7c4b;
  color: #f4f8ed;
  box-shadow: 0 5px 14px rgba(68, 102, 61, 0.15);
}

.orders-loading {
  display: grid;
  place-items: center;
  min-height: 38vh;
}

.order-list {
  padding: 0 16px;
}

.order-card {
  margin-bottom: 12px;
  padding: 16px;
  overflow: hidden;
  border: 1px solid rgba(65, 91, 61, 0.07);
  border-radius: 20px;
  background: #fffdfa;
  box-shadow: 0 8px 22px rgba(49, 70, 45, 0.03);
  cursor: pointer;
}

.order-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding-bottom: 13px;
  border-bottom: 1px solid #f2f4ef;
}

.order-head > div {
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 0;
}

.order-no {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: #536b51;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-time {
  font-size: 9px;
  color: #a5aea0;
}

.status-badge {
  flex: 0 0 auto;
  padding: 4px 9px;
  border-radius: 999px;
  font-size: 9px;
  background: #f1f4ed;
  color: #7b8974;
}

.status-badge.amber {
  background: #fbf3e6;
  color: #ac7d37;
}

.status-badge.green {
  background: #edf5e9;
  color: #527b4c;
}

.status-badge.deep {
  background: #e8f1e2;
  color: #3f6c40;
}

.status-badge.red {
  background: #fbefec;
  color: #b27a6b;
}

.order-body {
  padding: 14px 0 12px;
}

.order-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.order-total span {
  font-size: 10px;
  color: #929e8c;
}

.order-total strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 23px;
  font-weight: 400;
  color: #355a37;
}

.order-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 6px;
  font-size: 9px;
  color: #99a493;
}

.order-meta .van-icon {
  margin-right: 3px;
}

.order-meta .hint {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: #a6b09f;
}

.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #f2f4ef;
}

.detail-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: #6c8b63;
  font-size: 10px;
  cursor: pointer;
}

.order-actions {
  display: flex;
  gap: 7px;
}

.primary-action {
  min-width: 76px;
  border: 0;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
  font-size: 10px;
}

.plain-action {
  min-width: 76px;
  border-color: #e9ede4;
  color: #7c8a76;
  font-size: 10px;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 12px 0 10px;
  font-size: 9px;
  color: #a5aea0;
}

.empty-btn {
  min-width: 120px;
  height: 40px;
  border: 0;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
}

:deep(.van-empty) {
  padding: 50px 0;
}

:deep(.van-empty__description) {
  font-size: 11px;
  color: #98a491;
}
</style>
