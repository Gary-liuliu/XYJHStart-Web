<template>
  <el-card>
    <template #header>
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <span>许可证维护</span>
        <el-button size="small" type="success" @click="openBatchDialog">批量新增空许可证</el-button>
      </div>
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
      <el-table-column prop="status" label="状态" width="110">
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

      <!-- 新增：操作列（备注） -->
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="openNote(row)"
          >备注</el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <!-- 批量新增空许可证弹框（手机端适配） -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量新增空许可证"
      :width="isMobile ? '92%' : '420px'"
    >
      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="batchRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="100px"
      >
        <el-form-item label="数量" prop="count">
          <el-input-number
            v-model="batchForm.count"
            :min="1"
            :max="1000"
            :step="1"
            controls-position="right"
            :size="isMobile ? 'small' : 'default'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="batchDialogVisible = false"
            :disabled="batchSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitBatch"
            :loading="batchSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >确认创建</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增：备注编辑弹框（手机端适配） -->
    <el-dialog
      v-model="noteDialogVisible"
      title="编辑备注"
      :width="isMobile ? '92%' : '420px'"
    >
      <el-form
        ref="noteFormRef"
        :model="noteForm"
        :rules="noteRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="100px"
      >
        <el-form-item label="备注" prop="tipCustomer">
          <el-input
            v-model="noteForm.tipCustomer"
            type="textarea"
            :rows="isMobile ? 3 : 4"
            placeholder="请输入备注（会保留原有内容）"
            :size="isMobile ? 'small' : 'default'"
            autosize
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="noteDialogVisible = false"
            :disabled="noteSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitNote"
            :loading="noteSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >保存</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import api from '../utils/api'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { ElMessage } from 'element-plus'

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
    const res = await api.get('/api/admin/licenses', {
      params: { page: Math.max(page.value - 1, 0), size: pageSize.value }
    })
    tableData.value = res.content || []
    total.value = res.totalElements || 0
  } catch (e) {
    console.error('加载许可证失败:', e)
  } finally {
    loading.value = false
  }
}
const handleSizeChange = (val) => {
  pageSize.value = val
  page.value = 1
  loadData()
}

const handleCurrentChange = (val) => {
  page.value = val
  loadData()
}

const statusType = (status) => {
  switch (status) {
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

// 批量新增空许可证弹框与表单
const batchDialogVisible = ref(false)
const batchSubmitting = ref(false)
const batchFormRef = ref(null)
const batchForm = reactive({ count: 1 })
const batchRules = {
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}
const openBatchDialog = () => {
  batchForm.count = 1
  batchDialogVisible.value = true
}
const submitBatch = async () => {
  if (!batchFormRef.value) return
  await batchFormRef.value.validate(async (valid) => {
    if (!valid) return
    batchSubmitting.value = true
    try {
      await api.post('/api/admin/licenses/batch', { count: batchForm.count })
      ElMessage.success('批量创建成功')
      batchDialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '创建失败')
    } finally {
      batchSubmitting.value = false
    }
  })
}

// 新增：备注弹框与逻辑
const noteDialogVisible = ref(false)
const noteSubmitting = ref(false)
const noteTargetId = ref(null)
const noteFormRef = ref(null)
const noteForm = reactive({ tipCustomer: '' })
const noteRules = {
  tipCustomer: [
    { required: true, message: '请输入备注', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value || value.trim().length === 0) {
          callback(new Error('备注不能为空'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
const openNote = (row) => {
  noteTargetId.value = row.id
  // 保留原有备注
  noteForm.tipCustomer = row.tipCustomer ?? ''
  noteDialogVisible.value = true
}
const submitNote = async () => {
  if (!noteFormRef.value) return
  await noteFormRef.value.validate(async (valid) => {
    if (!valid) return
    noteSubmitting.value = true
    try {
      await api.put(`/api/admin/licenses/${noteTargetId.value}/note`, {
        tipCustomer: noteForm.tipCustomer.trim()
      })
      ElMessage.success('备注更新成功')
      noteDialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error(e.message || '备注更新失败')
    } finally {
      noteSubmitting.value = false
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