<template>
  <div class="app-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="queryParams.orderNo" placeholder="搜索订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待处理" value="0" />
            <el-option label="处理中" value="1" />
            <el-option label="已退款" value="2" />
            <el-option label="已拒绝" value="3" />
            <el-option label="已撤销" value="4" />
            <el-option label="已完成" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="dataList" stripe>
        <el-table-column label="工单ID" prop="complaintId" width="90" />
        <el-table-column label="订单号" prop="orderNo" min-width="170" />
        <el-table-column label="用户" prop="userNickname" width="100" />
        <el-table-column label="商家" prop="merchantName" width="140" show-overflow-tooltip />
        <el-table-column label="申请金额" prop="refundAmount" width="110">
          <template #default="{ row }">¥ {{ row.refundAmount || 0 }}</template>
        </el-table-column>
        <el-table-column label="原因" prop="reason" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" disable-transitions>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '0' || row.status === '1'" type="success" link @click="approve(row)">通过</el-button>
            <el-button v-if="row.status === '0' || row.status === '1'" type="danger" link @click="reject(row)">驳回</el-button>
            <el-button link @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="loadList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="退款详情" width="640px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单ID">{{ currentRow.complaintId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentRow.userNickname }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ currentRow.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">¥ {{ currentRow.refundAmount }}</el-descriptions-item>
        <el-descriptions-item label="审批金额">¥ {{ currentRow.approvedAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(currentRow.status) }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentRow.handleBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="原因" :span="2">{{ currentRow.reason }}</el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{ currentRow.handleRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutRefund">
import { ref, reactive, onMounted } from 'vue'
import { listRefund, getRefund, approveRefund, rejectRefund } from '@/api/takeout/refund'

const loading = ref(false)
const dataList = ref([])
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 20, orderNo: '', status: '' })
const detailVisible = ref(false)
const currentRow = ref({})

const statusType = (s) => ({ '0': 'info', '1': 'warning', '2': 'success', '3': 'danger', '4': 'info', '5': '' }[s] || '')
const statusText = (s) => ({ '0': '待处理', '1': '处理中', '2': '已退款', '3': '已拒绝', '4': '已撤销', '5': '已完成' }[s] || s)

async function loadList() {
  loading.value = true
  try {
    const res = await listRefund({
      orderNo: queryParams.value.orderNo,
      status: queryParams.value.status
    })
    dataList.value = res.rows || []
    total.value = res.total || 0
  } finally { loading.value = false }
}
function handleQuery() { queryParams.value.pageNum = 1; loadList() }
function resetQuery() { queryParams.value = { pageNum: 1, pageSize: 20, orderNo: '', status: '' }; loadList() }

async function approve(row) {
  await ElMessageBox.confirm(`确认通过退款 ${row.refundAmount} 元?`, '提示', { type: 'warning' })
  await approveRefund(row.complaintId, '审核通过')
  ElMessage.success('已退款')
  loadList()
}
async function reject(row) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回退款', { inputPattern: /.{2,}/, inputErrorMessage: '至少 2 个字' })
  await rejectRefund(row.complaintId, value)
  ElMessage.success('已驳回')
  loadList()
}
async function viewDetail(row) {
  currentRow.value = await getRefund(row.complaintId)
  detailVisible.value = true
}

onMounted(loadList)
</script>

<style lang="scss" scoped>
.filter-card { margin-bottom: 16px; }
</style>
