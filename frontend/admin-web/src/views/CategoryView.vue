<template>
  <section class="page">
    <header class="page-heading">
      <div>
        <p class="eyebrow">MENU STRUCTURE</p>
        <h2>分类管理</h2>
        <p class="page-desc">用清晰的分类组织菜单，让点餐更轻快。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
    </header>

    <div class="insight-row">
      <div class="insight-card">
        <span>分类总数</span>
        <strong>{{ categories.length }}</strong>
        <small>个菜单分类</small>
      </div>
      <div class="insight-card wide">
        <span>整理建议</span>
        <p>分类下存在菜品时不能删除；调整顺序时，数值越小展示越靠前。</p>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="categories" row-key="id">
        <el-table-column type="index" label="#" width="62" />
        <el-table-column label="分类名称" min-width="190">
          <template #default="{ row }">
            <div class="category-name">
              <span class="category-dot"></span>
              <strong>{{ row.name }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="260">
          <template #default="{ row }">
            <span class="muted">{{ row.description || '暂无描述' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" align="center">
          <template #default="{ row }">
            <span class="sort-pill">{{ row.sort }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="还没有分类，先创建一个吧" />
        </template>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑分类' : '新增分类'"
      width="500px"
      class="lf-dialog"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="例如：轻食沙拉" />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="一句话介绍这个分类"
          />
        </el-form-item>
        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
          <span class="field-tip">越小越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存分类</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createCategory, deleteCategory, getCategories, updateCategory } from '@/api/category'

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()

const form = reactive({
  name: '',
  description: '',
  sort: 0
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '不能超过 50 个字符', trigger: 'blur' }
  ],
  sort: [{ required: true, message: '请输入排序值', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    categories.value = await getCategories()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.description = ''
  form.sort = 0
  formRef.value?.clearValidate()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
  form.description = row.description || ''
  form.sort = row.sort ?? 0
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, form)
      ElMessage.success('分类已更新')
    } else {
      await createCategory(form)
      ElMessage.success('分类已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '删除分类', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteCategory(row.id)
  ElMessage.success('分类已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.insight-row {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.insight-card {
  padding: 20px 22px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(58, 83, 58, 0.08);
}

.insight-card span {
  font-size: 11px;
  letter-spacing: 0.08em;
  color: #8c9987;
}

.insight-card strong {
  display: inline-block;
  margin: 8px 8px 0 0;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 34px;
  font-weight: 400;
  color: #345438;
}

.insight-card small {
  font-size: 11px;
  color: #9aa694;
}

.insight-card.wide {
  display: flex;
  flex-direction: column;
  justify-content: center;
  background:
    radial-gradient(circle at 90% 20%, rgba(213, 230, 198, 0.8), transparent 40%),
    rgba(255, 255, 255, 0.68);
}

.insight-card.wide p {
  margin: 7px 0 0;
  font-size: 12px;
  line-height: 1.8;
  color: #72816c;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-name strong {
  font-size: 13px;
  font-weight: 600;
  color: #2a3f2d;
}

.category-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #93b882;
  box-shadow: 0 0 0 4px rgba(147, 184, 130, 0.12);
}

.sort-pill {
  display: inline-grid;
  min-width: 34px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 11px;
  color: #5d7358;
  background: #eef4e9;
}

.field-tip {
  margin-left: 10px;
  font-size: 11px;
  color: #9ba796;
}

@media (max-width: 720px) {
  .insight-row {
    grid-template-columns: 1fr;
  }
}
</style>
