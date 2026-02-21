<template>
  <el-card>
    <template #header>
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <span>账号买卖管理</span>
        <el-button size="small" type="success" @click="openCreateDialog">新增账号</el-button>
      </div>
    </template>

    <div class="search-controls">
      <el-input
        v-model="searchKeyword"
        placeholder="输入关键词搜索账号"
        style="max-width: 300px; margin-bottom: 16px;"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

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
      :row-class-name="tableRowClassName"
      :row-key="getRowKey"
      style="width: 100%"
    >
      <el-table-column prop="accountName" label="账号名" min-width="120" />
      <el-table-column prop="account" label="账号" min-width="160" show-overflow-tooltip />
      <el-table-column prop="password" label="密码" min-width="120">
        <template #default="{ row }">
          <span>{{ row.password || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="buyTime" label="购入时间" min-width="160">
        <template #default="{ row }">{{ formatDate(row.buyTime) }}</template>
      </el-table-column>
      <el-table-column prop="buyPrice" label="购入价格" min-width="100">
        <template #default="{ row }">{{ row.buyPrice ? `¥${row.buyPrice}` : '-' }}</template>
      </el-table-column>
      <el-table-column prop="greenTicket" label="绿票" min-width="100">
        <template #default="{ row }">
          <span v-if="editingTicket.id !== row.id || editingTicket.type !== 'green'" 
                @dblclick="startEditingTicket(row, 'green')"
                style="cursor: pointer;">
            {{ row.greenTicket }}
            <el-icon><EditPen /></el-icon>
          </span>
          <el-input v-else
                    v-model="editingTicket.value"
                    size="small"
                    ref="greenTicketInput"
                    @blur="saveTicketValue(editingTicket.id, 'green', editingTicket.value); editingTicket.id = null"
                    @keyup.enter="saveTicketValue(editingTicket.id, 'green', editingTicket.value); editingTicket.id = null"
                    @keyup.escape="editingTicket.id = null; row.greenTicket"
                    @focus="$event.target.select()" />
        </template>
      </el-table-column>
      <el-table-column prop="yellowTicket" label="黄票" min-width="100">
        <template #default="{ row }">
          <span v-if="editingTicket.id !== row.id || editingTicket.type !== 'yellow'" 
                @dblclick="startEditingTicket(row, 'yellow')"
                style="cursor: pointer;">
            {{ row.yellowTicket }}
            <el-icon><EditPen /></el-icon>
          </span>
          <el-input v-else
                    v-model="editingTicket.value"
                    size="small"
                    ref="yellowTicketInput"
                    @blur="saveTicketValue(editingTicket.id, 'yellow', editingTicket.value); editingTicket.id = null"
                    @keyup.enter="saveTicketValue(editingTicket.id, 'yellow', editingTicket.value); editingTicket.id = null"
                    @keyup.escape="editingTicket.id = null; row.yellowTicket"
                    @focus="$event.target.select()" />
        </template>
      </el-table-column>
      <el-table-column prop="sellTime" label="售出时间" min-width="160">
        <template #default="{ row }">{{ row.sellTime ? formatDate(row.sellTime) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="sellPrice" label="售出价格" min-width="100">
        <template #default="{ row }">{{ row.sellPrice !== null && row.sellPrice !== undefined && row.sellPrice !== 0 ? `¥${row.sellPrice}` : '-' }}</template>
      </el-table-column>
      <el-table-column prop="strongCharacter" label="强力角色" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="editingStrongCharacter.id !== row.id" 
                @dblclick="startEditingStrongCharacter(row)"
                style="cursor: pointer;">
            {{ row.strongCharacter || '-' }}
            <el-icon><EditPen /></el-icon>
          </span>
          <el-input v-else
                    v-model="editingStrongCharacter.value"
                    size="small"
                    ref="strongCharacterInput"
                    @blur="saveStrongCharacter(editingStrongCharacter.id, editingStrongCharacter.value); editingStrongCharacter.id = null"
                    @keyup.enter="saveStrongCharacter(editingStrongCharacter.id, editingStrongCharacter.value); editingStrongCharacter.id = null"
                    @keyup.escape="editingStrongCharacter.id = null; row.strongCharacter"
                    @focus="$event.target.select()" />
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="huoxingge" label="火星哥" min-width="120">
        <template #default="{ row }">{{ calculateHuoxingge(row) }}</template>
      </el-table-column>
      <el-table-column prop="kaka" label="卡卡" min-width="120">
        <template #default="{ row }">{{ calculateKaka(row) }}</template>
      </el-table-column>
      <el-table-column prop="intervalDays" label="间隔天数" min-width="100">
        <template #default="{ row }">{{ calculateIntervalDays(row) }}</template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            type="warning"
            size="small"
            @click="openSell(row)"
          >卖出</el-button>
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

    <!-- 新增/编辑账号弹框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑账号' : '新增账号'"
      :width="isMobile ? '92%' : '460px'"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="110px"
      >
        <el-form-item label="账号名" prop="accountName">
          <el-input
            v-model="form.accountName"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入账号名"
          />
        </el-form-item>
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="form.account"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入账号"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            :size="isMobile ? 'small' : 'default'"
            type="text"
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="购入时间" prop="buyTime">
          <el-config-provider :locale="locale">
            <el-date-picker
              v-model="form.buyTime"
              type="datetime"
              placeholder="请选择购入时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :size="isMobile ? 'small' : 'default'"
            />
          </el-config-provider>
        </el-form-item>
        <el-form-item label="购入价格" prop="buyPrice">
          <el-input-number
            v-model="form.buyPrice"
            :precision="2"
            :min="0"
            :step="1"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入购入价格"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="绿票" prop="greenTicket">
          <el-input-number
            v-model="form.greenTicket"
            :min="0"
            :step="1"
            controls-position="right"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入绿票数量"
          />
        </el-form-item>
        <el-form-item label="黄票" prop="yellowTicket">
          <el-input-number
            v-model="form.yellowTicket"
            :min="0"
            :step="1"
            controls-position="right"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入黄票数量"
          />
        </el-form-item>
        <el-form-item label="强力角色" prop="strongCharacter">
          <el-input
            v-model="form.strongCharacter"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入强力角色信息"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="isMobile ? 3 : 4"
            placeholder="请输入备注"
            :size="isMobile ? 'small' : 'default'"
            autosize
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="form.status"
            placeholder="请选择状态"
            :size="isMobile ? 'small' : 'default'"
            style="width: 100%;"
          >
            <el-option label="刷票中" :value="0" />
            <el-option label="出售中" :value="1" />
            <el-option label="出售未收货" :value="2" />
            <el-option label="出售完成" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="dialogVisible = false"
            :disabled="submitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitForm"
            :loading="submitting"
            :size="isMobile ? 'small' : 'default'"
          >{{ isEditing ? '更新' : '创建' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 卖出账号弹框 -->
    <el-dialog
      v-model="sellDialogVisible"
      title="卖出账号"
      :width="isMobile ? '92%' : '460px'"
    >
      <el-form
        ref="sellFormRef"
        :model="sellForm"
        :rules="sellRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="110px"
      >
        <el-form-item label="售出时间" prop="sellTime">
          <el-config-provider :locale="locale">
            <el-date-picker
              v-model="sellForm.sellTime"
              type="datetime"
              placeholder="请选择售出时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :size="isMobile ? 'small' : 'default'"
            />
          </el-config-provider>
        </el-form-item>
        <el-form-item label="售出价格" prop="sellPrice">
          <el-input-number
            v-model="sellForm.sellPrice"
            :precision="2"
            :min="0"
            :step="1"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入售出价格"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="sellForm.status"
            placeholder="请选择状态"
            :size="isMobile ? 'small' : 'default'"
            style="width: 100%;"
          >
            <el-option label="出售中" :value="1" />
            <el-option label="出售未收货" :value="2" />
            <el-option label="出售完成" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="sellDialogVisible = false"
            :disabled="sellSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitSell"
            :loading="sellSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >确认卖出</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 刷票账号弹框 -->
    <el-dialog
      v-model="brushDialogVisible"
      title="刷票账号"
      :width="isMobile ? '92%' : '460px'"
    >
      <el-form
        ref="brushFormRef"
        :model="brushForm"
        :rules="brushRules"
        :size="isMobile ? 'small' : 'default'"
        :label-position="isMobile ? 'top' : 'right'"
        label-width="110px"
      >
        <el-form-item label="绿票" prop="greenTicket">
          <el-input-number
            v-model="brushForm.greenTicket"
            :min="0"
            :step="1"
            controls-position="right"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入绿票数量"
          />
        </el-form-item>
        <el-form-item label="黄票" prop="yellowTicket">
          <el-input-number
            v-model="brushForm.yellowTicket"
            :min="0"
            :step="1"
            controls-position="right"
            :size="isMobile ? 'small' : 'default'"
            placeholder="请输入黄票数量"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right;">
          <el-button
            @click="brushDialogVisible = false"
            :disabled="brushSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >取消</el-button>
          <el-button
            type="primary"
            @click="submitBrush"
            :loading="brushSubmitting"
            :size="isMobile ? 'small' : 'default'"
          >确认刷票</el-button>
        </div>
      </template>
    </el-dialog>

  </el-card>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import api from '../utils/api'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, EditPen } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const router = useRouter()
const locale = zhCn
const loading = ref(false)
const tableData = ref([])

// 双击编辑票券相关
const editingTicket = ref({
  id: null,
  type: null, // 'green' 或 'yellow'
  value: null
})

// 双击编辑强力角色相关
const editingStrongCharacter = ref({
  id: null,
  value: null
})

const page = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 添加搜索相关变量
const searchKeyword = ref('')

// 根据屏幕宽度动态判断是否为手机模式
const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('md')

const loadData = async () => {
  loading.value = true
  try {
    const response = await api.post('/api/account-xyjh/list', {
      page: page.value - 1,
      size: pageSize.value,
      keyword: searchKeyword.value
    })
    
    // 响应拦截器已经处理了Result包装，直接使用response即可
    tableData.value = response.content || response.records || response
    total.value = response.totalElements || response.total || 0
  } catch (e) {
    console.error('加载账号失败:', e)
    if (e.response && e.response.status === 403) {
      ElMessage.error('权限不足或登录已过期，请重新登录')
      // 重定向到登录页
      router.push('/login')
    } else {
      // 检查是否有后端返回的具体错误信息
      if (e.response && e.response.data && e.response.data.message) {
        ElMessage.error(e.response.data.message)
      } else {
        ElMessage.error(e.message || '加载账号失败')
      }
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
    case 0: return 'info' // 刷票中
    case 1: return 'primary' // 出售中
    case 2: return 'warning' // 出售未收货
    case 3: return 'success' // 出售完成
    default: return ''
  }
}

const statusText = (status) => {
  switch (status) {
    case 0: return '刷票中'
    case 1: return '出售中'
    case 2: return '出售未收货'
    case 3: return '出售完成'
    default: return '未知'
  }
}

const formatDate = (str) => {
  if (!str) return '-'
  const d = new Date(str)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const getPasswordDisplay = (password) => {
  return password || '-'
}

// 计算火星哥：购入金额 + ((售出金额 - 手续费) - 购入金额) / 2
const calculateHuoxingge = (row) => {
  if (row.sellPrice == null || row.sellPrice === undefined || row.sellPrice === 0) {
    return '-';
  }
  const fee = row.sellPrice * 0.006; // 计算手续费为售出金额的0.006倍
  const adjustedSellPrice = row.sellPrice - fee; // 扣除手续费
  const result = row.buyPrice + (adjustedSellPrice - row.buyPrice) / 2;
  return isNaN(result) ? '-' : `¥${result.toFixed(2)}`;
}

// 计算卡卡：((售出金额 - 手续费) - 购入金额) / 2
const calculateKaka = (row) => {
  if (row.sellPrice == null || row.sellPrice === undefined || row.sellPrice === 0) {
    return '-';
  }
  const fee = row.sellPrice * 0.006; // 计算手续费为售出金额的0.006倍
  const adjustedSellPrice = row.sellPrice - fee; // 扣除手续费
  const result = (adjustedSellPrice - row.buyPrice) / 2;
  return isNaN(result) ? '-' : `¥${result.toFixed(2)}`;
}

// 计算间隔天数：售出时间减去购入时间的天数
const calculateIntervalDays = (row) => {
  if (!row.buyTime || !row.sellTime) {
    return '-';
  }
  
  // 将时间字符串转换为Date对象
  const buyDate = new Date(row.buyTime);
  const sellDate = new Date(row.sellTime);
  
  // 检查日期是否有效
  if (isNaN(buyDate.getTime()) || isNaN(sellDate.getTime())) {
    return '-';
  }
  
  // 计算毫秒差
  const diffInMs = sellDate.getTime() - buyDate.getTime();
  
  // 转换为天数（1天 = 24 * 60 * 60 * 1000 毫秒）
  const diffInDays = Math.round(diffInMs / (1000 * 60 * 60 * 24));
  
  return diffInDays >= 0 ? `${diffInDays}天` : `${diffInDays}天`;
}

// 表单相关
const dialogVisible = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  account: '',
  password: 'xh112233',
  accountName: '',
  buyTime: new Date(),
  buyPrice: null,
  greenTicket: 0,
  yellowTicket: 0,
  sellTime: null,
  sellPrice: null,
  strongCharacter: '',
  remark: '',
  status: 0
})

// 卖出相关
const sellDialogVisible = ref(false)
const sellSubmitting = ref(false)
const sellFormRef = ref(null)
const sellForm = reactive({
  id: null,
  sellTime: null,
  sellPrice: null,
  status: 1 // 默认为出售中
})

// 刷票相关
const brushDialogVisible = ref(false)
const brushSubmitting = ref(false)
const brushFormRef = ref(null)
const brushForm = reactive({
  id: null,
  greenTicket: 0,
  yellowTicket: 0
})

// 卖出表单验证规则
const sellRules = {
  sellTime: [
    { required: true, message: '请选择售出时间', trigger: 'change' }
  ],
  sellPrice: [
    { required: true, message: '请输入售出价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ]
}

// 刷票表单验证规则
const brushRules = {
  greenTicket: [
    { required: true, message: '请输入绿票数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '数量不能小于0', trigger: 'blur' }
  ],
  yellowTicket: [
    { required: true, message: '请输入黄票数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '数量不能小于0', trigger: 'blur' }
  ]
}

// 表单验证规则
const rules = {
  accountName: [
    { required: true, message: '请输入账号名', trigger: 'blur' }
  ],
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  buyTime: [
    { required: true, message: '请选择购入时间', trigger: 'change' }
  ],
  buyPrice: [
    { required: true, message: '请输入购入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ]
}

const resetForm = () => {
  Object.keys(form).forEach(key => {
    if (key === 'greenTicket' || key === 'yellowTicket') {
      form[key] = 0
    } else if (key === 'status') {
      form[key] = 0
    } else if (key === 'password') {
      form[key] = 'xh112233'
    } else if (key === 'buyTime') {
      // 格式化当前时间为 YYYY-MM-DD HH:mm:ss 格式
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      form[key] = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    } else if (key !== 'id') {
      form[key] = null
    }
  })
  form.id = null
}

const openCreateDialog = () => {
  resetForm()
  isEditing.value = false
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.keys(form).forEach(key => {
    form[key] = row[key]
  })
  isEditing.value = true
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEditing.value) {
        // 更新账号
        await api.put('/api/account-xyjh/update', form)
      } else {
        // 创建账号
        await api.post('/api/account-xyjh/create', form)
      }
      
      ElMessage.success(isEditing.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } catch (e) {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        // 检查是否有后端返回的具体错误信息
        if (e.response && e.response.data && e.response.data.message) {
          ElMessage.error(e.response.data.message)
        } else {
          ElMessage.error(e.message || (isEditing.value ? '更新失败' : '创建失败'))
        }
      }
    } finally {
      submitting.value = false
    }
  })
}

const deleteAccount = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个账号吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.delete(`/api/account-xyjh/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        // 检查是否有后端返回的具体错误信息
        if (e.response && e.response.data && e.response.data.message) {
          ElMessage.error(e.response.data.message)
        } else {
          ElMessage.error(e.message || '删除失败')
        }
      }
    }
  }
}

// 开始编辑票券数量
const startEditingTicket = (row, ticketType) => {
  editingTicket.value = {
    id: row.id,
    type: ticketType,
    value: ticketType === 'green' ? row.greenTicket : row.yellowTicket
  }
  // 延迟聚焦以确保DOM更新完成
  setTimeout(() => {
    const input = ticketType === 'green' ? document.querySelector('[ref="greenTicketInput"] input') : document.querySelector('[ref="yellowTicketInput"] input')
    if (input) {
      input.focus()
      input.select()
    }
  }, 0)
}

// 开始编辑强力角色
const startEditingStrongCharacter = (row) => {
  editingStrongCharacter.value = {
    id: row.id,
    value: row.strongCharacter || ''
  }
  // 延迟聚焦以确保DOM更新完成
  setTimeout(() => {
    const input = document.querySelector('[ref="strongCharacterInput"] input')
    if (input) {
      input.focus()
      input.select()
    }
  }, 0)
}

// 保存票券值到后端
const saveTicketValue = async (id, ticketType, value) => {
  try {
    const updateData = {
      id: id
    }
    
    if (ticketType === 'green') {
      updateData.greenTicket = value
    } else {
      updateData.yellowTicket = value
    }
    
    await api.put('/api/account-xyjh/update', updateData)
    ElMessage.success(`${ticketType === 'green' ? '绿票' : '黄票'}更新成功`)
    
    // 更新本地数据
    const row = tableData.value.find(item => item.id === id)
    if (row) {
      if (ticketType === 'green') {
        row.greenTicket = value
      } else {
        row.yellowTicket = value
      }
    }
  } catch (error) {
    console.error(`更新${ticketType === 'green' ? '绿票' : '黄票'}失败:`, error)
    ElMessage.error(`${ticketType === 'green' ? '绿票' : '黄票'}更新失败`)
    
    if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足或登录已过期，请重新登录')
      router.push('/login')
    }
  }
}

// 保存强力角色到后端
const saveStrongCharacter = async (id, value) => {
  try {
    const updateData = {
      id: id,
      strongCharacter: value
    }
    
    await api.put('/api/account-xyjh/update', updateData)
    ElMessage.success('强力角色更新成功')
    
    // 更新本地数据
    const row = tableData.value.find(item => item.id === id)
    if (row) {
      row.strongCharacter = value
    }
  } catch (error) {
    console.error('更新强力角色失败:', error)
    ElMessage.error('强力角色更新失败')
    
    if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足或登录已过期，请重新登录')
      router.push('/login')
    }
  }
}

const openSell = (row) => {
  sellForm.id = row.id
  // 设置售出时间为当前时间
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  sellForm.sellTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  sellForm.sellPrice = row.sellPrice
  // 卖出时默认状态设为"出售中"，而不是原始状态
  sellForm.status = 1
  sellDialogVisible.value = true
}

const submitSell = async () => {
  if (!sellFormRef.value) return
  await sellFormRef.value.validate(async (valid) => {
    if (!valid) return
    sellSubmitting.value = true
    try {
      // 创建一个包含ID和其他需要更新字段的对象
      const updateData = {
        id: sellForm.id,
        sellTime: sellForm.sellTime,
        sellPrice: sellForm.sellPrice,
        status: sellForm.status
      }
      await api.put('/api/account-xyjh/update', updateData)
      ElMessage.success('卖出信息更新成功')
      sellDialogVisible.value = false
      loadData()
    } catch (e) {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error(e.message || '卖出信息更新失败')
      }
    } finally {
      sellSubmitting.value = false
    }
  })
}

const openBrush = (row) => {
  brushForm.id = row.id
  brushForm.greenTicket = row.greenTicket
  brushForm.yellowTicket = row.yellowTicket
  brushDialogVisible.value = true
}

const submitBrush = async () => {
  if (!brushFormRef.value) return
  await brushFormRef.value.validate(async (valid) => {
    if (!valid) return
    brushSubmitting.value = true
    try {
      // 创建一个包含ID和其他需要更新字段的对象
      const updateData = {
        id: brushForm.id,
        greenTicket: brushForm.greenTicket,
        yellowTicket: brushForm.yellowTicket
      }
      await api.put('/api/account-xyjh/update', updateData)
      ElMessage.success('刷票信息更新成功')
      brushDialogVisible.value = false
      loadData()
    } catch (e) {
      if (e.response && e.response.status === 403) {
        ElMessage.error('权限不足或登录已过期，请重新登录')
        router.push('/login')
      } else {
        ElMessage.error(e.message || '刷票信息更新失败')
      }
    } finally {
      brushSubmitting.value = false
    }
  })
}

// 获取表格行的唯一键值
const getRowKey = (row) => {
  return row.id || `${row.account}_${row.accountName || ''}_${row.buyTime || ''}`;
}

// 根据状态设置行颜色
const tableRowClassName = ({ row }) => {
  // 确保状态值是数字类型
  const status = Number(row.status);
  
  if (status === 0) {
    return 'status-brushing' // 刷票中 - 浅灰色
  } else if (status === 1) {
    return 'status-selling' // 出售中 - 淡蓝色
  } else if (status === 2) {
    return 'status-pending' // 出售未收货 - 橙色
  } else if (status === 3) {
    return 'status-completed' // 出售完成 - 淡绿色
  }
  return ''
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

/* 根据状态设置表格行颜色 */
:deep(.el-table .status-brushing) {
  background-color: #f5f5f5; /* 刷票中 - 浅灰色 */
}

:deep(.el-table .status-selling) {
  background-color: #e3f2fd; /* 出售中 - 淡蓝色 */
}

:deep(.el-table .status-pending) {
  background-color: #fff3e0; /* 出售未收货 - 橙色 */
}

:deep(.el-table .status-completed) {
  background-color: #e8f5e8; /* 出售完成 - 淡绿色 */
}
.search-controls {
  margin-bottom: 16px;
}
</style>