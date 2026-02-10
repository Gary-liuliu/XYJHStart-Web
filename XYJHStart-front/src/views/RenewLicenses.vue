<template>
  <el-card>
    <template #header>
      <div>许可证续期</div>
    </template>

    <div class="search-controls">
      <el-input
        v-model="searchTipCustomer"
        placeholder="输入备注进行搜索"
        style="max-width: 300px; margin-bottom: 16px;"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prepend>备注</template>
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <div class="list-controls" v-if="isMobile">
      <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
        <span>每页</span>
        <el-select v-model="pageSize" size="small" style="width:90px" @change="handleSizeChange">
          <el-option label="8" value="8" />
          <el-option label="16" value="16" />
          <el-option label="24" value="24" />
        </el-select>
        <span style="margin-left:auto">共 {{ total }} 条</span>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="licenseKey" label="密钥" min-width="220" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deviceId" label="设备ID" min-width="160" />
      <el-table-column label="激活时间" min-width="160">
        <template #default="{ row }">{{ formatDate(row.activationDate) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="160">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="160">
        <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column prop="tipCustomer" label="客户备注" min-width="220" show-overflow-tooltip />
      <el-table-column label="过期时间" min-width="160">
        <template #default="{ row }">{{ formatDate(row.expirationDate) }}</template>
      </el-table-column>

      <!-- 新增：续期操作列 -->
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="openRenew(row)"
          >续期</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增：续期输入弹框（手机端适配） -->
    <el-dialog
      v-model="renewDialogVisible"
      title="续期设置"
      :width="isMobile ? '92%' : '460px'"
    >
      <el-form
        ref="renewFormRef"
        :model="renewForm"
        :rules="renewRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="110px"
      >
        <el-form-item label="续期天数（必填）" prop="durationInDays">
          <el-input-number
            v-model="renewForm.durationInDays"
            :min="1"
            :max="3650"
            :step="1"
            controls-position="right"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
        <el-form-item label="备注（必填）" prop="tipCustomer">
          <el-input
            v-model="renewForm.tipCustomer"
            type="textarea"
            :rows="isMobile ? 3 : 4"
            placeholder="请输入客户备注（例如续期原因等）"
            :size="isMobile ? 'small' : 'default'"
            autosize
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="renewDialogVisible = false"
            :disabled="renewSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitRenew"
            :loading="renewSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >确认续期</el-button>
        </div>
      </template>
    </el-dialog>

    <div v-if="isMobile" class="mobile-pagination">
      <el-pagination
        background
        small
        :current-page="page"
        :page-size="pageSize"
        layout="prev, pager, next"
        :total="total"
        @current-change="handleCurrentChange"
      />
    </div>

    <div v-else style="margin-top: 12px; display: flex; justify-content: flex-end;">
      <el-pagination
        background
        :current-page="page"
        :page-size="pageSize"
        :page-sizes="[8, 16, 24]"
        layout="sizes, prev, pager, next, jumper, total"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import api from '../utils/api'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])

const page = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 添加搜索相关变量
const searchTipCustomer = ref('')

const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('md')

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: Math.max(page.value - 1, 0),
      size: pageSize.value
    }
    
    // 如果搜索条件存在，则添加到参数中
    if (searchTipCustomer.value) {
      params.tipCustomer = searchTipCustomer.value
    }
    
    const res = await api.get('/api/admin/licenses/expired', { params })
    tableData.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    if (e.response && e.response.status === 403) {
      ElMessage.error('权限不足或登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(e.message || '加载过期许可证失败')
    }
  } finally {
    loading.value = false
  }
}

// 处理搜索事件
const handleSearch = () => {
  page.value = 1 // 搜索时回到第一页
  loadData()
}

const handleSizeChange = (val) => {
  pageSize.value = Number(val)
  page.value = 1
  loadData()
}

const handleCurrentChange = (val) => {
  page.value = val
  loadData()
}

const statusType = (status) => {
  switch (status) {
    case 'EXPIRED': return 'warning'
    case 'AVAILABLE': return 'info'
    case 'ACTIVATED': return 'success'
    case 'REVOKED': return 'danger'
    default: return ''
  }
}

const formatDate = (str) => {
  if (!str) return '-'
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 续期弹框与逻辑
const renewDialogVisible = ref(false)
const renewSubmitting = ref(false)
const renewTargetId = ref(null)
const renewFormRef = ref(null)
const renewForm = reactive({
  durationInDays: 30,
  tipCustomer: ''
})
// 校验：两个字段都必填，天数必须 > 0，备注非空（去除前后空格）
const validateTipNotEmpty = (rule, value, callback) => {
  if (!value || value.trim().length === 0) {
    callback(new Error('请输入备注'))
  } else {
    callback()
  }
}
const renewRules = {
  durationInDays: [
    { required: true, message: '请输入续期天数', trigger: 'change' },
    { type: 'number', min: 1, message: '续期天数必须大于0', trigger: 'change' },
  ],
  tipCustomer: [
    { required: true, message: '请输入备注', trigger: 'blur' },
    { validator: validateTipNotEmpty, trigger: 'blur' },
  ],
}

const openRenew = (row) => {
  renewTargetId.value = row.id
  renewForm.durationInDays = 30
  // 打开弹框时保留原有备注
  renewForm.tipCustomer = row.tipCustomer ?? ''
  renewDialogVisible.value = true
}

const submitRenew = async () => {
  if (!renewFormRef.value) return
  await renewFormRef.value.validate(async (valid) => {
    if (!valid) return
    renewSubmitting.value = true
    try {
      const payload = {
        durationInDays: renewForm.durationInDays,
        tipCustomer: renewForm.tipCustomer.trim(),
      }
      await api.put(`/api/admin/licenses/${renewTargetId.value}/renew`, payload)
      ElMessage.success('更新成功')
      renewDialogVisible.value = false
      loadData()
    } catch (e) {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error(e.message || '更新失败')
      }
    } finally {
      renewSubmitting.value = false
    }
  })
}

onMounted(loadData)
</script>

<style scoped>
.mobile-pagination {
  position: sticky;
  bottom: 0;
  background: #fff;
  padding: 8px 12px;
  border-top: 1px solid #ebeef5;
  z-index: 10;
}
.list-controls {
  margin-bottom: 8px;
}
.search-controls {
  margin-bottom: 16px;
}
</style>