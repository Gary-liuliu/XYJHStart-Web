<template>
  <el-container class="login-container">
    <el-main>
      <el-row justify="center" align="middle" style="height: 100%;">
        
        <el-col :xs="22" :sm="12" :md="8" :lg="6">
          <el-card shadow="always" class="login-card">
            
            <template #header>
              <div class="card-header">
                <span>管理员登录</span>
              </div>
            </template>
            
            <el-form 
              ref="loginFormRef" 
              :model="loginForm" 
              :rules="loginRules" 
              label-position="top"
              @submit.prevent="handleLogin"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" />
              </el-form-item>
              
              <el-form-item label="密码" prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password />
              </el-form-item>

              <el-form-item label="验证码" prop="captcha">
                <el-row :gutter="10" style="width: 100%;">
                  <el-col :span="14">
                    <el-input v-model="loginForm.captcha" placeholder="请输入右侧验证码" size="large" />
                  </el-col>
                  <el-col :span="10">
                    <div class="captcha-box" @click="refreshCaptcha" title="点击刷新">
                      {{ captchaCode }}
                    </div>
                  </el-col>
                </el-row>
              </el-form-item>

              <el-form-item>
                <el-button 
                  type="primary" 
                  native-type="submit" 
                  size="large" 
                  style="width: 100%;"
                  :loading="loading"
                >
                  登 录
                </el-button>
              </el-form-item>
              
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useAuthStore } from '../store/auth';
import { ElMessage } from 'element-plus'; // 导入消息提示
// 1. 导入 useRouter
import { useRouter } from 'vue-router';
// --- 状态和存储 ---
const authStore = useAuthStore();
const loginFormRef = ref(null); // 表单引用
const loading = ref(false); // 加载状态
// 2. 【添加这一行！】: 调用 useRouter() 来获取 router 实例
const router = useRouter();
// --- 登录表单数据 ---
const loginForm = reactive({
  username: 'admin',
  password: '',
  captcha: ''
});

// --- 登录表单验证规则 ---
const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
});

// --- 防刷验证码 ---
const captchaCode = ref('');

const refreshCaptcha = () => {
  // 生成一个4位随机数字验证码
  captchaCode.value = Math.floor(1000 + Math.random() * 9000).toString();
};

onMounted(() => {
  refreshCaptcha(); // 页面加载时生成
});

// --- 登录处理 (核心修改) ---
const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      if (loginForm.captcha !== captchaCode.value) {
        ElMessage.error('验证码错误！');
        refreshCaptcha();
        return;
      }
      
      loading.value = true;
      try {
        await authStore.login(loginForm.username, loginForm.password);
        
        // 3. 登录成功！在这里执行跳转
        router.push({ name: 'Home' });
        
        // 4. 【修复Bug】使用 .success (绿色) 而不是 .error (红色)
        ElMessage.success('登录成功！');
        
      } catch (error) {
        ElMessage.error(error.message || '用户名或密码错误');
        refreshCaptcha(); 
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  background-color: #f0f2f5;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-card {
  border-radius: 10px;
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
}

.captcha-box {
  width: 100%;
  height: 40px; /* 与 el-input large size 保持一致 */
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f4f4f5;
  color: #333;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 4px; /* 增加字母间距 */
  cursor: pointer;
  border-radius: 4px;
  user-select: none; /* 防止用户选中文字 */
}
</style>