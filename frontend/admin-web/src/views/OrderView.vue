<template>
  <section class="page">
    <header class="page-heading">
      <div>
        <p class="eyebrow">ORDER DESK</p>
        <h2>订单管理</h2>
        <p class="page-desc">接住每一份期待，及时接单，清楚掌握每笔订单的状态。</p>
      </div>
      <div class="heading-note">
        <span class="pulse"></span>
        每 30 秒可手动刷新
        <el-button text :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
    </header>

    <div class="order-stats">
      <button
        v-for="item in statusCards"
        :key="item.label"
        class="stat-card"
        :class="{ active: filters.status === item.value }"
        type="button"
        @click="selectStatus(item.value)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value === '' ? total : item.count }}</strong>
        <i :class="['stat-dot', item.tone]"></i>
      </button>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form" @submit.prevent="search">
        <el-form-item label="订单状态">
          <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 150px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="filters.orderNo" clearable placeholder="输入订单号搜索" style="width: 230px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="orders" row-key="id">
        <el-table-column label="订单号" min-width="220">
          <template #default="{ row }">
            <button class="order-link" type="button" @click="openDetail(row)">
              <span class="order-dot" :class="orderStatus(row.status).tone"></span>
              <span>
                <strong>{{ row.orderNo }}</strong>
                <small>{{ dateText(row.createTime) }}</small>
              </span>
            </button>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            <span class="user-name">{{ row.userName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收货人" width="130">
          <template #default="{ row }">
            <div class="receiver-mini">
              <strong>{{ row.receiverName }}</strong>
              <span>{{ row.receiverPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">
            <strong class="order-amount">{{ money(row.totalAmount) }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="orderStatus(row.status).type" effect="light" round>
              {{ orderStatus(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="215" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="success"
              :loading="row._actionLoading"
              @click="accept(row)"
            >
              接单
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === 2"
              link
              type="danger"
              :loading="row._actionLoading"
              @click="cancel(row)"
            >
              取消
            </el-button>
            <span v-if="row.status === 0" class="waiting-text">待用户付款</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无订单" />
        </template>
      </el-table>

      <div class="pagination-wrap">
        <span class="pagination-note">共 {{ total }} 笔订单</span>
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="load"
        />
      </div>
    </el-card>

    <OrderDetailDrawer v-model="drawerVisible" :order="currentOrder" :loading="detailLoading" />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, RefreshLeft, Search } from '@element-plus/icons-vue'
import { acceptOrder, cancelOrder, getOrder, getOrders } from '@/api/order'
import OrderDetailDrawer from '@/components/OrderDetailDrawer.vue'
import { dateText, money, orderStatus } from '@/utils/format'

const orders = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const drawerVisible = ref(false)
const detailLoading = ref(false)
const currentOrder = ref(null)

const filters = reactive({
  status: '',
  orderNo: ''
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待付款', value: 0 },
  { label: '待接单', value: 1 },
  { label: '已接单', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const statusCards = reactive([
  { label: '全部', value: '', tone: 'all', count: 0 },
  { label: '待付款', value: 0, tone: 'muted', count: 0 },
  { label: '待接单', value: 1, tone: 'amber', count: 0 },
  { label: '已接单', value: 2, tone: 'green', count: 0 },
  { label: '已完成', value: 3, tone: 'deep', count: 0 },
  { label: '已取消', value: 4, tone: 'red', count: 0 }
])

async function load() {
  loading.value = true
  try {
    const data = await getOrders({
      status: filters.status === '' ? undefined : filters.status,
      orderNo: filters.orderNo || undefined,
      pageNum: pageNum.value,
      pageSize
    })
    orders.value = (data.records || []).map((item) => ({ ...item, _actionLoading: false }))
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStatusCounts() {
  const cards = statusCards.slice(1)
  const results = await Promise.all(
    cards.map((card) => getOrders({ status: card.value, pageNum: 1, pageSize: 1 }))
  )
  let all = 0
  cards.forEach((card, index) => {
    card.count = results[index]?.total || 0
    all += card.count
  })
  statusCards[0].count = all
}

function search() {
  pageNum.value = 1
  load()
}

function resetFilters() {
  filters.status = ''
  filters.orderNo = ''
  pageNum.value = 1
  load()
}

function selectStatus(value) {
  filters.status = value
  pageNum.value = 1
  load()
}

async function openDetail(row) {
  drawerVisible.value = true
  currentOrder.value = null
  detailLoading.value = true
  try {
    currentOrder.value = await getOrder(row.id)
  } finally {
    detailLoading.value = false
  }
}

async function accept(row) {
  await ElMessageBox.confirm(`确认接单「${row.orderNo}」吗？`, '接单确认', {
    confirmButtonText: '确认接单',
    cancelButtonText: '再想想',
    type: 'success'
  })
  row._actionLoading = true
  try {
    await acceptOrder(row.id)
    ElMessage.success('已接单，准备制作吧')
    await load()
    await loadStatusCounts()
    if (drawerVisible.value && currentOrder.value?.id === row.id) {
      currentOrder.value = await getOrder(row.id)
    }
  } finally {
    row._actionLoading = false
  }
}

async function cancel(row) {
  const result = await ElMessageBox.prompt('请填写取消原因，用户会在订单详情中看到。', '取消订单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '返回',
    type: 'warning',
    inputPlaceholder: '例如：菜品售罄，无法制作',
    inputValidator: (value) => {
      if (!value || !value.trim()) return '取消原因不能为空'
      if (value.length > 200) return '不能超过 200 个字符'
      return true
    }
  })
  row._actionLoading = true
  try {
    await cancelOrder(row.id, result.value.trim())
    ElMessage.success('订单已取消')
    await load()
    await loadStatusCounts()
    if (drawerVisible.value && currentOrder.value?.id === row.id) {
      currentOrder.value = await getOrder(row.id)
    }
  } finally {
    row._actionLoading = false
  }
}

onMounted(() => {
  load()
  loadStatusCounts()
})
</script>

<style scoped>
.heading-note {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: #94a08f;
}

.pulse {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #8db47d;
  box-shadow: 0 0 0 5px rgba(141, 180, 125, 0.12);
}

.order-stats {
  display: grid;
  grid-template-columns: repeat(6, minmax(115px, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 15px 16px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  border: 1px solid rgba(58, 83, 58, 0.08);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: rgba(74, 111, 68, 0.24);
}

.stat-card.active {
  background: #fdfefb;
  border-color: #7fa575;
  box-shadow: 0 10px 22px rgba(69, 99, 65, 0.08);
}

.stat-card span {
  font-size: 10px;
  color: #879582;
}

.stat-card strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 25px;
  font-weight: 400;
  color: #2f4b32;
}

.stat-dot {
  position: absolute;
  right: 13px;
  top: 15px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #b7c2b0;
}

.stat-dot.amber {
  background: #d0a75d;
}

.stat-dot.green {
  background: #83ac78;
}

.stat-dot.deep {
  background: #466e47;
}

.stat-dot.red {
  background: #c58b7d;
}

.filter-card {
  margin-bottom: 18px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px 6px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.order-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.order-link > span:last-child {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-link strong {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 11px;
  font-weight: 600;
  color: #334b35;
}

.order-link small {
  font-size: 10px;
  color: #9da898;
}

.order-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #b7c2b0;
}

.order-dot.amber {
  background: #d0a75d;
}

.order-dot.green {
  background: #83ac78;
}

.order-dot.red {
  background: #c58b7d;
}

.user-name {
  font-size: 12px;
  color: #4b604b;
}

.receiver-mini {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.receiver-mini strong {
  font-size: 12px;
  font-weight: 500;
  color: #425943;
}

.receiver-mini span {
  font-size: 10px;
  color: #9ba695;
}

.order-amount {
  font-size: 13px;
  color: #395b3a;
  font-variant-numeric: tabular-nums;
}

.waiting-text {
  font-size: 10px;
  color: #a7b09f;
}

.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 18px;
}

.pagination-note {
  font-size: 11px;
  color: #98a592;
}

@media (max-width: 1180px) {
  .order-stats {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
