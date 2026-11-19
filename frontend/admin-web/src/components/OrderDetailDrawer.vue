<template>
  <el-drawer
    :model-value="modelValue"
    size="560px"
    class="order-drawer"
    :with-header="false"
    @update:model-value="(value) => emit('update:modelValue', value)"
  >
    <div v-loading="loading" class="drawer-body">
      <template v-if="order">
        <header class="drawer-head">
          <div>
            <p class="eyebrow">ORDER DETAIL</p>
            <h3>{{ order.orderNo }}</h3>
            <p>{{ dateText(order.createTime) }} 创建</p>
          </div>
          <el-tag :type="status.type" effect="light" round>{{ status.label }}</el-tag>
        </header>

        <div class="status-banner" :class="status.tone">
          <div class="status-mark"><el-icon><component :is="statusIcon" /></el-icon></div>
          <div>
            <strong>{{ status.label }}</strong>
            <p>{{ statusHint }}</p>
          </div>
        </div>

        <section class="info-grid">
          <div>
            <span>下单用户</span>
            <strong>{{ order.userName || '—' }}</strong>
          </div>
          <div>
            <span>订单金额</span>
            <strong class="amount">{{ money(order.totalAmount) }}</strong>
          </div>
          <div>
            <span>支付方式</span>
            <strong>{{ order.payMethod === 1 ? '模拟支付' : '未支付' }}</strong>
          </div>
          <div>
            <span>订单编号</span>
            <strong class="mono">{{ order.orderNo }}</strong>
          </div>
        </section>

        <section class="drawer-section">
          <div class="section-title">
            <h4>收货信息</h4>
            <span>DELIVERY</span>
          </div>
          <div class="receiver-card">
            <div class="receiver-avatar">{{ (order.receiverName || '客').slice(0, 1) }}</div>
            <div>
              <strong>{{ order.receiverName }} <small>{{ order.receiverPhone }}</small></strong>
              <p>{{ order.deliveryAddress }}</p>
            </div>
          </div>
        </section>

        <section class="drawer-section">
          <div class="section-title">
            <h4>菜品明细</h4>
            <span>{{ order.items?.length || 0 }} ITEMS</span>
          </div>
          <div class="item-list">
            <div v-for="item in order.items" :key="item.dishId + item.dishName" class="item-row">
              <el-image :src="item.dishImage" fit="cover" class="item-image">
                <template #error>
                  <div class="item-fallback"><el-icon><Picture /></el-icon></div>
                </template>
              </el-image>
              <div class="item-info">
                <strong>{{ item.dishName }}</strong>
                <span>{{ money(item.unitPrice) }} × {{ item.quantity }}</span>
              </div>
              <strong class="item-amount">{{ money(item.amount) }}</strong>
            </div>
          </div>
          <div class="total-line">
            <span>合计</span>
            <strong>{{ money(order.totalAmount) }}</strong>
          </div>
        </section>

        <section v-if="order.remark" class="drawer-section">
          <div class="section-title">
            <h4>订单备注</h4>
            <span>NOTE</span>
          </div>
          <p class="remark-box">{{ order.remark }}</p>
        </section>

        <section class="drawer-section">
          <div class="section-title">
            <h4>订单进度</h4>
            <span>TIMELINE</span>
          </div>
          <el-timeline class="order-timeline">
            <el-timeline-item
              v-for="node in timeline"
              :key="node.label"
              :timestamp="node.time"
              :color="node.color"
              placement="top"
            >
              <strong>{{ node.label }}</strong>
              <p v-if="node.desc">{{ node.desc }}</p>
            </el-timeline-item>
          </el-timeline>
        </section>
      </template>
      <el-empty v-else-if="!loading" description="订单详情为空" />
    </div>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'
import { CircleCheck, CircleClose, Clock, Picture, ShoppingBag } from '@element-plus/icons-vue'
import { cancelByText, dateText, money, orderStatus } from '@/utils/format'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const status = computed(() => orderStatus(props.order?.status))

const statusIcon = computed(() => {
  if (props.order?.status === 3) return CircleCheck
  if (props.order?.status === 4) return CircleClose
  return ShoppingBag
})

const statusHint = computed(() => {
  const map = {
    0: '等待用户完成模拟支付',
    1: '用户已付款，等待商家接单',
    2: '商家已接单，等待用户确认完成',
    3: '订单已完成',
    4: props.order?.cancelReason ? `取消原因：${props.order.cancelReason}` : '订单已取消'
  }
  return map[props.order?.status] || ''
})

const timeline = computed(() => {
  if (!props.order) return []
  const nodes = []
  const order = props.order
  if (order.createTime) {
    nodes.push({ label: '订单已提交', time: order.createTime, color: '#8fa383' })
  }
  if (order.payTime) {
    nodes.push({ label: '支付成功', time: order.payTime, color: '#79a16f' })
  }
  if (order.acceptTime) {
    nodes.push({ label: '商家已接单', time: order.acceptTime, color: '#5d8b55' })
  }
  if (order.finishTime) {
    nodes.push({ label: '订单已完成', time: order.finishTime, color: '#426b40' })
  }
  if (order.cancelTime) {
    nodes.push({
      label: '订单已取消',
      time: order.cancelTime,
      color: '#c88676',
      desc: `${cancelByText(order.cancelBy)}${order.cancelReason ? ` · ${order.cancelReason}` : ''}`
    })
  }
  return nodes
})
</script>

<style scoped>
.drawer-body {
  min-height: 100%;
  padding: 28px 28px 40px;
}

.drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 22px;
  border-bottom: 1px solid #edf0e9;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 9px;
  letter-spacing: 0.22em;
  color: #99a693;
}

.drawer-head h3 {
  margin: 0;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 18px;
  font-weight: 600;
  color: #243b27;
}

.drawer-head p:last-child {
  margin: 7px 0 0;
  font-size: 11px;
  color: #97a392;
}

.status-banner {
  display: flex;
  align-items: center;
  gap: 13px;
  margin: 22px 0;
  padding: 16px;
  border-radius: 16px;
  background: #f3f6ef;
}

.status-banner.amber {
  background: #fbf4e6;
}

.status-banner.green {
  background: #edf5e9;
}

.status-banner.red {
  background: #fbefec;
}

.status-mark {
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border-radius: 13px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.7);
  color: #547a4f;
  font-size: 20px;
}

.status-banner.amber .status-mark {
  color: #b18337;
}

.status-banner.red .status-mark {
  color: #b46d5e;
}

.status-banner strong {
  font-size: 14px;
  color: #2e4431;
}

.status-banner p {
  margin: 4px 0 0;
  font-size: 11px;
  line-height: 1.5;
  color: #84917f;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
  border: 1px solid #edf0e9;
  background: #fbfcf9;
}

.info-grid div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.info-grid span {
  font-size: 10px;
  color: #9ba695;
}

.info-grid strong {
  font-size: 12px;
  font-weight: 500;
  color: #3b4f3c;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-grid .amount {
  font-size: 17px;
  color: #3f6c40;
}

.info-grid .mono {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 10px;
}

.drawer-section {
  margin-top: 26px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title h4 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #2a402c;
}

.section-title span {
  font-size: 9px;
  letter-spacing: 0.16em;
  color: #afb9a9;
}

.receiver-card {
  display: flex;
  gap: 12px;
  padding: 15px;
  border-radius: 15px;
  background: #f7f9f4;
}

.receiver-avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #e2eddb;
  color: #55784f;
  font-size: 13px;
  font-weight: 600;
}

.receiver-card strong {
  font-size: 12px;
  color: #334a36;
}

.receiver-card small {
  margin-left: 7px;
  font-size: 10px;
  font-weight: 400;
  color: #929e8d;
}

.receiver-card p {
  margin: 6px 0 0;
  font-size: 11px;
  line-height: 1.7;
  color: #798675;
}

.item-list {
  border-top: 1px solid #edf0e9;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2ed;
}

.item-image {
  width: 46px;
  height: 38px;
  flex: 0 0 auto;
  border-radius: 9px;
  overflow: hidden;
  background: #edf3e8;
}

.item-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #9dad94;
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-info strong {
  font-size: 12px;
  color: #344a36;
}

.item-info span {
  font-size: 10px;
  color: #9aa695;
}

.item-amount {
  font-size: 12px;
  color: #405a40;
}

.total-line {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 12px;
  padding-top: 16px;
}

.total-line span {
  font-size: 11px;
  color: #8d9988;
}

.total-line strong {
  font-size: 20px;
  font-weight: 500;
  color: #355d38;
}

.remark-box {
  margin: 0;
  padding: 14px;
  border-radius: 12px;
  background: #fbf7ee;
  font-size: 12px;
  line-height: 1.7;
  color: #756b55;
}

.order-timeline {
  padding-left: 4px;
}

.order-timeline :deep(.el-timeline-item__timestamp) {
  font-size: 10px;
  color: #a0aa9a;
}

.order-timeline strong {
  font-size: 12px;
  color: #3b503d;
}

.order-timeline p {
  margin: 5px 0 0;
  font-size: 11px;
  color: #8f9b8a;
}
</style>
