<template>
  <section class="page">
    <header class="page-heading">
      <div>
        <p class="eyebrow">MENU LIBRARY</p>
        <h2>菜品管理</h2>
        <p class="page-desc">维护菜品信息与售卖状态，让每一份菜单保持新鲜。</p>
      </div>
      <el-button type="primary" :icon="Plus" :disabled="!categories.length" @click="openCreate">
        新增菜品
      </el-button>
    </header>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form" @submit.prevent="search">
        <el-form-item label="分类">
          <el-select v-model="filters.categoryId" clearable placeholder="全部分类" style="width: 160px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="上架中" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品">
          <el-input v-model="filters.name" clearable placeholder="搜索菜品名称" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="dishes" row-key="id">
        <el-table-column label="菜品" min-width="250">
          <template #default="{ row }">
            <div class="dish-cell">
              <el-image :src="row.imageUrl" fit="cover" class="dish-thumb">
                <template #error>
                  <div class="thumb-fallback"><el-icon><Picture /></el-icon></div>
                </template>
              </el-image>
              <div class="dish-meta">
                <strong>{{ row.name }}</strong>
                <span>{{ row.categoryName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="muted">{{ row.description || '暂无描述' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100" align="right">
          <template #default="{ row }">
            <strong class="price">{{ money(row.price) }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="72" align="center" />
        <el-table-column label="售卖状态" width="118" align="center">
          <template #default="{ row }">
            <div class="status-switch">
              <el-switch
                :model-value="row.status === 1"
                :loading="row._statusLoading"
                @change="(value) => toggleStatus(row, value)"
              />
              <span :class="['status-label', row.status === 1 ? 'on' : 'off']">
                {{ row.status === 1 ? '上架' : '下架' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            <span class="muted">{{ dateText(row.updateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="126">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有匹配的菜品" />
        </template>
      </el-table>

      <div class="pagination-wrap">
        <span class="pagination-note">共 {{ total }} 道菜品</span>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑菜品' : '新增菜品'"
      width="700px"
      class="lf-dialog"
      destroy-on-close
    >
      <div class="dish-editor">
        <div class="image-column">
          <div class="image-preview">
            <el-image v-if="form.imageUrl" :src="form.imageUrl" fit="cover">
              <template #error>
                <div class="preview-fallback">
                  <el-icon><Picture /></el-icon>
                  <span>图片路径不可访问</span>
                </div>
              </template>
            </el-image>
            <div v-else class="preview-fallback">
              <el-icon><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </div>
          <p class="image-tip">使用 <code>/mock/</code> 开头的模拟路径，不接图床。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dish-form">
          <div class="form-grid">
            <el-form-item label="菜品名称" prop="name">
              <el-input v-model="form.name" maxlength="100" placeholder="例如：鸡胸肉藜麦沙拉" />
            </el-form-item>
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
                <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="售价" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :max="99999999"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="排序值">
              <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </div>

          <el-form-item label="菜品描述">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              placeholder="食材、口味或搭配说明"
            />
          </el-form-item>

          <el-form-item label="图片路径">
            <el-input v-model="form.imageUrl" placeholder="/mock/dish/default.svg" />
          </el-form-item>

          <el-form-item label="售卖状态">
            <el-radio-group v-model="form.status">
              <el-radio-button :value="1">上架</el-radio-button>
              <el-radio-button :value="0">下架</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存菜品</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import { getCategories } from '@/api/category'
import { changeDishStatus, createDish, deleteDish, getDishes, updateDish } from '@/api/dish'
import { dateText, money } from '@/utils/format'

const categories = ref([])
const dishes = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

const filters = reactive({
  categoryId: '',
  status: '',
  name: ''
})

const form = reactive({
  categoryId: null,
  name: '',
  description: '',
  imageUrl: '/mock/dish/default.svg',
  price: 0,
  sort: 0,
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    { max: 100, message: '不能超过 100 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'change' }]
}

async function loadCategories() {
  categories.value = await getCategories()
}

async function load() {
  loading.value = true
  try {
    const data = await getDishes({
      categoryId: filters.categoryId || undefined,
      status: filters.status === '' ? undefined : filters.status,
      name: filters.name || undefined,
      pageNum: pageNum.value,
      pageSize
    })
    dishes.value = (data.records || []).map((item) => ({ ...item, _statusLoading: false }))
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  load()
}

function resetFilters() {
  filters.categoryId = ''
  filters.status = ''
  filters.name = ''
  pageNum.value = 1
  load()
}

function resetForm() {
  editingId.value = null
  form.categoryId = categories.value[0]?.id ?? null
  form.name = ''
  form.description = ''
  form.imageUrl = '/mock/dish/default.svg'
  form.price = 0
  form.sort = 0
  form.status = 1
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.categoryId = row.categoryId
  form.name = row.name
  form.description = row.description || ''
  form.imageUrl = row.imageUrl || '/mock/dish/default.svg'
  form.price = Number(row.price || 0)
  form.sort = row.sort ?? 0
  form.status = row.status
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      categoryId: form.categoryId,
      name: form.name,
      description: form.description,
      imageUrl: form.imageUrl,
      price: form.price,
      sort: form.sort,
      status: form.status
    }
    if (editingId.value) {
      await updateDish(editingId.value, payload)
      ElMessage.success('菜品已更新')
    } else {
      await createDish(payload)
      ElMessage.success('菜品已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row, value) {
  row._statusLoading = true
  try {
    await changeDishStatus(row.id, value ? 1 : 0)
    row.status = value ? 1 : 0
    ElMessage.success(value ? '菜品已上架' : '菜品已下架')
  } finally {
    row._statusLoading = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(
    `删除「${row.name}」后，购物车中的该菜品会移除，历史订单仍保留快照。确定删除吗？`,
    '删除菜品',
    { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
  )
  await deleteDish(row.id)
  ElMessage.success('菜品已删除')
  if (dishes.value.length === 1 && pageNum.value > 1) pageNum.value -= 1
  await load()
}

onMounted(async () => {
  await loadCategories()
  await load()
})
</script>

<style scoped>
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

.dish-cell {
  display: flex;
  align-items: center;
  gap: 13px;
}

.dish-thumb {
  width: 62px;
  height: 48px;
  flex: 0 0 auto;
  border-radius: 11px;
  overflow: hidden;
  background: #eef3e9;
}

.thumb-fallback,
.preview-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #8ca383;
  background: #edf3e8;
  font-size: 11px;
}

.thumb-fallback .el-icon {
  font-size: 18px;
}

.dish-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 0;
}

.dish-meta strong {
  font-size: 13px;
  font-weight: 600;
  color: #283e2b;
}

.dish-meta span {
  font-size: 11px;
  color: #94a08f;
}

.price {
  font-size: 13px;
  color: #315236;
  font-variant-numeric: tabular-nums;
}

.status-switch {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.status-label {
  font-size: 11px;
}

.status-label.on {
  color: #4e7b4a;
}

.status-label.off {
  color: #a0a99a;
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

.dish-editor {
  display: grid;
  grid-template-columns: 210px minmax(0, 1fr);
  gap: 24px;
}

.image-column {
  min-width: 0;
}

.image-preview {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: 16px;
  background: #edf3e8;
  border: 1px solid rgba(74, 104, 68, 0.1);
}

.image-preview :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.image-tip {
  margin: 12px 2px 0;
  font-size: 11px;
  line-height: 1.7;
  color: #96a290;
}

.image-tip code {
  padding: 1px 4px;
  border-radius: 4px;
  background: #eef3e9;
  color: #5c7855;
}

.dish-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 14px;
}

@media (max-width: 760px) {
  .dish-editor {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .image-column {
    max-width: 250px;
  }
}
</style>
