<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单ID" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="订单ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商家ID" prop="merchantId">
        <el-input v-model="queryParams.merchantId" placeholder="商家ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="complaintType">
        <el-select v-model="queryParams.complaintType" placeholder="全部" clearable style="width: 140px">
          <el-option label="商家问题" value="1" />
          <el-option label="骑手问题" value="2" />
          <el-option label="商品问题" value="3" />
          <el-option label="其他" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="待处理" value="0" />
          <el-option label="处理中" value="1" />
          <el-option label="已完成" value="2" />
          <el-option label="已驳回" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['takeout:complaint:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="complaintList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="投诉ID" align="center" prop="complaintId" width="90" />
      <el-table-column label="订单ID" align="center" prop="orderId" width="90" />
      <el-table-column label="用户ID" align="center" prop="userId" width="90" />
      <el-table-column label="商家ID" align="center" prop="merchantId" width="90" />
      <el-table-column label="类型" align="center" prop="complaintType" width="100">
        <template #default="scope">
          <el-tag :type="typeTagType(scope.row.complaintType)">{{ typeDict[scope.row.complaintType] || scope.row.complaintType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="内容" align="center" prop="content" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="处理结果" align="center" prop="handleRemark" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="赔偿金额" align="center" prop="approvedAmount" width="100">
        <template #default="scope">¥ {{ scope.row.approvedAmount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusDict[scope.row.status] || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleProcess(scope.row)" v-hasPermi="['takeout:complaint:process']">处理</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:complaint:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="处理投诉" v-model="processOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border style="margin-bottom: 16px">
        <el-descriptions-item label="投诉ID">{{ processForm.complaintId }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ processForm.orderId }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeDict[processForm.complaintType] }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ processForm.userId }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ processForm.content }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="processRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="processForm.status">
            <el-radio value="1">处理中</el-radio>
            <el-radio value="2">已完成</el-radio>
            <el-radio value="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="赔偿金额" prop="approvedAmount">
          <el-input-number v-model="processForm.approvedAmount" :min="0" :precision="2" :step="1" controls-position="right" />
          <span style="margin-left:8px;color:#909399">元</span>
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
import { listComplaint, delComplaint, processComplaint } from "@/api/takeout/complaint"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const complaintList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const processOpen = ref(false)

const typeDict = { '1': '商家问题', '2': '骑手问题', '3': '商品问题', '4': '其他' }
const statusDict = { '0': '待处理', '1': '处理中', '2': '已完成', '3': '已驳回' }

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderId: undefined, userId: undefined, merchantId: undefined, complaintType: undefined, status: undefined },
  processForm: {},
  processRules: {
    status: [{ required: true, message: "请选择处理状态", trigger: "change" }],
    handleRemark: [{ required: true, message: "请输入处理备注", trigger: "blur" }]
  }
})
const { queryParams, processForm, processRules } = toRefs(data)

function typeTagType(t) { return ({ '1': 'danger', '2': 'warning', '3': 'info', '4': '' })[t] || '' }
function statusTagType(s) { return ({ '0': 'warning', '1': 'primary', '2': 'success', '3': 'info' })[s] || '' }

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
function handleSelectionChange(sel) {
  ids.value = sel.map(i => i.complaintId)
  single.value = sel.length != 1
  multiple.value = !sel.length
}
function handleProcess(row) {
  processForm.value = {
    complaintId: row.complaintId,
    orderId: row.orderId,
    userId: row.userId,
    complaintType: row.complaintType,
    content: row.content,
    status: '1',
    approvedAmount: row.approvedAmount ?? 0,
    handleRemark: row.handleRemark || ''
  }
  processOpen.value = true
}
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
function handleDelete(row) {
  const id = row.complaintId || ids.value
  proxy.$modal.confirm('确认删除投诉编号为 "' + id + '" 的数据？').then(() => delComplaint(id)).then(() => {
    proxy.$modal.msgSuccess("删除成功"); getList()
  }).catch(() => {})
}

onMounted(getList)
</script>
