import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../utils/api'; // 导入我们的 API 客户端


export const useAuthStore = defineStore('auth', () => {
  
  // 1. State: 从 localStorage 初始化 token，实现“保持登录”
  const token = ref(localStorage.getItem('token') || null);

  // 2. Action: 登录
  async function login(username, password) {
    try {
      // 调用 API 登录接口
      const jwtToken = await api.post('/api/admin/users/login', {
        username: username,
        password: password
      });

      // 登录成功
      token.value = jwtToken;
      localStorage.setItem('token', jwtToken); // 存入 localStorage
      
      // 添加请求头 (为后续所有请求做准备)
      api.defaults.headers.common['Authorization'] = 'Bearer ' + jwtToken;
      console.log("登录成功:", jwtToken);


    } catch (error) {
      // 登录失败，抛出错误给组件处理
      console.error("登录失败:", error);
      throw error;
    }
  }

  // 3. Action: 登出
  function logout(router = null) {
    token.value = null;
    localStorage.removeItem('token');
    delete api.defaults.headers.common['Authorization'];
    
    // 如果提供了 router，则跳转到登录页
    if (router) {
      router.push('/login');
    }
  }

  // 4. Getter: (可选) 判断是否登录
  const isAuthenticated = () => !!token.value;

  return { token, login, logout, isAuthenticated };
});