<!-- ... existing code ... -->
<template>
  <el-card>
    <template #header>
      <!-- Deleted:<div style="display:flex;align-items:center;justify-content:space-between;">
      Deleted:  <span>管理员账号维护</span>
      Deleted:  <el-button size="small" type="primary" @click="loadData" :loading="loading">刷新</el-button>
      Deleted:</div> -->
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <span>管理员账号维护</span>
        <div style="display:flex;gap:8px;">
              <el-button size="small" type="success" @click="openCreateAdmin">新增管理员</el-button>
          <el-button size="small" type="warning" @click="openChangePwd">修改当前账号密码</el-button>
          <el-button size="small" type="primary" @click="loadData" :loading="loading">刷新</el-button>
        </div>
      </div>
    </template>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="180" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="roleType(row.role)">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="180">
        <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
      </el-table-column>

<!-- 新增：操作列（删除） -->
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-popconfirm
            title="确认删除该管理员？"
            confirm-button-text="确认"
            cancel-button-text="取消"
            @confirm="deleteUser(row.id)"
          >
            <template #reference>
              <el-button
                type="danger"
                size="small"
                :loading="deleteLoadingId === row.id"
              >删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>

    </el-table>

    <!-- 修改密码弹框 -->
    <!-- Deleted:<el-dialog v-model="pwdDialogVisible" title="修改当前账号密码" width="420px"> -->
    <!-- Deleted:<el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px"> -->
    <el-dialog
      v-model="pwdDialogVisible"
      title="修改当前账号密码"
      :width="isMobile ? '92%' : '420px'"
    >
      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="100px"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <!-- Deleted:<el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" /> -->
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入旧密码"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <!-- Deleted:<el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" /> -->
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（至少6位）"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmNewPassword">
          <!-- Deleted:<el-input v-model="pwdForm.confirmNewPassword" type="password" show-password placeholder="请再次输入新密码" /> -->
          <el-input
            v-model="pwdForm.confirmNewPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <!-- Deleted:<div style="text-align:right;">
        Deleted:  <el-button @click="pwdDialogVisible = false" :disabled="pwdSubmitting">取消</el-button>
        Deleted:  <el-button type="primary" @click="submitChangePwd" :loading="pwdSubmitting">确认修改</el-button>
        Deleted:</div> -->
        <div style="text-align:right;">
          <el-button
            @click="pwdDialogVisible = false"
            :disabled="pwdSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitChangePwd"
            :loading="pwdSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >确认修改</el-button>
        </div>
      </template>
    </el-dialog>

     <!-- 新增管理员弹框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新增管理员"
      :width="isMobile ? '92%' : '420px'"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="createForm.username"
            placeholder="请输入用户名"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="createForm.password"
            type="password"
            show-password
            placeholder="请输入密码（至少6位）"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="createDialogVisible = false"
            :disabled="createSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitCreateAdmin"
            :loading="createSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >确认创建</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted,reactive } from 'vue'
import api from '../utils/api'
import { ElMessage } from 'element-plus'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { useRouter } from 'vue-router'

const router = useRouter()

const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('md')
const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    // 相对路径，自动携带 Authorization: Bearer <token>
    const res = await api.get('/api/admin/users')
    tableData.value = Array.isArray(res) ? res : []
  } catch (e) {
    if (e.response && e.response.status === 403) {
      ElMessage.error('权限不足或登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(e.message || '服务器错误，无法查询管理员列表')
    }
  } finally {
    loading.value = false
  }
}

const roleType = (role) => {
  switch (role) {
    case 'ADMIN': return 'success'
    case 'EDITOR': return 'warning'
    default: return ''
  }
}

const formatDate = (str) => {
  if (!str) return '-'
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
//删除管理员账号

const deleteLoadingId = ref(null)
const deleteUser = async (id) => {
  if (!id) return
  deleteLoadingId.value = id
  try {
    await api.delete(`/api/admin/users/${id}`)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e) {
    if (e.response && e.response.status === 403) {
      ElMessage.error('权限不足或登录已过期，请重新登录')
      router.push('/login')
    } else {
      // 后端可能返回：不能删除自己(400)、用户不存在(404)
      ElMessage.error(e.message || '删除失败')
    }
  } finally {
    deleteLoadingId.value = null
  }
}
//新增管理员
// 新增管理员弹框与表单
const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  username: '',
  password: ''
})
const createRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}
const openCreateAdmin = () => {
  createForm.username = ''
  createForm.password = ''
  createDialogVisible.value = true
}
const submitCreateAdmin = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createSubmitting.value = true
    try {
      const res = await api.post('/api/admin/users', {
        username: createForm.username,
        password: createForm.password
      })
      ElMessage.success(res?.message || '新管理员创建成功')
      createDialogVisible.value = false
      await loadData()
    } catch (e) {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        // 可能返回409：用户名已存在
        ElMessage.error(e.message || '创建失败')
      }
    } finally {
      createSubmitting.value = false
    }
  })
}
// 修改密码弹框与表单
const pwdDialogVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: ''
})
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}
const openChangePwd = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmNewPassword = ''
  pwdDialogVisible.value = true
}
const submitChangePwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSubmitting.value = true
    try {
      await api.put('/api/admin/users/password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功')
      pwdDialogVisible.value = false
    } catch (e) {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        // 后端失败示例：code=400, message="旧密码错误"
        ElMessage.error(e.message || '修改失败')
      }
    } finally {
      pwdSubmitting.value = false
    }
  })
}

onMounted(loadData)
</script>
<!-- ... existing code ... -->