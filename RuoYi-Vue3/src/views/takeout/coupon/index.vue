<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="名称" prop="couponName">
        <el-input v-model="queryParams.couponName" placeholder="优惠券名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="couponType">
        <el-select v-model="queryParams.couponType" placeholder="全部" clearable style="width: 130px">
          <el-option label="满减" value="1" />
          <el-option label="折扣" value="2" />
          <el-option label="无门槛" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="未开始" value="0" />
          <el-option label="进行中" value="1" />
          <el-option label="已结束" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['takeout:coupon:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['takeout:coupon:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="couponList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="优惠券ID" align="center" prop="couponId" width="90" />
      <el-table-column label="名称" align="center" prop="couponName" min-width="160" />
      <el-table-column label="类型" align="center" prop="couponType" width="90">
        <template #default="scope">
          <el-tag :type="typeTagType(scope.row.couponType)">{{ typeDict[scope.row.couponType] || scope.row.couponType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="面值/折扣" align="center" width="120">
        <template #default="scope">
          <span v-if="scope.row.couponType === '2'">{{ ((scope.row.discount || 10) * 10).toFixed(1) }} 折</span>
          <span v-else>¥ {{ scope.row.amount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="使用门槛" align="center" width="120">
        <template #default="scope">满 ¥ {{ scope.row.minAmount || 0 }}</template>
      </el-table-column>
      <el-table-column label="已领/总数" align="center" width="120">
        <template #default="scope">{{ scope.row.received || 0 }} / {{ scope.row.total || 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusDict[scope.row.status] || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="有效期" align="center" width="240">
        <template #default="scope">{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') }} ~ {{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['takeout:coupon:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:coupon:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form :model="form" :rules="rules" ref="couponRef" label-width="100px">
        <el-form-item label="名称" prop="couponName"><el-input v-model="form.couponName" placeholder="请输入优惠券名称" /></el-form-item>
        <el-form-item label="类型" prop="couponType">
          <el-radio-group v-model="form.couponType">
            <el-radio value="1">满减</el-radio>
            <el-radio value="2">折扣</el-radio>
            <el-radio value="3">无门槛</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面值(满减)" prop="amount" v-if="form.couponType !== '2'">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="1" controls-position="right" />
          <span style="margin-left:8px;color:#909399">元</span>
        </el-form-item>
        <el-form-item label="折扣" prop="discount" v-if="form.couponType === '2'">
          <el-input-number v-model="form.discount" :min="0.1" :max="9.9" :precision="1" :step="0.1" controls-position="right" />
          <span style="margin-left:8px;color:#909399">折（如 8.5 表示 85 折）</span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" :step="1" controls-position="right" />
          <span style="margin-left:8px;color:#909399">元（0 表示无门槛）</span>
        </el-form-item>
        <el-form-item label="发行总量" prop="total">
          <el-input-number v-model="form.total" :min="0" :step="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            value-format="yyyy-MM-dd HH:mm:ss"
            range-separator="-"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutCoupon">
import { listCoupon, addCoupon, updateCoupon, delCoupon } from "@/api/takeout/coupon"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const couponList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const typeDict = { '1': '满减', '2': '折扣', '3': '无门槛' }
const statusDict = { '0': '未开始', '1': '进行中', '2': '已结束' }

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, couponName: undefined, couponType: undefined, status: undefined },
  form: { dateRange: [] },
  rules: {
    couponName: [{ required: true, message: "请输入名称", trigger: "blur" }],
    couponType: [{ required: true, message: "请选择类型", trigger: "change" }],
    amount: [{ required: false }],
    discount: [{ required: false }],
    minAmount: [{ required: true, message: "请输入使用门槛", trigger: "blur" }],
    total: [{ required: true, message: "请输入发行总量", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function typeTagType(t) { return ({ '1': 'success', '2': 'warning', '3': 'info' })[t] || '' }
function statusTagType(s) { return ({ '0': 'info', '1': 'success', '2': 'danger' })[s] || '' }

function getList() {
  loading.value = true
  listCoupon(queryParams.value).then(res => {
    loading.value = false
    couponList.value = res.rows || []
    total.value = res.total || 0
  })
}

function reset() {
  form.value = { couponId: undefined, couponName: undefined, couponType: '1', amount: 0, discount: 8.5, minAmount: 0, total: 100, dateRange: [], remark: undefined }
  proxy.resetForm("couponRef")
}
function handleAdd() { reset(); open.value = true; title.value = "新增优惠券" }
function handleUpdate(row) {
  reset()
  Object.assign(form.value, row)
  if (row.startTime && row.endTime) form.value.dateRange = [row.startTime, row.endTime]
  open.value = true; title.value = "修改优惠券"
}
function submitForm() {
  proxy.$refs["couponRef"].validate(valid => {
    if (!valid) return
    if (form.value.dateRange && form.value.dateRange.length === 2) {
      form.value.startTime = form.value.dateRange[0]
      form.value.endTime = form.value.dateRange[1]
    }
    const fn = form.value.couponId ? updateCoupon : addCoupon
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.couponId ? "修改成功" : "新增成功")
      open.value = false; getList()
    })
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(sel) {
  ids.value = sel.map(i => i.couponId)
  single.value = sel.length != 1
  multiple.value = !sel.length
}
function handleDelete(row) {
  const id = row.couponId || ids.value
  proxy.$modal.confirm('确认删除优惠券编号为 "' + id + '" 的数据？').then(() => delCoupon(id)).then(() => {
    proxy.$modal.msgSuccess("删除成功"); getList()
  }).catch(() => {})
}
function cancel() { open.value = false; reset() }

onMounted(getList)
</script>
