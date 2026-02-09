import axios from 'axios';

// 1. 创建 Axios 实例
const api = axios.create({
  // 使用相对路径，配合 Vite 代理实现环境适配与跨域
  baseURL: '/', 
  timeout: 10000, // 请求超时时间
});

// 2. 响应拦截器 (用于统一处理后端返回的 Result 结构)
api.interceptors.response.use(
  (response) => {
    // 后端返回的结构是 { code: 0, message: "...", data: ... }
    const res = response.data;

    // 如果 code 不是 0，就抛出错误
    if (res.code !== 0) {
      // ElMessage 在 Login.vue 中使用，这里先用 alert 占位
      // ElMessage.error(res.message || 'Error'); 
      return Promise.reject(new Error(res.message || 'Error'));
    } else {
      // 如果 code 是 0，我们只关心 data 部分
      return res.data;
    }
  },
  (error) => {
    // 处理网络层面的错误 (e.g., 401, 404, 500)
    console.error('API Error: ' + error); 


    return Promise.reject(error);
  }
);
// 3. 【新增】请求拦截器 (自动附加 Token)
//    我们之前在 store/auth.js 中手动添加了 header，在这里统一处理更规范
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 在应用初始化时也检查是否有token并设置默认headers
const token = localStorage.getItem('token');
if (token) {
  api.defaults.headers.common['Authorization'] = 'Bearer ' + token;
}
export default api;