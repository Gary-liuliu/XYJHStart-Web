<template>
  <el-card>
    <template #header>
      <div>待审核许可证</div>
    </template>

    <!-- 手机模式：上方紧凑的每页条数选择器与总数 -->
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
      <el-table-column prop="status" label="状态" width="140">
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
      <!-- 新增：审核操作列 -->
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="openApprove(row)"
          >审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增：审核输入弹框（手机端适配） -->
    <el-dialog
      v-model="approveDialogVisible"
      title="批准使用"
      :width="isMobile ? '92%' : '420px'"
    >
      <el-form
        ref="approveFormRef"
        :model="approveForm"
        :rules="approveRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="100px"
      >
        <el-form-item label="使用天数（必填）" prop="durationInDays">
          <el-input-number
            v-model="approveForm.durationInDays"
            :min="1"
            :max="3650"
            :step="1"
            controls-position="right"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
        <el-form-item label="备注（可选）" prop="tipCustomer">
          <el-input
            v-model="approveForm.tipCustomer"
            type="textarea"
            :rows="isMobile ? 3 : 4"
            placeholder="请输入批准备注（将保存原有内容）"
            :size="isMobile ? 'small' : 'default'"
            autosize
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="approveDialogVisible = false"
            :disabled="approveSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitApprove"
            :loading="approveSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >确认批准</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 手机模式：粘底、仅 prev/pager/next，small，避免挤出页面 -->
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

    <!-- 桌面模式：完整布局与页尺寸选择 -->
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
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])

const page = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 根据屏幕宽度动态判断是否为手机模式
const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('md')

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/admin/licenses/pending/paginated', {
      // 后端0基分页：第一页应传 page=0
      params: { page: Math.max(page.value - 1, 0), size: pageSize.value }
    })
    tableData.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    if (e.response && e.response.status === 403) {
      ElMessage.error('权限不足或登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(e.message || '加载待审核许可证失败')
    }
  } finally {
    loading.value = false
  }
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
    case 'PENDING_APPROVAL': return 'warning'
    case 'AVAILABLE': return 'info'
    case 'ACTIVATED': return 'success'
    case 'REVOKED': return 'danger'
    case 'EXPIRED': return 'warning'
    default: return ''
  }
}

const formatDate = (str) => {
  if (!str) return '-'
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 审核弹框状态与表单
const approveDialogVisible = ref(false)
const approveSubmitting = ref(false)
const approveTargetId = ref(null)
const approveFormRef = ref(null)
const approveForm = reactive({
  durationInDays: 1,
  tipCustomer: ''
})
const approveRules = {
  durationInDays: [
    { required: true, message: '请输入使用天数', trigger: 'change' },
    { type: 'number', min: 1, message: '使用天数必须大于0', trigger: 'change' }
  ]
  // tipCustomer 可选，无需必填校验
}
const openApprove = (row) => {
  approveTargetId.value = row.id
  approveForm.durationInDays = 1
  // 打开时保留原有备注内容
  approveForm.tipCustomer = row.tipCustomer ?? ''
  approveDialogVisible.value = true
}
const submitApprove = async () => {
  if (!approveFormRef.value) return
  await approveFormRef.value.validate(async (valid) => {
    if (!valid) return
    approveSubmitting.value = true
    try {
      const payload = {
        durationInDays: approveForm.durationInDays,
        tipCustomer: approveForm.tipCustomer?.trim() || ''
      }
      await api.put(`/api/admin/licenses/${approveTargetId.value}/approve`, payload)
      ElMessage.success('批准成功')
      approveDialogVisible.value = false
      loadData()
    } catch (e) {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error(e.message || '批准失败')
      }
    } finally {
      approveSubmitting.value = false
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
</style>