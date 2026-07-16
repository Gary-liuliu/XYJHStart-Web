<template>
  <section ref="pageRoot" class="trade-console">
    <section v-if="isMobile" class="mobile-delivery">
      <div class="mobile-delivery-bar mobile-entrance">
        <el-input
          v-model="searchKeyword"
          class="mobile-search"
          placeholder="搜索账号"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <button class="mobile-refresh" type="button" @click="loadData">
          <el-icon><RefreshRight /></el-icon>
        </button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        :row-class-name="tableRowClassName"
        :cell-class-name="mobileTableCellClassName"
        :row-key="getRowKey"
        class="mobile-account-table mobile-entrance"
        :height="mobileTableHeight"
      >
        <el-table-column label="复制" width="70" fixed="left" align="center">
          <template #default="{ row }">
            <button class="mobile-copy-button" type="button" @click.stop="copyAccount(row)">复制</button>
          </template>
        </el-table-column>
        <el-table-column prop="accountName" label="账号" min-width="188">
          <template #default="{ row }">
            <div class="mobile-account-cell">
              <strong>{{ row.accountName || '-' }}</strong>
              <span>{{ row.account || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="password" label="密码" min-width="102">
          <template #default="{ row }">{{ row.password || '-' }}</template>
        </el-table-column>
        <el-table-column label="票券" width="126" align="center">
          <template #default="{ row }">
            <div class="mobile-ticket-stack">
              <span class="mobile-ticket-value green">绿 {{ row.greenTicket ?? 0 }}</span>
              <span class="mobile-ticket-value yellow">黄 {{ row.yellowTicket ?? 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sellPrice" label="售价" width="94" align="right">
          <template #default="{ row }">
            <button class="mobile-price-button" type="button" @click.stop="openPriceEdit(row)">
              {{ row.sellPrice ? formatMoney(row.sellPrice) : '填价格' }}
            </button>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="96">
          <template #default="{ row }">
            <el-select
              v-model="row.status"
              class="mobile-status-select"
              size="small"
              @click.stop
              @change="updateAccountStatus(row, $event)"
            >
              <el-option v-for="option in statusSelectOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <div class="mobile-pagination mobile-entrance">
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
    </section>

    <section v-else class="desktop-console">
      <section class="desk-header desktop-entrance">
        <div>
          <p>账号买卖</p>
          <h1>账号库存与真实记账</h1>
        </div>
        <div class="desk-actions">
          <button class="console-button ghost" type="button" @click="loadData">
            <el-icon><RefreshRight /></el-icon>
            刷新
          </button>
          <button class="console-button primary" type="button" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            新增账号
          </button>
        </div>
      </section>

      <section class="accounting-strip desktop-entrance">
        <div class="accounting-card huoxing">
          <span>火星哥记账</span>
          <strong>{{ formatMoney(accountingSummary.huoxinggeTotal) }}</strong>
        </div>
        <div class="accounting-card kaka">
          <span>卡卡记账</span>
          <strong>{{ formatMoney(accountingSummary.kakaTotal) }}</strong>
        </div>
      </section>

      <section class="table-panel desktop-entrance">
        <div class="table-toolbar">
          <el-input
            v-model="searchKeyword"
            class="keyword-input"
            placeholder="搜索账号名、邮箱或备注"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select v-model="statusFilter" class="status-select" @change="handleSearch">
            <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>

          <button class="console-button ghost" type="button" @click="handleSearch">
            <el-icon><Operation /></el-icon>
            查询
          </button>
        </div>

        <el-table
          v-loading="loading"
          :data="tableData"
          :row-class-name="tableRowClassName"
          :row-key="getRowKey"
          class="account-table"
          :height="desktopTableHeight"
        >
          <el-table-column prop="accountName" label="账号名" min-width="132" />
          <el-table-column prop="account" label="账号" min-width="178" show-overflow-tooltip />
          <el-table-column prop="password" label="密码" width="118">
            <template #default="{ row }">{{ row.password || '-' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="108">
            <template #default="{ row }">
              <el-select
                v-model="row.status"
                class="status-inline-select"
                size="small"
                @change="updateAccountStatus(row, $event)"
              >
                <el-option v-for="option in statusSelectOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="票券" min-width="184">
            <template #default="{ row }">
              <div class="ticket-stack">
                <button class="editable-value green" type="button" @click.stop="startEditingTicket(row, 'green')">
                  绿 {{ row.greenTicket ?? 0 }}
                </button>
                <button class="editable-value yellow" type="button" @click.stop="startEditingTicket(row, 'yellow')">
                  黄 {{ row.yellowTicket ?? 0 }}
                </button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="buyPrice" label="购入价" width="96" align="right">
            <template #default="{ row }">{{ formatMoney(row.buyPrice) }}</template>
          </el-table-column>
          <el-table-column prop="sellPrice" label="售出价" width="96" align="right">
            <template #default="{ row }">
              <button class="price-edit" type="button" @click.stop="openPriceEdit(row)">
                {{ row.sellPrice ? formatMoney(row.sellPrice) : '填价格' }}
              </button>
            </template>
          </el-table-column>
          <el-table-column label="火星哥" width="104" align="right">
            <template #default="{ row }">{{ calculateHuoxingge(row) }}</template>
          </el-table-column>
          <el-table-column label="卡卡" width="104" align="right">
            <template #default="{ row }">{{ calculateKaka(row) }}</template>
          </el-table-column>
          <el-table-column prop="strongCharacter" label="强力角色" min-width="126" show-overflow-tooltip>
            <template #default="{ row }">
              <button class="text-edit" type="button" @click.stop="startEditingStrongCharacter(row)">
                {{ row.strongCharacter || '补充角色' }}
              </button>
            </template>
          </el-table-column>
          <el-table-column prop="buyTime" label="购入时间" min-width="148">
            <template #default="{ row }">{{ formatDate(row.buyTime) }}</template>
          </el-table-column>
          <el-table-column prop="sellTime" label="售出时间" min-width="148">
            <template #default="{ row }">{{ formatDate(row.sellTime) }}</template>
          </el-table-column>
          <el-table-column label="周期" width="76" align="center">
            <template #default="{ row }">{{ calculateIntervalDays(row) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="206" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button size="small" :icon="EditPen" @click.stop="openEdit(row)">编辑</el-button>
                <el-button size="small" type="warning" :icon="Sell" @click.stop="openSell(row)">卖出</el-button>
                <el-button size="small" type="primary" plain :icon="CopyDocument" @click.stop="copyAccount(row)">复制</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            background
            :current-page="page"
            :page-size="pageSize"
            :page-sizes="[16, 24, 40, 80]"
            layout="sizes, prev, pager, next, jumper, total"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </section>
    </section>

    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑账号' : '新增账号'" :width="dialogWidth">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" :label-position="isMobile ? 'top' : 'right'">
        <div class="form-grid">
          <el-form-item label="账号名" prop="accountName">
            <el-input v-model="form.accountName" placeholder="请输入账号名" />
          </el-form-item>
          <el-form-item label="账号" prop="account">
            <el-input v-model="form.account" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="option in statusSelectOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="购入时间" prop="buyTime">
            <el-date-picker v-model="form.buyTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item label="购入价" prop="buyPrice">
            <el-input-number v-model="form.buyPrice" :precision="2" :min="0" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="绿票" prop="greenTicket">
            <el-input-number v-model="form.greenTicket" :min="0" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="黄票" prop="yellowTicket">
            <el-input-number v-model="form.yellowTicket" :min="0" :step="1" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="强力角色" prop="strongCharacter">
            <el-input v-model="form.strongCharacter" placeholder="例如：限定角色、强力组合" />
          </el-form-item>
          <el-form-item class="full-row" label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">{{ isEditing ? '更新' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sellDialogVisible" title="账号卖出" :width="dialogWidth">
      <el-form label-width="92px" :label-position="isMobile ? 'top' : 'right'">
        <el-form-item label="账号名">
          <el-input :model-value="activeAccount?.accountName || '-'" disabled />
        </el-form-item>
        <el-form-item label="售出价">
          <el-input-number v-model="sellForm.sellPrice" :precision="2" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="售出时间">
          <el-date-picker v-model="sellForm.sellTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="sellDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSell">确认卖出</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ticketDialogVisible" :title="ticketEditTitle" width="360px">
      <el-input-number v-model="ticketForm.value" :min="0" :step="1" controls-position="right" style="width: 100%" />
      <template #footer>
        <el-button :disabled="submitting" @click="ticketDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTicketEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="priceDialogVisible" title="修改售价" width="360px">
      <el-input-number v-model="priceForm.sellPrice" :precision="2" :min="0" :step="1" controls-position="right" style="width: 100%" />
      <template #footer>
        <el-button :disabled="submitting" @click="priceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPriceEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="strongCharacterDialogVisible" title="修改强力角色" :width="dialogWidth">
      <el-input v-model="strongCharacterForm.strongCharacter" placeholder="请输入强力角色" />
      <template #footer>
        <el-button :disabled="submitting" @click="strongCharacterDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitStrongCharacterEdit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useBreakpoints, breakpointsTailwind } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { CopyDocument, EditPen, Operation, Plus, RefreshRight, Search, Sell } from '@element-plus/icons-vue'
import { gsap } from 'gsap'
import api from '../utils/api'

const pageRoot = ref(null)
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(16)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const sellDialogVisible = ref(false)
const ticketDialogVisible = ref(false)
const priceDialogVisible = ref(false)
const strongCharacterDialogVisible = ref(false)
const isEditing = ref(false)
const activeAccount = ref(null)
const formRef = ref(null)
let pageAnimationContext

const breakpoints = useBreakpoints(breakpointsTailwind)
const isMobile = breakpoints.smaller('md')
const dialogWidth = computed(() => (isMobile.value ? '92vw' : '720px'))
const mobileTableHeight = computed(() => '100%')
const desktopTableHeight = computed(() => 'calc(100vh - 254px)')

const statusSelectOptions = [
  { label: '刷票中', value: 0 },
  { label: '出售中', value: 1 },
  { label: '未收款', value: 2 },
  { label: '已完成', value: 3 }
]

const statusOptions = [
  { label: '全部状态', value: '' },
  ...statusSelectOptions
]

const accountingSummary = reactive({
  huoxinggeTotal: 0,
  kakaTotal: 0,
  accountedCount: 0
})

const form = reactive({
  id: null,
  account: '',
  password: '',
  accountName: '',
  buyTime: '',
  status: 0,
  buyPrice: 0,
  greenTicket: 0,
  yellowTicket: 0,
  strongCharacter: '',
  remark: ''
})

const sellForm = reactive({
  sellPrice: 0,
  sellTime: ''
})

const ticketForm = reactive({
  account: null,
  type: 'green',
  value: 0
})

const priceForm = reactive({
  account: null,
  sellPrice: 0
})

const strongCharacterForm = reactive({
  account: null,
  strongCharacter: ''
})

const rules = {
  accountName: [{ required: true, message: '请输入账号名', trigger: 'blur' }],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const ticketEditTitle = computed(() => (ticketForm.type === 'green' ? '修改绿票' : '修改黄票'))

const nowDateTime = () => {
  const date = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const normalizeListResponse = (res) => {
  if (Array.isArray(res)) {
    return { list: res, totalCount: res.length }
  }
  return {
    list: res?.content || res?.records || res?.list || [],
    totalCount: res?.totalElements ?? res?.total ?? res?.totalCount ?? 0
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const keyword = searchKeyword.value.trim()
    const params = {
      page: page.value - 1,
      size: pageSize.value
    }

    if (keyword) {
      params.account = keyword
    }

    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }

    const res = await api.post('/api/account-xyjh/list', params)
    const normalized = normalizeListResponse(res)
    tableData.value = normalized.list
    total.value = normalized.totalCount
    await nextTick()
    animateTableRows()
  } catch (error) {
    ElMessage.error(error.message || '账号列表加载失败')
  } finally {
    loading.value = false
  }
}

const loadAccountingSummary = async () => {
  try {
    const res = await api.get('/api/account-xyjh/accounting-summary')
    accountingSummary.huoxinggeTotal = res?.huoxinggeTotal ?? 0
    accountingSummary.kakaTotal = res?.kakaTotal ?? 0
    accountingSummary.accountedCount = res?.accountedCount ?? 0
  } catch (error) {
    ElMessage.error(error.message || '记账统计加载失败')
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  page.value = 1
  loadData()
}

const handleCurrentChange = (currentPage) => {
  page.value = currentPage
  loadData()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    account: '',
    password: 'xh112233',
    accountName: getNextNumericAccountName(),
    buyTime: nowDateTime(),
    status: 0,
    buyPrice: 0,
    greenTicket: 0,
    yellowTicket: 0,
    strongCharacter: '',
    remark: ''
  })
}

const openCreateDialog = () => {
  resetForm()
  isEditing.value = false
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate?.())
}

const getNextNumericAccountName = () => {
  const latestAccountName = String(tableData.value.find((account) => account.accountName)?.accountName || '').trim()
  if (!/^\d+$/.test(latestAccountName)) {
    return ''
  }
  return String(Number(latestAccountName) + 1)
}

const openEdit = (row) => {
  resetForm()
  Object.assign(form, {
    ...row,
    buyTime: formatDateTimeInput(row.buyTime),
    status: Number(row.status ?? 0),
    buyPrice: Number(row.buyPrice ?? 0),
    greenTicket: Number(row.greenTicket ?? 0),
    yellowTicket: Number(row.yellowTicket ?? 0)
  })
  isEditing.value = true
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate?.())
}

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = { ...form }
    if (isEditing.value) {
      await api.put('/api/account-xyjh/update', payload)
      ElMessage.success('账号已更新')
    } else {
      delete payload.id
      await api.post('/api/account-xyjh/create', payload)
      ElMessage.success('账号已新增')
    }
    dialogVisible.value = false
    await Promise.all([loadData(), loadAccountingSummary()])
  } catch (error) {
    ElMessage.error(error.message || '保存账号失败')
  } finally {
    submitting.value = false
  }
}

const openSell = (row) => {
  activeAccount.value = row
  sellForm.sellPrice = Number(row.sellPrice ?? 0)
  sellForm.sellTime = formatDateTimeInput(row.sellTime) || nowDateTime()
  sellDialogVisible.value = true
}

const submitSell = async () => {
  if (!activeAccount.value?.id) return
  submitting.value = true
  try {
    await updateAccountFields(activeAccount.value, {
      sellPrice: sellForm.sellPrice,
      sellTime: sellForm.sellTime,
      status: 3
    })
    sellDialogVisible.value = false
    ElMessage.success('卖出信息已保存')
    await Promise.all([loadData(), loadAccountingSummary()])
  } catch (error) {
    ElMessage.error(error.message || '卖出保存失败')
  } finally {
    submitting.value = false
  }
}

const startEditingTicket = (row, type) => {
  ticketForm.account = row
  ticketForm.type = type
  ticketForm.value = Number(type === 'green' ? row.greenTicket ?? 0 : row.yellowTicket ?? 0)
  ticketDialogVisible.value = true
}

const submitTicketEdit = async () => {
  if (!ticketForm.account?.id) return
  submitting.value = true
  try {
    const field = ticketForm.type === 'green' ? 'greenTicket' : 'yellowTicket'
    await updateAccountFields(ticketForm.account, { [field]: ticketForm.value })
    ticketDialogVisible.value = false
    ElMessage.success('票券已更新')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '票券更新失败')
  } finally {
    submitting.value = false
  }
}

const openPriceEdit = (row) => {
  priceForm.account = row
  priceForm.sellPrice = Number(row.sellPrice ?? 0)
  priceDialogVisible.value = true
}

const submitPriceEdit = async () => {
  if (!priceForm.account?.id) return
  submitting.value = true
  try {
    await updateAccountFields(priceForm.account, { sellPrice: priceForm.sellPrice })
    priceDialogVisible.value = false
    ElMessage.success('售价已更新')
    await Promise.all([loadData(), loadAccountingSummary()])
  } catch (error) {
    ElMessage.error(error.message || '售价更新失败')
  } finally {
    submitting.value = false
  }
}

const startEditingStrongCharacter = (row) => {
  strongCharacterForm.account = row
  strongCharacterForm.strongCharacter = row.strongCharacter || ''
  strongCharacterDialogVisible.value = true
}

const submitStrongCharacterEdit = async () => {
  if (!strongCharacterForm.account?.id) return
  submitting.value = true
  try {
    await updateAccountFields(strongCharacterForm.account, {
      strongCharacter: strongCharacterForm.strongCharacter
    })
    strongCharacterDialogVisible.value = false
    ElMessage.success('强力角色已更新')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '强力角色更新失败')
  } finally {
    submitting.value = false
  }
}

const updateAccountStatus = async (row, status) => {
  const previousStatus = row.status
  row.status = status
  try {
    await updateAccountFields(row, { status })
    ElMessage.success('状态已更新')
    await Promise.all([loadData(), loadAccountingSummary()])
  } catch (error) {
    row.status = previousStatus
    ElMessage.error(error.message || '状态更新失败')
  }
}

const updateAccountFields = (row, fields) => {
  return api.put('/api/account-xyjh/update', {
    id: row.id,
    ...fields
  })
}

const copyAccount = async (row) => {
  const text = `账号名：${row.accountName || ''}\n账号：${row.account || ''}\n密码：${row.password || ''}`
  try {
    await writeClipboardText(text)
    ElMessage.success('账号已复制')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const writeClipboardText = async (text) => {
  if (navigator.clipboard && window.isSecureContext) {
    await navigator.clipboard.writeText(text)
    return
  }

  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', '')
  textarea.style.position = 'fixed'
  textarea.style.top = '-9999px'
  textarea.style.left = '-9999px'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  const ok = document.execCommand('copy')
  document.body.removeChild(textarea)
  if (!ok) {
    throw new Error('copy failed')
  }
}

const calculateProfitValues = (row) => {
  const sellPrice = Number(row.sellPrice ?? 0)
  const buyPrice = Number(row.buyPrice ?? 0)
  if (!sellPrice || !buyPrice) {
    return null
  }
  const adjustedSellPrice = sellPrice - sellPrice * 0.006
  const kaka = (adjustedSellPrice - buyPrice) / 2
  const huoxingge = buyPrice + kaka
  return { kaka, huoxingge }
}

const calculateHuoxingge = (row) => {
  const values = calculateProfitValues(row)
  return values ? formatMoney(values.huoxingge) : '-'
}

const calculateKaka = (row) => {
  const values = calculateProfitValues(row)
  return values ? formatMoney(values.kaka) : '-'
}

const calculateIntervalDays = (row) => {
  if (!row.buyTime || !row.sellTime) return '-'
  const buy = new Date(row.buyTime)
  const sell = new Date(row.sellTime)
  if (Number.isNaN(buy.getTime()) || Number.isNaN(sell.getTime())) return '-'
  const days = Math.ceil((sell.getTime() - buy.getTime()) / 86400000)
  return `${Math.max(days, 0)}天`
}

const formatMoney = (value) => {
  const number = Number(value ?? 0)
  if (!Number.isFinite(number)) return '¥0.00'
  return `¥${number.toFixed(2)}`
}

const formatDate = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

const formatDateTimeInput = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

const statusText = (status) => {
  return statusSelectOptions.find((option) => option.value === Number(status))?.label || '未知'
}

const statusType = (status) => {
  const value = Number(status)
  if (value === 0) return 'primary'
  if (value === 1) return 'warning'
  if (value === 2) return 'danger'
  if (value === 3) return 'success'
  return 'info'
}

const tableRowClassName = ({ row }) => `status-${Number(row.status ?? 0)}`

const mobileTableCellClassName = ({ row, column }) => {
  if (column.property !== 'status') return ''
  return `mobile-status-cell status-${Number(row.status ?? 0)}`
}

const getRowKey = (row) => row.id || row.account || row.accountName

const animatePageEntrance = () => {
  if (!pageRoot.value) return
  pageAnimationContext?.revert()
  pageAnimationContext = gsap.context(() => {
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (reduceMotion) return
    gsap.from('.desktop-entrance, .mobile-entrance', {
      autoAlpha: 0,
      y: 18,
      duration: 0.58,
      ease: 'power2.out',
      stagger: 0.08
    })
  }, pageRoot.value)
}

const animateTableRows = () => {
  if (!pageRoot.value) return
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduceMotion) return
  gsap.from(pageRoot.value.querySelectorAll('.el-table__body-wrapper tbody tr'), {
    autoAlpha: 0,
    y: 8,
    duration: 0.28,
    ease: 'power1.out',
    stagger: 0.015,
    overwrite: 'auto'
  })
}

watch(isMobile, () => {
  pageSize.value = isMobile.value ? 20 : 16
  page.value = 1
  nextTick(animatePageEntrance)
  loadData()
})

onMounted(async () => {
  pageSize.value = isMobile.value ? 20 : 16
  await Promise.all([loadData(), loadAccountingSummary()])
  await nextTick()
  animatePageEntrance()
})

onUnmounted(() => {
  pageAnimationContext?.revert()
})
</script>

<style scoped>
.trade-console {
  min-height: 100%;
}

.desktop-console {
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 12px;
  min-height: calc(100vh - 32px);
}

.desk-header,
.accounting-strip,
.table-panel {
  border: 1px solid #e3e8f0;
  background: #ffffff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.desk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-radius: 8px;
}

.desk-header p,
.desk-header h1 {
  margin: 0;
}

.desk-header p {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.desk-header h1 {
  margin-top: 4px;
  color: #111827;
  font-size: 22px;
  line-height: 1.2;
}

.desk-actions,
.table-toolbar,
.row-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.console-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
  padding: 0 14px;
  border-radius: 7px;
  border: 1px solid #d8e1ec;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.console-button:hover {
  transform: translateY(-1px);
}

.console-button.primary {
  color: #fff;
  border-color: #2563eb;
  background: #2563eb;
}

.console-button.ghost {
  color: #334155;
  background: #fff;
}

.accounting-strip {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px;
  overflow: hidden;
  border-radius: 8px;
  background: #e3e8f0;
}

.accounting-card {
  min-height: 70px;
  padding: 14px 18px;
  background: #fff;
}

.accounting-card span,
.accounting-card small {
  display: block;
  color: #64748b;
}

.accounting-card span {
  font-size: 13px;
  font-weight: 700;
}

.accounting-card strong {
  display: block;
  margin: 8px 0 0;
  color: #111827;
  font-size: 24px;
  line-height: 1;
}

.accounting-card small {
  font-size: 12px;
}

.accounting-card.huoxing {
  border-top: 3px solid #2563eb;
}

.accounting-card.kaka {
  border-top: 3px solid #0f766e;
}

.table-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  min-height: 0;
  padding: 12px;
  border-radius: 8px;
}

.table-toolbar {
  justify-content: flex-start;
  padding-bottom: 12px;
}

.keyword-input {
  width: 320px;
}

.status-select {
  width: 132px;
}

.account-table {
  --el-table-border-color: #eef2f7;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f6faff;
  border-radius: 8px;
  overflow: hidden;
  font-size: 13px;
}

:deep(.account-table .el-table__header th) {
  height: 42px;
  color: #475569;
  font-weight: 800;
  background: #f8fafc !important;
}

:deep(.account-table .el-table__cell) {
  border-right: 0 !important;
}

:deep(.account-table .el-table__body tr.status-0 > td) {
  background: #dbeafe !important;
}

:deep(.account-table .el-table__body tr.status-1 > td) {
  background: #fef3c7 !important;
}

:deep(.account-table .el-table__body tr.status-3 > td) {
  background: #dcfce7 !important;
}

:deep(.account-table .el-table__body tr.status-2 > td) {
  background: #fee2e2 !important;
}

:deep(.account-table .el-table__fixed-right::before),
:deep(.account-table .el-table__fixed::before) {
  box-shadow: none;
}

:deep(.account-table .el-table__fixed-right) {
  box-shadow: -8px 0 18px rgba(15, 23, 42, 0.06);
}

.ticket-stack {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
}

.editable-value,
.price-edit,
.text-edit {
  border: 0;
  background: transparent;
  cursor: pointer;
  font: inherit;
}

.editable-value {
  padding: 3px 7px;
  border-radius: 6px;
  font-weight: 800;
  white-space: nowrap;
}

.editable-value.green {
  color: #047857;
  background: #dff7ea;
}

.editable-value.yellow {
  color: #b45309;
  background: #fef3c7;
}

.price-edit,
.text-edit {
  padding: 0;
  color: #2563eb;
  font-weight: 700;
}

.status-inline-select {
  width: 92px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 18px;
}

.form-grid .full-row {
  grid-column: 1 / -1;
}

.mobile-delivery {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 8px;
  height: calc(100dvh - 48px);
  min-height: 0;
  padding: 8px;
  overflow: hidden;
  background: #f3f6fb;
}

.mobile-delivery-bar {
  display: flex;
  gap: 8px;
}

.mobile-search {
  flex: 1;
}

.mobile-refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  border: 1px solid #d8e1ec;
  border-radius: 8px;
  color: #1f2937;
  background: #fff;
}

.mobile-account-table {
  min-height: 0;
  border-radius: 8px;
  overflow: hidden;
  font-size: 13px;
}

:deep(.mobile-account-table .el-table__header th) {
  height: 38px;
  color: #475569;
  font-weight: 800;
  background: #f8fafc !important;
}

:deep(.mobile-account-table .el-table__cell) {
  border-right: 0 !important;
}

:deep(.mobile-account-table .el-table__body tr.status-0 > td) {
  background: #dbeafe !important;
}

:deep(.mobile-account-table .el-table__body tr.status-1 > td) {
  background: #fef3c7 !important;
}

:deep(.mobile-account-table .el-table__body tr.status-2 > td) {
  background: #fee2e2 !important;
}

:deep(.mobile-account-table .el-table__body tr.status-3 > td) {
  background: #dcfce7 !important;
}

:deep(.mobile-account-table .el-table__body tr.status-0 > td.mobile-status-cell) {
  background: #93c5fd !important;
}

:deep(.mobile-account-table .el-table__body tr.status-1 > td.mobile-status-cell) {
  background: #fcd34d !important;
}

:deep(.mobile-account-table .el-table__body tr.status-2 > td.mobile-status-cell) {
  background: #fca5a5 !important;
}

:deep(.mobile-account-table .el-table__body tr.status-3 > td.mobile-status-cell) {
  background: #86efac !important;
}

:deep(.mobile-status-cell .el-select__wrapper) {
  background: rgba(255, 255, 255, 0.74);
}

.mobile-account-cell {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.mobile-account-cell strong,
.mobile-account-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-account-cell strong {
  color: #111827;
}

.mobile-account-cell span {
  color: #64748b;
  font-size: 12px;
}

.mobile-ticket-stack {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.mobile-ticket-value {
  padding: 3px 6px;
  border-radius: 6px;
  font-weight: 800;
  white-space: nowrap;
}

.mobile-ticket-value.green {
  color: #047857;
  background: #dff7ea;
}

.mobile-ticket-value.yellow {
  color: #b45309;
  background: #fef3c7;
}

.mobile-copy-button,
.mobile-price-button {
  border: 0;
  border-radius: 7px;
  font-weight: 800;
  cursor: pointer;
}

.mobile-copy-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 30px;
  padding: 0 12px;
  color: #1d4ed8;
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, 0.18);
}

.mobile-price-button {
  padding: 4px 0;
  color: #0f766e;
  background: transparent;
}

.mobile-status-select {
  width: 82px;
}

.mobile-pagination {
  display: flex;
  flex-shrink: 0;
  justify-content: center;
  min-height: 34px;
  padding: 2px 0 0;
}

@media (max-width: 767px) {
  .trade-console {
    height: calc(100dvh - 48px);
    min-height: 0;
    overflow: hidden;
  }

  .mobile-delivery {
    height: 100%;
    padding: 6px;
    gap: 6px;
  }

  .mobile-account-table {
    font-size: 12px;
  }

  .mobile-copy-button {
    min-width: 44px;
    height: 28px;
    padding: 0 10px;
    border-radius: 999px;
    font-size: 12px;
  }

  .mobile-price-button {
    font-size: 12px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
