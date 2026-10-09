<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width: 120px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商家" prop="merchantId">
        <el-input v-model="queryParams.merchantId" placeholder="商家ID" clearable style="width: 120px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 140px">
          <el-option label="退款申请" value="0" />
          <el-option label="投诉商家" value="1" />
          <el-option label="投诉骑手" value="2" />
          <el-option label="其他" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
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

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="complaintList">
      <el-table-column label="工单号" align="center" prop="complaintId" width="90" />
      <el-table-column label="订单号" align="center" prop="orderNo" width="160">
        <template #default="scope">
          <el-link type="primary" :underline="false" @click="showOrderDetail(scope.row)">{{ scope.row.orderNo || scope.row.orderId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="用户" align="center" prop="userNickname" width="120">
        <template #default="scope">{{ scope.row.userNickname || `用户${scope.row.userId}` }}</template>
      </el-table-column>
      <el-table-column label="商家" align="center" prop="merchantName" :show-overflow-tooltip="true" min-width="140">
        <template #default="scope">{{ scope.row.merchantName || `商家${scope.row.merchantId}` }}</template>
      </el-table-column>
      <el-table-column label="骑手" align="center" prop="riderName" width="100">
        <template #default="scope">{{ scope.row.riderName || scope.row.riderId ? `骑手${scope.row.riderId}` : '-' }}</template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag :type="typeTagType(scope.row.type)" size="small">{{ typeDict[scope.row.type] || scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="投诉原因" align="center" prop="reason" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="申请金额" align="center" prop="refundAmount" width="100">
        <template #default="scope">¥ {{ scope.row.refundAmount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="实退金额" align="center" prop="approvedAmount" width="100">
        <template #default="scope">
          <span v-if="scope.row.approvedAmount && Number(scope.row.approvedAmount) > 0" style="color:#67c23a;font-weight:600">¥ {{ scope.row.approvedAmount }}</span>
          <span v-else style="color:#999">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusDict[scope.row.status] || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="handleBy" width="100">
        <template #default="scope">{{ scope.row.handleBy || '-' }}</template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createTime" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="warning" icon="Edit" @click="handleProcess(scope.row)" v-hasPermi="['takeout:complaint:process']">处理</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- ========== 详情弹窗 ========== -->
    <el-dialog title="投诉详情" v-model="viewOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单号">{{ viewForm.complaintId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ viewForm.orderNo || viewForm.orderId }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ viewForm.userNickname || `用户${viewForm.userId}` }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ viewForm.merchantName || `商家${viewForm.merchantId}` }}</el-descriptions-item>
        <el-descriptions-item label="骑手">{{ viewForm.riderName || (viewForm.riderId ? `骑手${viewForm.riderId}` : '-') }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="typeTagType(viewForm.type)" size="small">{{ typeDict[viewForm.type] || viewForm.type }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="投诉原因" :span="2">{{ viewForm.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">¥ {{ viewForm.refundAmount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="实退金额">
          <span v-if="viewForm.approvedAmount && Number(viewForm.approvedAmount) > 0" style="color:#67c23a;font-weight:600">¥ {{ viewForm.approvedAmount }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="statusTagType(viewForm.status)" size="small">{{ statusDict[viewForm.status] || viewForm.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="凭证" :span="2">
          <template v-if="viewForm.images">
            <el-image
              v-for="(img, idx) in JSON.parse(viewForm.images)" :key="idx"
              :src="img" :preview-src-list="JSON.parse(viewForm.images)"
              style="width:80px;height:80px;margin-right:8px;border-radius:4px;" fit="cover" />
          </template>
          <span v-else style="color:#999">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{ viewForm.handleRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ viewForm.handleBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ viewForm.handleTime ? parseTime(viewForm.handleTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ parseTime(viewForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ viewForm.refundTime ? parseTime(viewForm.refundTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款流水">{{ viewForm.refundFlowNo || '-' }}</el-descriptions-item>
      </el-descriptions>
      <!-- 申诉信息 -->
      <el-divider v-if="viewForm.appealContent" content-position="left">用户申诉</el-divider>
      <el-descriptions v-if="viewForm.appealContent" :column="2" border size="small">
        <el-descriptions-item label="申诉内容" :span="2">{{ viewForm.appealContent }}</el-descriptions-item>
        <el-descriptions-item label="申诉时间">{{ parseTime(viewForm.appealTime) }}</el-descriptions-item>
        <el-descriptions-item label="申诉状态">
          <el-tag size="small" :type="{ '0':'warning','1':'success','2':'info' }[viewForm.appealStatus] || ''">
            {{ appealStatusDict[viewForm.appealStatus] || viewForm.appealStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="viewForm.appealHandle" label="申诉结果" :span="2">{{ viewForm.appealHandle }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- ========== 处理弹窗 ========== -->
    <el-dialog title="处理投诉" v-model="processOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border style="margin-bottom: 16px" size="small">
        <el-descriptions-item label="工单号">{{ processForm.complaintId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ processForm.orderNo || processForm.orderId }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ processForm.userNickname || `用户${processForm.userId}` }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ processForm.merchantName || `商家${processForm.merchantId}` }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeDict[processForm.type] || processForm.type }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">¥ {{ processForm.refundAmount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="投诉原因" :span="2">{{ processForm.reason }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="processRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="processForm.status">
            <el-radio value="1">处理中</el-radio>
            <el-radio value="2">已退款</el-radio>
            <el-radio value="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款金额" prop="approvedAmount">
          <el-input-number v-model="processForm.approvedAmount" :min="0" :max="Number(processForm.refundAmount) || 99999" :precision="2" :step="1" controls-position="right" />
          <span style="margin-left:8px;color:#909399">元（最高 ¥{{ processForm.refundAmount ?? 0 }}）</span>
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input v-model="processForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitProcess">确 定</el-button>
        <el-button @click="processOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutComplaint">
import { listComplaint, getComplaint, processComplaint } from "@/api/takeout/complaint"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const complaintList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const viewOpen = ref(false)
const processOpen = ref(false)
const viewForm = ref({})
const processForm = ref({})

/** 6种状态 + 类型 + 申诉状态字典 */
const typeDict = { '0': '退款申请', '1': '投诉商家', '2': '投诉骑手', '3': '其他' }
const statusDict = { '0': '待处理', '1': '处理中', '2': '已退款', '3': '已拒绝', '4': '已撤销', '5': '已完成' }
const appealStatusDict = { '0': '待审核', '1': '通过', '2': '驳回' }

const data = reactive({
  queryParams: {
    pageNum: 1, pageSize: 10,
    orderNo: undefined, userId: undefined, merchantId: undefined,
    type: undefined, status: undefined
  },
  processRules: {
    status: [{ required: true, message: "请选择处理状态", trigger: "change" }],
    handleRemark: [{ required: true, message: "请输入处理备注", trigger: "blur" }]
  }
})
const { queryParams, processRules } = toRefs(data)

function typeTagType(t) {
  return ({ '0': '', '1': 'danger', '2': 'warning', '3': 'info' })[t] || ''
}
function statusTagType(s) {
  return ({ '0': 'warning', '1': 'primary', '2': 'success', '3': 'info', '4': 'warning', '5': 'success' })[s] || ''
}

function getList() {
  loading.value = true
  listComplaint(queryParams.value).then(res => {
    loading.value = false
    complaintList.value = res.rows || []
    total.value = res.total || 0
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

/** 查看详情 */
function handleView(row) {
  getComplaint(row.complaintId).then(res => {
    viewForm.value = res.data || row
    viewOpen.value = true
  })
}

/** 打开处理弹窗 */
function handleProcess(row) {
  processForm.value = {
    complaintId: row.complaintId,
    orderId: row.orderId,
    orderNo: row.orderNo,
    userId: row.userId,
    userNickname: row.userNickname,
    merchantId: row.merchantId,
    merchantName: row.merchantName,
    type: row.type,
    reason: row.reason,
    refundAmount: row.refundAmount ?? 0,
    approvedAmount: row.approvedAmount ?? 0,
    handleRemark: row.handleRemark || '',
    status: '1'
  }
  processOpen.value = true
}

/** 提交处理 */
function submitProcess() {
  proxy.$refs["processRef"].validate(valid => {
    if (!valid) return
    const { complaintId, status, approvedAmount, handleRemark } = processForm.value
    processComplaint(complaintId, status, approvedAmount, handleRemark).then(() => {
      proxy.$modal.msgSuccess("处理成功")
      processOpen.value = false
      getList()
    })
  })
}

/** 点击订单号查看订单（预留，可跳转） */
function showOrderDetail(row) {
  proxy.$modal.msg(`订单号：${row.orderNo || row.orderId}`)
}

onMounted(getList)
</script>
