<template>
  <div class="app-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="搜索订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="queryParams.payMethod" placeholder="全部" clearable style="width: 160px">
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="模拟支付" value="MOCK" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="queryParams.payStatus" placeholder="全部" clearable style="width: 160px">
            <el-option label="未支付" value="UNPAID" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已退款" value="REFUND" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-row :gutter="16" style="margin-bottom: 16px">
        <el-col :span="8">
          <KpiCard title="总流水" :value="totalAmount" :precision="2" prefix="¥" tone="primary" />
        </el-col>
        <el-col :span="8">
          <KpiCard title="已支付" :value="paidCount" tone="success" />
        </el-col>
        <el-col :span="8">
          <KpiCard title="已退款" :value="refundAmount" :precision="2" prefix="¥" tone="danger" />
        </el-col>
      </el-row>
      <el-table v-loading="loading" :data="filteredList" stripe>
        <el-table-column label="支付ID" prop="paymentId" width="90" />
        <el-table-column label="订单号" prop="orderNo" min-width="160" />
        <el-table-column label="支付方式" prop="payMethod" width="100" />
        <el-table-column label="金额" prop="amount" width="120">
          <template #default="{ row }">¥ {{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 'PAID' ? 'success' : row.payStatus === 'REFUND' ? 'danger' : 'info'" disable-transitions>
              {{ row.payStatus === 'PAID' ? '已支付' : row.payStatus === 'REFUND' ? '已退款' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流水号" prop="flowNo" min-width="200" show-overflow-tooltip />
        <el-table-column label="支付时间" prop="payTime" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="TakeoutPayment">
import { ref, computed, onMounted } from 'vue'
import { listPayment } from '@/api/takeout/payment'
import KpiCard from '@/components/KpiCard/index.vue'

const loading = ref(false)
const dataList = ref([])
const queryParams = ref({ orderNo: '', payMethod: '', payStatus: '' })

const filteredList = computed(() => dataList.value.filter(p => {
  if (queryParams.value.orderNo && !(p.orderNo || '').includes(queryParams.value.orderNo)) return false
  if (queryParams.value.payMethod && p.payMethod !== queryParams.value.payMethod) return false
  if (queryParams.value.payStatus && p.payStatus !== queryParams.value.payStatus) return false
  return true
}))

const totalAmount  = computed(() => dataList.value.reduce((s, p) => s + Number(p.amount || 0), 0).toFixed(2))
const paidCount    = computed(() => dataList.value.filter(p => p.payStatus === 'PAID').length)
const refundAmount = computed(() => dataList.value.filter(p => p.payStatus === 'REFUND').reduce((s, p) => s + Number(p.amount || 0), 0).toFixed(2))

async function loadList() {
  loading.value = true
  try {
    const res = await listPayment()
    dataList.value = res.rows || []
  } finally { loading.value = false }
}
function handleQuery() {}
function resetQuery() { queryParams.value = { orderNo: '', payMethod: '', payStatus: '' } }

onMounted(loadList)
</script>

<style lang="scss" scoped>
.filter-card { margin-bottom: 16px; }
</style>
