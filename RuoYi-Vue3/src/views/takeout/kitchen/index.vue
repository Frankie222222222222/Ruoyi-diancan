<template>
  <div class="app-container">
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6">
        <KpiCard title="待制作" :value="dashboard.paidCount || 0" icon="Tickets" tone="primary" />
      </el-col>
      <el-col :span="6">
        <KpiCard title="制作中" :value="dashboard.makingCount || 0" icon="Clock" tone="warning" />
      </el-col>
      <el-col :span="6">
        <KpiCard title="待出餐" :value="dashboard.readyCount || 0" icon="Bell" tone="success" />
      </el-col>
      <el-col :span="6">
        <KpiCard title="今日总单" :value="dashboard.totalCount || 0" icon="DataAnalysis" tone="info" />
      </el-col>
    </el-row>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" ref="queryForm" size="default">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="搜索订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="下单时间">
          <TimeRangePicker v-model="queryParams.dateRange" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="filteredList" stripe>
        <el-table-column label="订单号" prop="orderNo" min-width="170" />
        <el-table-column label="顾客" prop="userNickname" min-width="100" />
        <el-table-column label="电话" prop="userPhone" min-width="120" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div v-for="it in (row.items || [])" :key="it.dishId" class="kitchen-item">
              <span>{{ it.dishName }} × {{ it.qty }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" prop="totalAmount" width="90">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" disable-transitions>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
        <el-table-column label="下单时间" prop="createTime" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canAccept(row)" type="primary" link @click="onAccept(row)">接单</el-button>
            <el-button v-if="canReady(row)" type="success" link @click="onReady(row)">出餐</el-button>
            <el-button link @click="onDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadList"
      />
    </el-card>
  </div>
</template>

<script setup name="TakeoutKitchen">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { listKitchenOrders, kitchenAccept, kitchenReady, kitchenDashboard } from '@/api/takeout/kitchen'
import KpiCard from '@/components/KpiCard/index.vue'
import TimeRangePicker from '@/components/TimeRangePicker/index.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dashboard = ref({})
const queryParams = ref({
  pageNum: 1, pageSize: 20,
  orderNo: '',
  dateRange: []
})

const filteredList = computed(() => {
  let l = list.value
  if (queryParams.value.orderNo) {
    l = l.filter(o => (o.orderNo || '').includes(queryParams.value.orderNo))
  }
  if (queryParams.value.dateRange && queryParams.value.dateRange.length === 2) {
    const [s, e] = queryParams.value.dateRange
    l = l.filter(o => o.createTime >= s && o.createTime <= e)
  }
  return l
})

const canAccept = (row) => row.status === '1' || row.status === '2'
const canReady  = (row) => row.status === '2a' || row.status === '2' || row.status === 'COOKING'

const statusType = (s) => ({
  '1': 'info', '2': 'warning', '2a': 'warning', '3': 'success', '4': 'success',
  PENDING: 'info', COOKING: 'warning', READY: 'success', DELIVERING: 'success', DONE: '', CANCELLED: 'danger'
}[s] || '')

const statusText = (s) => ({
  '1': '已支付', '2': '待制作', '2a': '制作中', '3': '待配送', '4': '配送中',
  PENDING: '待支付', COOKING: '制作中', READY: '待配送', DELIVERING: '配送中', DONE: '已完成', CANCELLED: '已取消'
}[s] || s)

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? t : d.toLocaleString('zh-CN', { hour12: false })
}

async function loadList() {
  loading.value = true
  try {
    const res = await listKitchenOrders()
    list.value = res.rows || []
    total.value = res.total || 0
  } finally { loading.value = false }
}
async function loadDashboard() {
  try { dashboard.value = await kitchenDashboard() || {} } catch (_) {}
}

async function onAccept(row) {
  await kitchenAccept(row.orderId)
  ElMessage.success('接单成功')
  loadList(); loadDashboard()
}
async function onReady(row) {
  await kitchenReady(row.orderId)
  ElMessage.success('已出餐')
  loadList(); loadDashboard()
}
function onDetail(row) {
  // 跳订单详情(已有)
  router.push({ path: '/takeout/order/index', query: { orderId: row.orderId } })
}

function handleQuery() { queryParams.value.pageNum = 1; loadList() }
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 20, orderNo: '', dateRange: [] }
  loadList()
}

import { useRouter } from 'vue-router'
const router = useRouter()

let timer = null
onMounted(() => { loadList(); loadDashboard(); timer = setInterval(() => { loadList(); loadDashboard() }, 15000) })
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style lang="scss" scoped>
.kpi-row { margin-bottom: 16px; }
.filter-card { margin-bottom: 16px; }
.kitchen-item { font-size: 12px; line-height: 1.6; }
</style>
