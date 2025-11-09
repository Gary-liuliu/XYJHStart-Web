<template>
  <el-container class="layout-container">
    <el-aside v-if="!isMobile" :width="isCollapsed ? '64px' : '200px'" class="layout-aside">
      <el-menu
        :default-active="$route.path"
        class="layout-menu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="logo-area">
          <span v-if="!isCollapsed">XYJH 管理后台</span>
        </div>
        
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-menu-item index="/pending-licenses">
          <el-icon><Clock /></el-icon>
          <span>待审核模块</span>
        </el-menu-item>
        
        <el-menu-item index="/manage-licenses">
          <el-icon><Files /></el-icon>
          <span>许可证维护</span>
        </el-menu-item>
        <!-- 新增：管理员维护 -->
        <el-menu-item index="/manage-admins">
          <el-icon><UserFilled /></el-icon>
          <span>管理员维护</span>
        </el-menu-item>
           <!-- 新增：许可证续期 -->
        <el-menu-item index="/renew-licenses">
          <el-icon><RefreshRight /></el-icon>
          <span>许可证续期</span>
        </el-menu-item>
      </el-menu>
      
    </el-aside>

    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      :with-header="false"
      size="200px"
      custom-class="mobile-drawer"
    >
      <el-menu
        :default-active="$route.path"
        class="layout-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="logo-area">
          <span>XYJH 管理后台</span>
        </div>
        
        <el-menu-item index="/" @click="drawerVisible = false">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-menu-item index="/pending-licenses" @click="drawerVisible = false">
          <el-icon><Clock /></el-icon>
          <span>待审核模块</span>
        </el-menu-item>
        
        <el-menu-item index="/manage-licenses" @click="drawerVisible = false">
          <el-icon><Files /></el-icon>
          <span>许可证维护</span>
        </el-menu-item>
          <!-- 新增：管理员维护（移动抽屉） -->
        <el-menu-item index="/manage-admins" @click="drawerVisible = false">
          <el-icon><UserFilled /></el-icon>
          <span>管理员维护</span>
        </el-menu-item>
         <!-- 新增：许可证续期（移动抽屉） -->
        <el-menu-item index="/renew-licenses" @click="drawerVisible = false">
          <el-icon><RefreshRight /></el-icon>
          <span>许可证续期</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <el-container>
     <el-header class="layout-header">
        <div class="header-left">
          
          <div v-if="isMobile" @click="drawerVisible = true" class="hamburger-button">
            <span class="bar"></span>
            <span class="bar"></span>
            <span class="bar"></span>
          </div>

          <el-icon v-else @click="isCollapsed = !isCollapsed" class="collapse-icon">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
        </div>
        
        <div class="header-right">
          <el-dropdown>
            <span class="user-avatar">
              <span>admin</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
      
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useAuthStore } from '../store/auth';
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core';

// 【已修复】: 明确导入 Header 中需要用到的所有图标
// 即使 main.js 注册了，这里也导入一次，确保万无一失
import { 
 
  Expand, 
  Fold, 
  ArrowDown, 
  HomeFilled, 
  Clock, 
  Files,
  UserFilled,
  RefreshRight
} from '@element-plus/icons-vue';

const authStore = useAuthStore();

// PC端侧边栏折叠状态
const isCollapsed = ref(false); 
// 移动端抽屉可见状态
const drawerVisible = ref(false);

// 使用 VueUse 来创建响应式断点
const breakpoints = useBreakpoints(breakpointsTailwind);
// 创建一个计算属性，当屏幕宽度小于 md (768px) 断点时，isMobile 为 true
const isMobile = breakpoints.smaller('md');

const handleLogout = () => {
  authStore.logout();
};
</script>



<style scoped>
/* 【组件局部样式】
  带 "scoped" 属性。
  这里放你所有的 .layout-container, .hamburger-button 等样式。
*/

/* 确保布局撑满全屏 */
.layout-container {
  height: 100vh;
  width: 100%;        /* 修复：避免 100vw 导致水平溢出产生右侧白边 */
  overflow-x: hidden; /* 保险：关闭水平滚动 */
}

.layout-aside {
  background-color: #304156;
  transition: width 0.28s;
  overflow-x: hidden; /* 防止折叠时内容溢出 */
}

/* 修复 el-menu 边框问题 */
.layout-menu {
  height: 100%;
  border-right: none;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap; /* 防止文字换行 */
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  padding: 0 20px;
}

.collapse-icon {
  font-size: 24px;
  cursor: pointer;
  display: flex; 
  align-items: center;
}

/* 【新增】: 自定义CSS汉堡按钮样式 */
.hamburger-button {
  width: 24px; /* 匹配 .collapse-icon 的字体大小 */
  height: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-around; /* 均匀分布三条杠 */
  cursor: pointer;
  padding: 3px 0; /* 垂直内边距，让杠更聚拢 */
  box-sizing: border-box; /* 确保 padding 不会撑大 24px 的高度 */
}

.hamburger-button .bar {
  width: 100%;
  height: 3px; /* 杠的粗细 */
  background-color: #5a5e66; /* 按钮颜色 (Element Plus 的 header 字体颜色) */
  border-radius: 1px;
}

/* 【新增】: 确保 .header-left 容器垂直居中 */
.header-left {
  display: flex;
  align-items: center;
  height: 100%;
}

.user-avatar {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>