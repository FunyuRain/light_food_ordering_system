<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark"><span></span></div>
        <div>
          <p class="brand-kicker">LIGHT FOOD</p>
          <h1>轻食管理台</h1>
        </div>
      </div>

      <p class="nav-label">工作台</p>
      <el-menu
        :default-active="route.path"
        router
        class="side-menu"
        background-color="transparent"
        text-color="rgba(255,255,255,.72)"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/dishes">
          <el-icon><Dish /></el-icon>
          <span>菜品管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><CollectionTag /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-note">
        <span class="note-dot"></span>
        <div>
          <strong>门店营业中</strong>
          <p>轻食 · 新鲜 · 简单</p>
        </div>
      </div>
      <p class="sidebar-version">LIGHT FOOD ADMIN · 1.0</p>
    </aside>

    <div class="main">
      <header class="topbar">
        <div>
          <p class="topbar-eyebrow">LIGHT FOOD / OPERATIONS</p>
          <h2>{{ route.meta.title || '工作台' }}</h2>
        </div>
        <el-dropdown trigger="click" @command="onCommand">
          <button class="admin-user" type="button">
            <span class="avatar">{{ initial }}</span>
            <span class="admin-meta">
              <strong>{{ auth.displayName }}</strong>
              <small>管理员</small>
            </span>
            <el-icon><ArrowDown /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const initial = computed(() => (auth.displayName || '管').slice(0, 1))

async function onCommand(command) {
  if (command !== 'logout') return
  await ElMessageBox.confirm('确定要退出管理台吗？', '退出登录', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  })
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: flex;
  background:
    radial-gradient(circle at 82% 0%, rgba(214, 229, 200, 0.55), transparent 34%),
    #f3f5f0;
}

.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 246px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  padding: 28px 20px 20px;
  color: #f1f6ec;
  background:
    radial-gradient(circle at 15% 15%, rgba(157, 193, 139, 0.22), transparent 30%),
    linear-gradient(168deg, #1e3324 0%, #294632 58%, #223b2b 100%);
  box-shadow: 12px 0 40px rgba(31, 55, 39, 0.12);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 15px;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #d5e9c7, #8fb77e);
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.18);
}

.brand-mark span {
  width: 18px;
  height: 25px;
  border-radius: 18px 4px 18px 4px;
  background: #25402e;
  transform: rotate(35deg);
}

.brand-kicker {
  margin: 0 0 3px;
  font-size: 9px;
  letter-spacing: 0.24em;
  color: rgba(224, 240, 213, 0.62);
}

.brand h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.nav-label {
  margin: 46px 12px 12px;
  font-size: 10px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: rgba(234, 246, 226, 0.42);
}

.side-menu {
  border-right: 0;
}

.side-menu :deep(.el-menu-item) {
  height: 48px;
  margin: 5px 0;
  border-radius: 13px;
  font-size: 14px;
  letter-spacing: 0.04em;
  transition: background 0.18s ease, color 0.18s ease;
}

.side-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(100deg, rgba(206, 231, 191, 0.23), rgba(255, 255, 255, 0.07));
  box-shadow: inset 0 0 0 1px rgba(213, 235, 199, 0.12);
}

.sidebar-note {
  margin-top: auto;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.note-dot {
  width: 8px;
  height: 8px;
  margin-top: 5px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #b9df97;
  box-shadow: 0 0 0 5px rgba(185, 223, 151, 0.12);
}

.sidebar-note strong {
  font-size: 12px;
  font-weight: 600;
}

.sidebar-note p {
  margin: 4px 0 0;
  font-size: 11px;
  color: rgba(236, 246, 229, 0.48);
}

.sidebar-version {
  margin: 18px 4px 0;
  font-size: 9px;
  letter-spacing: 0.16em;
  color: rgba(255, 255, 255, 0.26);
}

.main {
  width: calc(100% - 246px);
  margin-left: 246px;
  min-width: 0;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 8;
  height: 96px;
  padding: 0 34px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(243, 245, 240, 0.82);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(54, 79, 57, 0.07);
}

.topbar-eyebrow {
  margin: 0 0 5px;
  font-size: 9px;
  letter-spacing: 0.24em;
  color: #8b9988;
}

.topbar h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #203423;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px 8px 8px;
  border: 1px solid rgba(58, 83, 58, 0.1);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  color: #2f4632;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.admin-user:hover {
  border-color: rgba(72, 111, 68, 0.3);
  box-shadow: 0 8px 20px rgba(45, 72, 44, 0.08);
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #eff6e9;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(145deg, #6d9362, #36563a);
}

.admin-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.15;
}

.admin-meta strong {
  font-size: 12px;
  font-weight: 600;
}

.admin-meta small {
  margin-top: 2px;
  font-size: 10px;
  color: #8a9887;
}

.content {
  padding: 28px 34px 56px;
  max-width: 1520px;
}

@media (max-width: 1100px) {
  .sidebar {
    width: 210px;
  }

  .main {
    width: calc(100% - 210px);
    margin-left: 210px;
  }

  .topbar,
  .content {
    padding-left: 22px;
    padding-right: 22px;
  }
}
</style>
