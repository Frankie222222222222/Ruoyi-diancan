<template>
  <div class="app-container">
    <!-- 顶部查询 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单ID" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="订单ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="骑手名" prop="riderName">
        <el-input v-model="queryParams.riderName" placeholder="骑手名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 三栏布局：订单池 / 操作区 / 骑手池 -->
    <el-row :gutter="12">
      <!-- 左：待派单订单池 -->
      <el-col :span="9">
        <el-card shadow="hover" header="待派单订单池" body-style="padding: 8px">
          <div slot="header" style="display:flex;justify-content:space-between;align-items:center">
            <span><el-icon><Bell /></el-icon> 待派单订单池 ({{ pendingOrderList.length }})</span>
            <el-button size="small" icon="Refresh" @click="loadPendingOrders" circle />
          </div>
          <el-table
            ref="orderTableRef"
            :data="pendingOrderList"
            height="380"
            highlight-current-row
            @current-change="onOrderSelect"
            v-loading="orderLoading"
            size="small"
            empty-text="暂无待派单订单"
          >
            <el-table-column label="订单ID" prop="orderId" width="80" align="center" />
            <el-table-column label="订单号" prop="orderNo" width="170" :show-overflow-tooltip="true" />
            <el-table-column label="金额" width="80" align="center">
              <template #default="scope">¥{{ scope.row.totalAmount }}</template>
            </el-table-column>
            <el-table-column label="收货人" prop="receiverName" width="80" align="center" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 中：操作区 -->
      <el-col :span="6">
        <el-card shadow="hover" header="派单操作" body-style="padding: 12px">
          <template #header>
            <span><el-icon><Promotion /></el-icon> 派单操作</span>
          </template>
          <div class="ops">
            <div class="op-row">
              <el-tag size="small">订单</el-tag>
              <span class="op-val">{{ selectedOrder ? `#${selectedOrder.orderId} ${selectedOrder.receiverName || ''}` : '未选择' }}</span>
            </div>
            <div class="op-row">
              <el-tag size="small" type="success">骑手</el-tag>
              <span class="op-val">{{ selectedRider ? `${selectedRider.name} (#${selectedRider.riderId})` : '未选择' }}</span>
            </div>
            <el-divider />
            <el-button
              type="primary"
              icon="Promotion"
              :disabled="!selectedOrder || !selectedRider"
              style="width:100%;margin-bottom:8px"
              @click="handleCreateDispatch"
            >派单给该骑手</el-button>
            <el-button
              type="warning"
              icon="Refresh"
              :disabled="!selectedOrder || !selectedRider || selectedOrder._riderId === selectedRider.riderId"
              style="width:100%;margin-bottom:8px"
              @click="handleReassign"
            >改派给该骑手</el-button>
            <el-alert type="info" :closable="false" show-icon>
              <template #title>操作流程</template>
              <div style="font-size:12px;line-height:1.6">
                1. 左侧选订单<br />
                2. 右侧选骑手<br />
                3. 点派单/改派
              </div>
            </el-alert>
          </div>
        </el-card>
      </el-col>

      <!-- 右：可接单骑手池 -->
      <el-col :span="9">
        <el-card shadow="hover" body-style="padding: 8px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span><el-icon><Avatar /></el-icon> 可接单骑手池 ({{ availableRiderList.length }})</span>
              <el-button size="small" icon="Refresh" @click="loadAvailableRiders" circle />
            </div>
          </template>
          <el-table
            ref="riderTableRef"
            :data="availableRiderList"
            height="380"
            highlight-current-row
            @current-change="onRiderSelect"
            v-loading="riderLoading"
            size="small"
            empty-text="暂无可接单骑手"
          >
            <el-table-column label="骑手ID" prop="riderId" width="70" align="center" />
            <el-table-column label="姓名" prop="name" width="90" />
            <el-table-column label="手机" prop="phone" width="130" align="center" />
            <el-table-column label="评分" prop="rating" width="60" align="center" />
            <el-table-column label="进行中" prop="activeCount" width="70" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.activeCount >= 3" type="danger" size="small">{{ scope.row.activeCount }} 忙</el-tag>
                <el-tag v-else-if="scope.row.activeCount > 0" type="warning" size="small">{{ scope.row.activeCount }}</el-tag>
                <el-tag v-else type="success" size="small">空闲</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部：派单流水（Tab 切换） -->
    <el-card shadow="never" style="margin-top:12px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span><el-icon><List /></el-icon> 派单流水</span>
          <div>
            <el-button size="small" icon="Refresh" @click="loadAll">刷新</el-button>
            <el-button size="small" :type="activeTab==='active'?'primary':''" @click="switchTab('active')">进行中</el-button>
            <el-button size="small" :type="activeTab==='done'?'primary':''" @click="switchTab('done')">已完成</el-button>
            <el-button size="small" :type="activeTab==='all'?'primary':''" @click="switchTab('all')">全部</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="filteredDispatchList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="派单ID" align="center" prop="dispatchId" width="80" />
        <el-table-column label="订单ID" align="center" prop="orderId" width="80" />
        <el-table-column label="订单号" align="center" prop="orderNo" :show-overflow-tooltip="true" width="170" />
        <el-table-column label="骑手" align="center" prop="riderName" width="100" />
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ statusDict[scope.row.status] || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="派单时间" align="center" prop="dispatchTime" width="160">
          <template #default="scope"><span>{{ parseTime(scope.row.dispatchTime) }}</span></template>
        </el-table-column>
        <el-table-column label="完成时间" align="center" prop="completeTime" width="160">
          <template #default="scope"><span>{{ scope.row.completeTime ? parseTime(scope.row.completeTime) : '-' }}</span></template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
        <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
          <template #default="scope">
            <!-- 状态流转按钮组 -->
            <el-button v-if="String(scope.row.status)==='0'" link type="primary" icon="Check" @click="handleAccept(scope.row)">接单</el-button>
            <el-button v-if="String(scope.row.status)==='1'" link type="primary" icon="Box" @click="handlePickup(scope.row)">取餐</el-button>
            <el-button v-if="String(scope.row.status)==='2'" link type="primary" icon="Van" @click="handleComplete(scope.row)">送达</el-button>
            <el-button v-if="['0','1','2'].includes(String(scope.row.status))" link type="warning" icon="Refresh" @click="quickReassign(scope.row)">改派</el-button>
            <el-button v-if="['0','1','2'].includes(String(scope.row.status))" link type="danger" icon="Close" @click="handleCancel(scope.row)">取消</el-button>
            <el-button link type="info" icon="View" @click="handleView(scope.row)">详情</el-button>
            <el-button v-if="['3','4'].includes(String(scope.row.status))" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 派单详情 -->
    <el-dialog title="派单详情" v-model="detailOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="派单ID">{{ detail.dispatchId }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="骑手ID">{{ detail.riderId }}</el-descriptions-item>
        <el-descriptions-item label="骑手姓名">{{ detail.riderName }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusTagType(detail.status)">{{ statusDict[detail.status] || detail.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="派单时间">{{ parseTime(detail.dispatchTime) }}</el-descriptions-item>
        <el-descriptions-item label="接单时间">{{ parseTime(detail.acceptTime) }}</el-descriptions-item>
        <el-descriptions-item label="取餐时间">{{ parseTime(detail.pickupTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ parseTime(detail.completeTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2">{{ detail.cancelReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="骑手实时经度" :span="2">{{ detail.riderLng || '-' }}</el-descriptions-item>
        <el-descriptions-item label="骑手实时纬度" :span="2">{{ detail.riderLat || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 取消原因弹窗 -->
    <el-dialog title="取消派单" v-model="cancelOpen" width="500px" append-to-body>
      <el-form :model="cancelForm" ref="cancelRef" label-width="90px">
        <el-form-item label="派单ID"><span>{{ cancelForm.dispatchId }}</span></el-form-item>
        <el-form-item label="取消原因" prop="cancelReason">
          <el-input v-model="cancelForm.cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitCancel">确 定</el-button>
        <el-button @click="cancelOpen=false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 改派原因弹窗 -->
    <el-dialog title="改派骑手" v-model="reassignOpen" width="500px" append-to-body>
      <el-form :model="reassignForm" ref="reassignRef" label-width="90px">
        <el-form-item label="派单ID"><span>{{ reassignForm.dispatchId }}</span></el-form-item>
        <el-form-item label="原骑手"><span>{{ reassignForm.oldRiderName }} (#{{ reassignForm.oldRiderId }})</span></el-form-item>
        <el-form-item label="新骑手" prop="newRiderId">
          <el-select v-model="reassignForm.newRiderId" placeholder="请选择新骑手" filterable style="width:100%">
            <el-option
              v-for="r in availableRiderList"
              :key="r.riderId"
              :label="`${r.name} (${r.phone})`"
              :value="r.riderId"
              :disabled="r.riderId === reassignForm.oldRiderId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="改派原因">
          <el-input v-model="reassignForm.reason" type="textarea" :rows="2" placeholder="请输入改派原因" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitReassign">确 定</el-button>
        <el-button @click="reassignOpen=false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutDispatch">
import { listDispatch, getDispatch, delDispatch, createDispatch, acceptDispatch, pickupDispatch, completeDispatch, cancelDispatch, reassignDispatch, getActiveDispatchByOrder } from "@/api/takeout/dispatch"
import { listOrder } from "@/api/takeout/order"
import { listRider } from "@/api/takeout/rider"
import { parseTime } from "@/utils/ruoyi"
import { getCurrentInstance } from 'vue'
import { ref, reactive, computed, toRefs, onMounted } from 'vue'
import modal from '@/plugins/modal'

const { proxy } = getCurrentInstance()
console.log('[dispatch] proxy.$modal available:', !!proxy?.$modal)

// ===== 顶部派单流水（底部 Tab） =====
const dispatchList = ref([])
const detail = ref({})
const detailOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const activeTab = ref('active')

// ===== 订单池 =====
const pendingOrderList = ref([])
const orderLoading = ref(false)
const selectedOrder = ref(null)
const orderTableRef = ref(null)

// ===== 骑手池 =====
const availableRiderList = ref([])
const riderLoading = ref(false)
const selectedRider = ref(null)
const riderTableRef = ref(null)

// ===== 弹窗 =====
const cancelOpen = ref(false)
const reassignOpen = ref(false)

const statusDict = { '0': '待接单', '1': '已接单', '2': '取餐中', '3': '已送达', '4': '已取消' }

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderId: undefined, riderName: undefined, status: undefined },
  cancelForm: { dispatchId: undefined, cancelReason: undefined },
  reassignForm: { dispatchId: undefined, oldRiderId: undefined, oldRiderName: undefined, newRiderId: undefined, reason: undefined }
})
const { queryParams, cancelForm, reassignForm } = toRefs(data)

function statusTagType(s) {
  return ({ '0': 'info', '1': 'primary', '2': 'warning', '3': 'success', '4': 'danger' })[s] || ''
}

// 过滤底部列表（按 Tab）
const filteredDispatchList = computed(() => {
  if (activeTab.value === 'active') {
    return dispatchList.value.filter(d => ['0', '1', '2'].includes(d.status))
  } else if (activeTab.value === 'done') {
    return dispatchList.value.filter(d => ['3', '4'].includes(d.status))
  }
  return dispatchList.value
})

function switchTab(tab) {
  activeTab.value = tab
}

// ===== 加载数据 =====
function getList() {
  loading.value = true
  listDispatch(queryParams.value).then(res => {
    loading.value = false
    dispatchList.value = res.rows || []
    total.value = res.total || 0
  })
}

function loadPendingOrders() {
  orderLoading.value = true
  // 待派单：订单 status='1'(已支付)，前端无法直接判断是否有 dispatch，只能全部拉取
  listOrder({ pageNum: 1, pageSize: 50, status: '1' }).then(res => {
    const orders = res.rows || []
    // 过滤掉已有 active 派单的订单
    const dispatchedOrderIds = new Set(dispatchList.value
      .filter(d => ['0', '1', '2'].includes(d.status))
      .map(d => d.orderId))
    const filtered = orders.filter(o => !dispatchedOrderIds.has(o.orderId))
    // 补充当前订单的原骑手ID（若有 dispatch 但不active的不算待派单）
    filtered.forEach(o => { o._riderId = undefined })
    pendingOrderList.value = filtered
    orderLoading.value = false
  }).catch(() => { orderLoading.value = false })
}

function loadAvailableRiders() {
  riderLoading.value = true
  // 可接单：骑手 status='1'(可接单)
  listRider({ pageNum: 1, pageSize: 100, status: '1' }).then(res => {
    const riders = res.rows || []
    // 计算每个骑手当前进行中单数（从 dispatchList 推算）
    const activeMap = {}
    dispatchList.value.forEach(d => {
      if (['0', '1', '2'].includes(d.status) && d.riderId) {
        activeMap[d.riderId] = (activeMap[d.riderId] || 0) + 1
      }
    })
    riders.forEach(r => { r.activeCount = activeMap[r.riderId] || 0 })
    availableRiderList.value = riders
    riderLoading.value = false
  }).catch(() => { riderLoading.value = false })
}

function loadAll() {
  getList()
  loadPendingOrders()
  loadAvailableRiders()
}

// ===== 选择事件 =====
function onOrderSelect(row) {
  selectedOrder.value = row
  // 自动选中该订单原本的骑手（如果有）
  if (row && row._riderId) {
    const r = availableRiderList.value.find(x => x.riderId === row._riderId)
    if (r) riderTableRef.value.setCurrentRow(r)
  }
}
function onRiderSelect(row) { selectedRider.value = row }

// ===== 派单 / 改派（中部按钮） =====
function handleCreateDispatch() {
  if (!selectedOrder.value || !selectedRider.value) return
  modal.confirm(
    `确认将订单 #${selectedOrder.value.orderId} 派给骑手 ${selectedRider.value.name}？`
  ).then(() => {
    return createDispatch({
      orderId: selectedOrder.value.orderId,
      riderId: selectedRider.value.riderId,
      dispatchType: '0' // 0=系统派单
    })
  }).then(() => {
    modal.msgSuccess('派单成功')
    selectedOrder.value = null
    selectedRider.value = null
    orderTableRef.value.setCurrentRow()
    riderTableRef.value.setCurrentRow()
    loadAll()
  }).catch(() => {})
}

function handleReassign() {
  if (!selectedOrder.value || !selectedRider.value) return
  // 查询该订单当前进行中的派单
  getActiveDispatchByOrder(selectedOrder.value.orderId).then(res => {
    const active = res.data
    if (!active || !active.dispatchId) {
      modal.msgWarning('该订单当前没有可改派的派单（已结束/未派单）')
      return
    }
    if (active.riderId === selectedRider.value.riderId) {
      modal.msgWarning('新骑手与原骑手相同')
      return
    }
    modal.confirm(`确认将订单 #${selectedOrder.value.orderId} 从骑手 ${active.riderName} 改派给 ${selectedRider.value.name}？`).then(() => {
      return reassignDispatch(active.dispatchId, selectedRider.value.riderId, '指挥中心改派')
    }).then(() => {
      modal.msgSuccess('改派成功')
      selectedOrder.value = null
      selectedRider.value = null
      orderTableRef.value.setCurrentRow()
      riderTableRef.value.setCurrentRow()
      loadAll()
    }).catch((e) => { if (e) modal.msgError(e.message || '改派失败') })
  }).catch(() => {
    // 接口失败时退化为创建新派单
    modal.confirm(`确认将订单 #${selectedOrder.value.orderId} 派给骑手 ${selectedRider.value.name}？`).then(() => {
      return createDispatch({ orderId: selectedOrder.value.orderId, riderId: selectedRider.value.riderId, dispatchType: '0' })
    }).then(() => {
      modal.msgSuccess('派单成功')
      selectedOrder.value = null
      selectedRider.value = null
      orderTableRef.value.setCurrentRow()
      riderTableRef.value.setCurrentRow()
      loadAll()
    }).catch(() => {})
  })
}

// ===== 行内按钮：状态流转 =====
function handleAccept(row) {
  console.log('[dispatch] handleAccept', row)
  modal.confirm(`确认骑手 ${row.riderName} 接单 #${row.dispatchId}？`).then(() => {
    return acceptDispatch(row.dispatchId, row.riderId)
  }).then(() => { modal.msgSuccess('已接单'); loadAll() }).catch((e) => { if (e) modal.msgError(e.message || '操作失败') })
}

function handlePickup(row) {
  modal.confirm(`确认骑手 ${row.riderName} 已取餐 #${row.dispatchId}？`).then(() => {
    return pickupDispatch(row.dispatchId)
  }).then(() => { modal.msgSuccess('已取餐'); loadAll() }).catch((e) => { if (e) modal.msgError(e.message || '操作失败') })
}

function handleComplete(row) {
  modal.confirm(`确认订单 #${row.orderId} 已送达？`).then(() => {
    return completeDispatch(row.dispatchId)
  }).then(() => { modal.msgSuccess('已送达'); loadAll() }).catch((e) => { if (e) modal.msgError(e.message || '操作失败') })
}

function handleCancel(row) {
  cancelForm.value = { dispatchId: row.dispatchId, cancelReason: undefined }
  cancelOpen.value = true
}

function submitCancel() {
  if (!cancelForm.value.cancelReason) {
    modal.msgWarning('请输入取消原因')
    return
  }
  cancelDispatch(cancelForm.value.dispatchId, cancelForm.value.cancelReason).then(() => {
    modal.msgSuccess('已取消')
    cancelOpen.value = false
    loadAll()
  })
}

// ===== 行内按钮：改派 =====
function quickReassign(row) {
  reassignForm.value = {
    dispatchId: row.dispatchId,
    oldRiderId: row.riderId,
    oldRiderName: row.riderName,
    newRiderId: undefined,
    reason: undefined
  }
  reassignOpen.value = true
  // 顺便刷新骑手池
  loadAvailableRiders()
}

function submitReassign() {
  if (!reassignForm.value.newRiderId) {
    modal.msgWarning('请选择新骑手')
    return
  }
  reassignDispatch(reassignForm.value.dispatchId, reassignForm.value.newRiderId, reassignForm.value.reason)
    .then(() => {
      modal.msgSuccess('改派成功')
      reassignOpen.value = false
      loadAll()
    })
}

// ===== 其他 =====
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  if (proxy?.resetForm) proxy.resetForm("queryRef"); else {
    queryParams.value = { pageNum: 1, pageSize: 10, orderId: undefined, riderName: undefined, status: undefined }
  }
  handleQuery()
}
function handleSelectionChange(sel) {
  ids.value = sel.map(i => i.dispatchId)
  single.value = sel.length != 1
  multiple.value = !sel.length
}
function handleView(row) {
  getDispatch(row.dispatchId).then(res => { detail.value = res.data; detailOpen.value = true })
}
function handleDelete(row) {
  const id = row.dispatchId || ids.value
  modal.confirm('确认删除派单编号为 "' + id + '" 的数据？').then(() => delDispatch(id)).then(() => {
    modal.msgSuccess('删除成功'); loadAll()
  }).catch(() => {})
}

onMounted(loadAll)
</script>

<style scoped>
.ops { padding: 4px 0; }
.op-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}
.op-val {
  flex: 1;
  color: #303133;
  font-weight: 500;
}
</style>
