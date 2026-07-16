<template>
  <section ref="loginRoot" class="login-screen">
    <img class="login-background" :src="loginBackground" alt="" />
    <div class="login-stage">
      <div class="login-orbit orbit-a"></div>
      <div class="login-orbit orbit-b"></div>
      <div class="login-grid"></div>

      <div class="login-hero">
        <p class="eyebrow">星陨账号管理后台</p>
        <h1>账号系统登录</h1>
        <p class="hero-copy">账号、发货、记账，统一进入后台处理。</p>
      </div>

      <el-card ref="cardRef" class="login-card" shadow="never">
        <div class="card-head">
          <div>
            <span>欢迎回来</span>
            <strong>管理员登录</strong>
          </div>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" label-position="top" @submit.prevent="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" size="large" placeholder="请输入密码" show-password />
          </el-form-item>

          <el-form-item label="验证码" prop="captcha">
            <div class="captcha-row">
              <el-input v-model="loginForm.captcha" size="large" placeholder="请输入右侧验证码" />
              <button class="captcha-box" type="button" @click="refreshCaptcha">
                {{ captchaCode }}
              </button>
            </div>
          </el-form-item>

          <el-button class="submit-button" type="primary" native-type="submit" size="large" :loading="loading">登录</el-button>
        </el-form>
      </el-card>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { gsap } from 'gsap'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import loginBackground from '../assets/image-20260616-234013-small.webp'

const authStore = useAuthStore()
const router = useRouter()
const loginRoot = ref(null)
const loginFormRef = ref(null)
const loading = ref(false)
const captchaCode = ref('')
const loginForm = reactive({
  username: 'admin',
  password: '',
  captcha: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

let loginContext

const refreshCaptcha = () => {
  captchaCode.value = Math.floor(1000 + Math.random() * 9000).toString()
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (loginForm.captcha !== captchaCode.value) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return
  }

  loading.value = true
  try {
    await authStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push({ name: 'Home' })
  } catch (error) {
    ElMessage.error(error.message || '用户名或密码错误')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
  loginContext = gsap.context(() => {
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (reduceMotion) return

    const entranceTimeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
    entranceTimeline
      .fromTo('.login-hero', {
        autoAlpha: 0,
        x: -28
      }, {
        autoAlpha: 1,
        x: 0,
        duration: 0.78
      })
      .fromTo('.login-card', {
        autoAlpha: 0,
        y: 24,
        scale: 0.96
      }, {
        autoAlpha: 1,
        y: 0,
        scale: 1,
        duration: 0.72
      }, '-=0.42')
      .fromTo('.login-card .el-form-item, .submit-button', {
        autoAlpha: 0,
        y: 10
      }, {
        autoAlpha: 1,
        y: 0,
        duration: 0.42,
        stagger: 0.06
      }, '-=0.22')

    gsap.to('.orbit-a', {
      rotation: 360,
      duration: 24,
      repeat: -1,
      ease: 'none'
    })

    gsap.to('.orbit-b', {
      rotation: -360,
      duration: 30,
      repeat: -1,
      ease: 'none'
    })

    gsap.to('.login-grid', {
      backgroundPosition: '0 64px',
      duration: 18,
      repeat: -1,
      ease: 'none'
    })
  }, loginRoot.value)
})

onUnmounted(() => {
  loginContext?.revert()
})
</script>

<style scoped>
.login-screen {
  min-height: 100vh;
  overflow: hidden;
  background: #eef3ff;
}

.login-background {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(1.03) contrast(1.03) brightness(0.72);
  transform: scale(1.02);
}

.login-screen::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(12, 16, 28, 0.22) 0%, rgba(12, 16, 28, 0.06) 48%, rgba(12, 16, 28, 0.48) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.04), rgba(12, 16, 28, 0.2));
  pointer-events: none;
}

.login-stage {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  align-items: center;
  gap: clamp(24px, 5vw, 68px);
  width: min(1320px, calc(100% - 40px));
  min-height: 100vh;
  margin: 0 auto;
  padding: 48px clamp(18px, 3vw, 42px);
}

.login-stage,
.login-background,
.login-screen::before {
  z-index: 0;
}

.login-hero {
  position: relative;
  z-index: 1;
  max-width: 680px;
  padding-left: clamp(4px, 1.4vw, 24px);
}

.eyebrow {
  margin: 0 0 10px;
  color: #ec4899;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.login-hero h1 {
  margin: 0;
  color: #ffffff;
  font-size: clamp(34px, 5vw, 60px);
  line-height: 1.02;
}

.hero-copy {
  max-width: 34rem;
  margin: 18px 0 0;
  color: rgba(255, 255, 255, 0.94);
  font-size: 16px;
  line-height: 1.7;
}

.login-card {
  position: relative;
  z-index: 1;
  justify-self: center;
  border: 1px solid rgba(244, 114, 182, 0.22);
  border-radius: 12px;
  background:
    linear-gradient(180deg, rgba(31, 27, 58, 0.98) 0%, rgba(18, 20, 48, 0.97) 48%, rgba(10, 18, 38, 0.98) 100%);
  box-shadow: 0 42px 110px rgba(42, 24, 70, 0.46), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  backdrop-filter: none;
  will-change: transform;
}

:deep(.login-card .el-card__body) {
  position: static;
  background:
    radial-gradient(circle at 16% 0%, rgba(244, 114, 182, 0.11), transparent 34%),
    radial-gradient(circle at 90% 12%, rgba(56, 189, 248, 0.1), transparent 32%);
}

.card-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 22px;
}

.card-head span {
  display: block;
  color: rgba(248, 187, 230, 0.78);
  font-size: 13px;
  font-weight: 700;
}

.card-head strong {
  display: block;
  margin-top: 4px;
  color: #fff7ff;
  font-size: 22px;
}

:deep(.login-card .el-form-item__label) {
  color: rgba(236, 244, 255, 0.9);
  font-weight: 700;
}

:deep(.login-card .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(196, 181, 253, 0.22);
  box-shadow: none;
}

:deep(.login-card .el-input__inner) {
  color: #f8fafc;
}

:deep(.login-card .el-input__inner::placeholder) {
  color: rgba(203, 213, 225, 0.65);
}

:deep(.login-card .el-input__prefix-inner),
:deep(.login-card .el-input__suffix-inner) {
  color: rgba(255, 255, 255, 0.72);
}

.login-form {
  display: grid;
  gap: 6px;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 116px;
  gap: 10px;
}

.captcha-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(244, 114, 182, 0.24);
  border-radius: 10px;
  color: #fff;
  background:
    linear-gradient(135deg, #38bdf8 0%, #818cf8 44%, #f472b6 100%);
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0;
  cursor: pointer;
  user-select: none;
}

.submit-button {
  width: 100%;
  margin-top: 8px;
  --el-button-bg-color: #a855f7;
  --el-button-border-color: #a855f7;
  --el-button-hover-bg-color: #f472b6;
  --el-button-hover-border-color: #f472b6;
  box-shadow: 0 12px 26px rgba(168, 85, 247, 0.28);
}

.login-orbit,
.login-grid {
  position: absolute;
  inset: auto;
  pointer-events: none;
}

.login-orbit {
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.28);
}

.orbit-a {
  right: 5vw;
  top: 10vh;
  width: 440px;
  height: 440px;
}

.orbit-b {
  right: 9vw;
  top: 18vh;
  width: 260px;
  height: 260px;
}

.login-grid {
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.12) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.28), transparent 82%);
}

@media (max-width: 900px) {
  .login-stage {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto;
    align-content: center;
    gap: 14px;
    padding: 14px 12px 18px;
    width: 100%;
  }

  .login-hero {
    max-width: none;
  }

  .login-hero h1 {
    font-size: 30px;
  }

  .hero-copy {
    margin-top: 12px;
    font-size: 14px;
    line-height: 1.55;
  }

  .login-card {
    width: min(100%, 420px);
    justify-self: center;
    border-radius: 14px;
  }

  .card-head {
    margin-bottom: 18px;
  }

  :deep(.login-card .el-form-item) {
    margin-bottom: 16px;
  }

  .captcha-row {
    grid-template-columns: minmax(0, 1fr) 96px;
  }

  .captcha-box {
    font-size: 20px;
  }

  .orbit-a,
  .orbit-b {
    display: none;
  }

  .login-grid {
    background-size: 36px 36px;
  }
}
</style>
