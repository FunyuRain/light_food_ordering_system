<template>
  <div class="detail-page" :class="{ 'has-actions': hasActions }">
    <van-nav-bar title="订单详情" left-arrow safe-area-inset-top @click-left="router.back()" />

    <div v-if="loading" class="detail-loading">
      <van-loading color="#4f7c4b" size="24" vertical>加载订单中</van-loading>
    </div>

    <template v-else-if="order">
      <header class="status-hero" :class="status.tone">
        <div class="status-copy">
          <p class="eyebrow">ORDER STATUS</p>
          <h1>{{ status.label }}</h1>
          <p>{{ status.hint }}</p>
        </div>
        <div class="hero-order">
          <span>{{ order.orderNo }}</span>
          <small>{{ dateText(order.createTime) }}</small>
        </div>
        <div class="hero-orbit orbit-a"></div>
        <div class="hero-orbit orbit-b"></div>
      </header>

      <main class="detail-body">
        <section v-if="order.status === 4" class="cancel-card">
          <div class="cancel-icon"><van-icon name="close" /></div>
          <div>
            <strong>{{ cancelByText(order.cancelBy) }}</strong>
            <p>{{ order.cancelReason || '订单已取消' }}</p>
            <span>{{ dateText(order.cancelTime) }}</span>
          </div>
        </section>

        <section class="detail-card">
          <div class="card-title">
            <h2>收货信息</h2>
            <span>DELIVERY</span>
          </div>
          <div class="receiver-row">
            <div class="receiver-icon"><van-icon name="location" /></div>
            <div>
              <strong>{{ order.receiverName }} <small>{{ order.receiverPhone }}</small></strong>
              <p>{{ order.deliveryAddress }}</p>
            </div>
          </div>
        </section>

        <section class="detail-card">
          <div class="card-title">
            <h2>菜品明细</h2>
            <span>{{ order.items?.length || 0 }} ITEMS</span>
          </div>
          <div class="detail-items">
            <div v-for="item in order.items" :key="item.dishId + item.dishName" class="detail-item">
              <div class="item-image">
                <img :src="item.dishImage || '/mock/dish/default.svg'" :alt="item.dishName" @error="onImageError" />
              </div>
              <div class="item-info">
                <strong>{{ item.dishName }}</strong>
                <span>{{ money(item.unitPrice) }} × {{ item.quantity }}</span>
              </div>
              <span class="item-amount">{{ money(item.amount) }}</span>
            </div>
          </div>
        </section>

        <section class="detail-card">
          <div class="card-title">
            <h2>费用明细</h2>
            <span>PAYMENT</span>
          </div>
          <div class="fee-row">
            <span>商品金额</span>
            <strong>{{ money(order.totalAmount) }}</strong>
          </div>
          <div class="fee-row">
            <span>配送费</span>
            <strong>免配送费</strong>
          </div>
          <div class="fee-total">
            <span>实付金额</span>
            <strong>{{ money(order.totalAmount) }}</strong>
          </div>
          <div class="pay-meta">
            <span>{{ order.payMethod === 1 ? '模拟支付' : '尚未支付' }}</span>
            <span v-if="order.payTime">{{ dateText(order.payTime) }}</span>
          </div>
        </section>

        <section v-if="order.remark" class="detail-card">
          <div class="card-title">
            <h2>订单备注</h2>
            <span>NOTE</span>
          </div>
          <p class="remark-text">{{ order.remark }}</p>
        </section>

        <section class="detail-card">
          <div class="card-title">
            <h2>订单进度</h2>
            <span>TIMELINE</span>
          </div>
          <div class="timeline">
            <div v-for="node in timeline" :key="node.label" class="timeline-row">
              <div class="timeline-mark" :style="{ background: node.color }"></div>
              <div class="timeline-copy">
                <strong>{{ node.label }}</strong>
                <span>{{ node.time }}</span>
                <p v-if="node.desc">{{ node.desc }}</p>
              </div>
            </div>
          </div>
        </section>
      </main>

      <div v-if="hasActions" class="detail-actions">
        <van-button
          v-if="order.status === 0"
          round
          plain
          class="secondary-btn"
          @click="cancel"
        >
          取消订单
        </van-button>
        <van-button
          v-if="order.status === 0"
          round
          type="primary"
          class="primary-btn"
          @click="pay"
        >
          模拟支付
        </van-button>
        <van-button
          v-if="order.status === 2"
          round
          type="primary"
          class="primary-btn wide"
          @click="complete"
        >
          确认完成
        </van-button>
      </div>
    </template>

    <van-empty v-else description="订单不存在" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { cancelOrder, completeOrder, getOrder, payOrder } from '@/api/order'
import { cancelByText, dateText, money, orderStatus } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const order = ref(null)

const status = computed(() => orderStatus(order.value?.status))
const hasActions = computed(() => order.value && (order.value.status === 0 || order.value.status === 2))

const timeline = computed(() => {
  if (!order.value) return []
  const nodes = []
  const item = order.value
  if (item.createTime) {
    nodes.push({ label: '订单已提交', time: item.createTime, color: '#8fa383' })
  }
  if (item.payTime) {
    nodes.push({ label: '模拟支付成功', time: item.payTime, color: '#79a16f' })
  }
  if (item.acceptTime) {
    nodes.push({ label: '商家已接单', time: item.acceptTime, color: '#5d8b55' })
  }
  if (item.finishTime) {
    nodes.push({ label: '订单已完成', time: item.finishTime, color: '#426b40' })
  }
  if (item.cancelTime) {
    nodes.push({
      label: '订单已取消',
      time: item.cancelTime,
      color: '#c88676',
      desc: `${cancelByText(item.cancelBy)}${item.cancelReason ? ` · ${item.cancelReason}` : ''}`
    })
  }
  return nodes
})

async function load() {
  loading.value = true
  try {
    order.value = await getOrder(route.params.id)
  } finally {
    loading.value = false
  }
}

async function pay() {
  try {
    await showConfirmDialog({
      title: '模拟支付',
      message: `确认支付 ${money(order.value.totalAmount)} 吗？本功能不对接真实支付，不产生扣款。`,
      confirmButtonText: '确认支付',
      cancelButtonText: '稍后'
    })
  } catch {
    return
  }
  order.value = await payOrder(order.value.id)
  showSuccessToast('支付成功，等待商家接单')
}

async function cancel() {
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
  order.value = await cancelOrder(order.value.id, '用户取消')
  showSuccessToast('订单已取消')
}

async function complete() {
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
  order.value = await completeOrder(order.value.id)
  showSuccessToast('订单已完成，感谢你的选择')
}

function onImageError(event) {
  event.target.onerror = null
  event.target.src = '/mock/dish/default.svg'
}

onMounted(load)
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f8f7f2;
}

.detail-page.has-actions {
  padding-bottom: 92px;
}

.detail-page :deep(.van-nav-bar) {
  background: #f8f7f2;
}

.detail-page :deep(.van-nav-bar__title) {
  font-size: 14px;
  font-weight: 600;
  color: #2b422e;
}

.detail-loading {
  display: grid;
  place-items: center;
  min-height: 55vh;
}

.status-hero {
  position: relative;
  overflow: hidden;
  margin: 0 14px;
  padding: 24px 20px 20px;
  border-radius: 24px;
  color: #f2f8ea;
  background:
    radial-gradient(circle at 84% 20%, rgba(196, 222, 164, 0.19), transparent 30%),
    linear-gradient(148deg, #28432f, #3c6041);
}

.status-hero.amber {
  background:
    radial-gradient(circle at 84% 20%, rgba(245, 218, 165, 0.2), transparent 30%),
    linear-gradient(148deg, #6c5230, #8e6a39);
}

.status-hero.red {
  background:
    radial-gradient(circle at 84% 20%, rgba(245, 197, 184, 0.16), transparent 30%),
    linear-gradient(148deg, #5f3d35, #835348);
}

.status-copy {
  position: relative;
  z-index: 2;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 8px;
  letter-spacing: 0.22em;
  color: rgba(230, 244, 217, 0.6);
}

.status-copy h1 {
  margin: 0;
  font-size: 30px;
  font-weight: 500;
  letter-spacing: 0.04em;
}

.status-copy > p:last-child {
  margin: 8px 0 0;
  max-width: 280px;
  font-size: 10px;
  line-height: 1.7;
  color: rgba(238, 248, 229, 0.62);
}

.hero-order {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 22px;
  padding-top: 13px;
  border-top: 1px solid rgba(234, 245, 223, 0.12);
}

.hero-order span {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
  color: rgba(240, 249, 232, 0.72);
}

.hero-order small {
  font-size: 9px;
  color: rgba(238, 248, 229, 0.45);
}

.hero-orbit {
  position: absolute;
  border: 1px solid rgba(219, 238, 201, 0.1);
  border-radius: 50%;
}

.orbit-a {
  width: 160px;
  height: 160px;
  right: -55px;
  top: -40px;
}

.orbit-b {
  width: 90px;
  height: 90px;
  right: -15px;
  top: -5px;
  background: rgba(217, 238, 194, 0.04);
}

.detail-body {
  padding: 13px 14px 0;
}

.cancel-card {
  display: flex;
  gap: 11px;
  margin-bottom: 11px;
  padding: 14px;
  border-radius: 16px;
  background: #fbefec;
}

.cancel-icon {
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 9px;
  background: rgba(255, 255, 255, 0.56);
  color: #b37b6c;
}

.cancel-card strong {
  font-size: 11px;
  color: #8f5d51;
}

.cancel-card p {
  margin: 4px 0 0;
  font-size: 10px;
  line-height: 1.6;
  color: #9d7469;
}

.cancel-card span {
  display: block;
  margin-top: 5px;
  font-size: 8px;
  color: #b69a92;
}

.detail-card {
  margin-bottom: 11px;
  padding: 16px;
  border: 1px solid rgba(65, 91, 61, 0.07);
  border-radius: 19px;
  background: #fffdfa;
  box-shadow: 0 8px 22px rgba(49, 70, 45, 0.025);
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 13px;
}

.card-title h2 {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: #334a35;
}

.card-title span {
  font-size: 8px;
  letter-spacing: 0.15em;
  color: #adb6a6;
}

.receiver-row {
  display: flex;
  gap: 11px;
}

.receiver-icon {
  width: 31px;
  height: 31px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: #edf4e7;
  color: #5e8054;
}

.receiver-row strong {
  font-size: 12px;
  color: #2e4530;
}

.receiver-row small {
  margin-left: 6px;
  font-size: 9px;
  font-weight: 400;
  color: #97a392;
}

.receiver-row p {
  margin: 6px 0 0;
  font-size: 10px;
  line-height: 1.7;
  color: #7f8b78;
}

.detail-items {
  border-top: 1px solid #f1f3ee;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 0;
  border-bottom: 1px solid #f3f4f0;
}

.detail-item:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.item-image {
  width: 44px;
  height: 38px;
  flex: 0 0 auto;
  overflow: hidden;
  border-radius: 9px;
  background: #edf3e8;
}

.item-image img {
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
  color: #364c38;
}

.item-info span {
  font-size: 9px;
  color: #9ba695;
}

.item-amount {
  font-size: 11px;
  color: #4b6648;
}

.fee-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 10px;
  color: #8e9a88;
}

.fee-row strong {
  font-weight: 500;
  color: #6d7e68;
}

.fee-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f2ed;
  font-size: 11px;
  color: #4d604b;
}

.fee-total strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 23px;
  font-weight: 400;
  color: #355a37;
}

.pay-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 9px;
  font-size: 8px;
  color: #a8b1a2;
}

.remark-text {
  margin: 0;
  padding: 11px;
  border-radius: 11px;
  background: #fbf8ef;
  font-size: 10px;
  line-height: 1.7;
  color: #81765f;
}

.timeline {
  padding-left: 3px;
}

.timeline-row {
  position: relative;
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
}

.timeline-row:last-child {
  padding-bottom: 0;
}

.timeline-mark {
  width: 8px;
  height: 8px;
  flex: 0 0 auto;
  margin-top: 3px;
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(122, 155, 108, 0.08);
}

.timeline-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: -2px;
}

.timeline-copy strong {
  font-size: 11px;
  font-weight: 500;
  color: #405641;
}

.timeline-copy span {
  font-size: 8px;
  color: #a2ac9c;
}

.timeline-copy p {
  margin: 2px 0 0;
  font-size: 9px;
  color: #a08a80;
}

.detail-actions {
  position: fixed;
  left: 50%;
  bottom: 0;
  z-index: 10;
  width: min(100%, 520px);
  transform: translateX(-50%);
  display: flex;
  gap: 9px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(253, 252, 247, 0.96);
  border-top: 1px solid rgba(66, 92, 62, 0.08);
  backdrop-filter: blur(14px);
}

.secondary-btn {
  flex: 0 0 34%;
  height: 43px;
  border-color: #e7ebe1;
  color: #7c8a76;
  font-size: 12px;
}

.primary-btn {
  flex: 1;
  height: 43px;
  border: 0;
  font-size: 12px;
  background: linear-gradient(100deg, #4f7c4b, #345b39);
}

.primary-btn.wide {
  flex: 1;
}
</style>
