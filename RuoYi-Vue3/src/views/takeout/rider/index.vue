<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="骑手姓名" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="城市" prop="city">
        <el-input v-model="queryParams.city" placeholder="城市" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="休息中" value="0" />
          <el-option label="可接单" value="1" />
          <el-option label="配送中" value="2" />
          <el-option label="禁用" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['takeout:rider:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['takeout:rider:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="riderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="骑手ID" align="center" prop="riderId" width="80" />
      <el-table-column label="姓名" align="center" prop="name" width="100" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="城市" align="center" prop="city" width="100" />
      <el-table-column label="评分" align="center" prop="rating" width="80">
        <template #default="scope">{{ scope.row.rating ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="总单数" align="center" prop="totalOrders" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-switch v-model="scope.row.status" active-value="1" inactive-value="0" @change="handleStatusChange(scope.row)" v-hasPermi="['takeout:rider:edit']"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="入职时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['takeout:rider:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:rider:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form :model="form" :rules="rules" ref="riderRef" label-width="90px">
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="请输入骑手姓名" /></el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="城市" prop="city"><el-input v-model="form.city" placeholder="如：上海" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">休息中</el-radio>
            <el-radio value="1">可接单</el-radio>
            <el-radio value="2">配送中</el-radio>
            <el-radio value="3">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutRider">
import { listRider, getRider, addRider, updateRider, delRider, changeRiderStatus } from "@/api/takeout/rider"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const riderList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, name: undefined, phone: undefined, city: undefined, status: undefined },
  form: {},
  rules: {
    name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
    phone: [
      { required: true, message: "请输入手机号", trigger: "blur" },
      { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
    ],
    city: [{ required: true, message: "请输入城市", trigger: "blur" }],
    status: [{ required: true, message: "请选择状态", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listRider(queryParams.value).then(res => {
    loading.value = false
    riderList.value = res.rows || []
    total.value = res.total || 0
  })
}

function reset() {
  form.value = { riderId: undefined, name: undefined, phone: undefined, city: undefined, status: '1', remark: undefined }
  proxy.resetForm("riderRef")
}
function handleAdd() { reset(); open.value = true; title.value = "新增骑手" }
function handleUpdate(row) {
  reset()
  getRider(row.riderId).then(res => { form.value = res.data; open.value = true; title.value = "修改骑手" })
}
function submitForm() {
  proxy.$refs["riderRef"].validate(valid => {
    if (!valid) return
    if (form.value.riderId) {
      updateRider(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList() })
    } else {
      addRider(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList() })
    }
  })
}
function handleStatusChange(row) {
  const text = row.status === '1' ? '启用' : '停用'
  proxy.$modal.confirm('确认要' + text + '该骑手吗？').then(() =>
    changeRiderStatus(row.riderId, row.status)
  ).then(() => { proxy.$modal.msgSuccess(text + "成功") }).catch(() => { row.status = row.status === '1' ? '0' : '1' })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(sel) {
  ids.value = sel.map(i => i.riderId)
  single.value = sel.length != 1
  multiple.value = !sel.length
}
function handleDelete(row) {
  const id = row.riderId || ids.value
  proxy.$modal.confirm('确认删除骑手编号为 "' + id + '" 的数据？').then(() => delRider(id)).then(() => {
    proxy.$modal.msgSuccess("删除成功"); getList()
  }).catch(() => {})
}
function cancel() { open.value = false; reset() }

onMounted(getList)
</script>
