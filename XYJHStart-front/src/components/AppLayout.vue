<template>
  <el-container class="layout-container">
    <el-aside v-if="!isMobile" width="220px" class="layout-aside">
      <el-menu :default-active="$route.path" class="layout-menu" router>
        <div class="logo-area">XYJH 管理后台</div>
        <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
      <button class="logout-button" type="button" @click="handleLogout">退出登录</button>
    </el-aside>

    <el-drawer v-model="drawerVisible" direction="ltr" :with-header="false" size="220px" custom-class="mobile-drawer">
      <el-menu :default-active="$route.path" class="layout-menu" router>
        <div class="logo-area">XYJH 管理后台</div>
        <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index" @click="drawerVisible = false">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <el-container class="content-container">
      <el-header v-if="isMobile" class="mobile-header">
        <button class="icon-button" type="button" aria-label="打开菜单" @click="drawerVisible = true">
          <el-icon><Expand /></el-icon>
        </button>
        <span>XYJH</span>
        <button class="mobile-logout" type="button" @click="handleLogout">退出</button>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { Clock, Expand, Files, RefreshRight, User, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()
const router = useRouter()
const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('md')
const drawerVisible = ref(false)

const menuItems = [
  { index: '/', label: '账号买卖', icon: User },
  { index: '/pending-licenses', label: '待审核模块', icon: Clock },
  { index: '/manage-licenses', label: '许可证维护', icon: Files },
  { index: '/manage-admins', label: '管理员维护', icon: UserFilled },
  { index: '/renew-licenses', label: '许可证续期', icon: RefreshRight }
]

const handleLogout = () => {
  authStore.logout(router)
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: #edf2f8;
}

.layout-aside {
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  overflow: hidden;
  background: #111827;
  border-right: 1px solid #1f2937;
}

.layout-menu {
  height: 100%;
  border-right: 0;
  background: transparent;
}

.logo-area {
  display: flex;
  align-items: center;
  height: 66px;
  padding: 0 20px;
  color: #fff;
  font-size: 16px;
  font-weight: 800;
}

.logout-button {
  height: 42px;
  margin: 12px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 8px;
  color: #cbd5e1;
  background: rgba(255, 255, 255, 0.06);
  cursor: pointer;
}

.content-container {
  min-width: 0;
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 10px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
}

.mobile-header span {
  color: #111827;
  font-weight: 800;
}

.icon-button,
.mobile-logout {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  border: 1px solid #d8e1ec;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  cursor: pointer;
}

.icon-button {
  width: 36px;
}

.mobile-logout {
  padding: 0 10px;
}

.layout-main {
  min-width: 0;
  overflow: auto;
  padding: 16px;
  background: transparent;
}

:deep(.layout-menu .el-menu-item) {
  height: 44px;
  margin: 6px 10px;
  border-radius: 8px;
  color: #cbd5e1;
}

:deep(.layout-menu .el-menu-item.is-active) {
  background: #2563eb;
  color: #fff;
}

:deep(.layout-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

@media (max-width: 767px) {
  .layout-container {
    overflow: hidden;
  }

  .layout-main {
    overflow: hidden;
    padding: 0;
  }
}
</style>
