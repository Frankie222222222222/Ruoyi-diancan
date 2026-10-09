<template>
  <div class="app-container">
    <!-- 查询区 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="商家名称" prop="merchantName">
        <el-input
          v-model="queryParams.merchantName"
          placeholder="请输入商家名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input
          v-model="queryParams.contactName"
          placeholder="请输入联系人"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input
          v-model="queryParams.contactPhone"
          placeholder="请输入联系电话"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="营业状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="营业状态" clearable style="width: 200px">
          <el-option label="营业中" value="0" />
          <el-option label="已打烊" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="审核状态" clearable style="width: 200px">
          <el-option label="待审核" value="0" />
          <el-option label="通过" value="1" />
          <el-option label="拒绝" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['takeout:merchant:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['takeout:merchant:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['takeout:merchant:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Check"
          :disabled="single"
          @click="handleAudit"
          v-hasPermi="['takeout:merchant:audit']"
        >审核</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="merchantList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="商家编号" align="center" prop="merchantId" width="80" />
      <el-table-column label="商家名称" align="center" prop="merchantName" :show-overflow-tooltip="true" />
      <el-table-column label="联系人" align="center" prop="contactName" width="100" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="130" />
      <el-table-column label="营业状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            active-text="营业"
            inactive-text="打烊"
            inline-prompt
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['takeout:merchant:status']"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="auditStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.auditStatus === '0'" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === '1'" type="success">通过</el-tag>
          <el-tag v-else type="danger">拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="绑定用户" align="center" prop="bindUserName" width="120" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['takeout:merchant:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:merchant:remove']">删除</el-button>
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

    <!-- 新增 / 修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="merchantRef" label-width="100px">
        <el-form-item label="商家名称" prop="merchantName">
          <el-input v-model="form.merchantName" placeholder="请输入商家名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="商家地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入商家地址" maxlength="255" />
        </el-form-item>
        <el-form-item label="营业状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">营业中</el-radio>
            <el-radio value="1">已打烊</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-radio-group v-model="form.auditStatus">
            <el-radio value="0">待审核</el-radio>
            <el-radio value="1">通过</el-radio>
            <el-radio value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog title="商家审核" v-model="auditOpen" width="500px" append-to-body>
      <el-form :model="auditForm" ref="auditRef" label-width="100px">
        <el-form-item label="商家名称">
          <span>{{ auditForm.merchantName }}</span>
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio value="1">通过</el-radio>
            <el-radio value="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" placeholder="请输入审核备注" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutMerchant">
import { listMerchant, getMerchant, addMerchant, updateMerchant, delMerchant, changeMerchantStatus, auditMerchant } from "@/api/takeout/merchant"

const { proxy } = getCurrentInstance()

const merchantList = ref([])
const open = ref(false)
const auditOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    merchantName: undefined,
    contactName: undefined,
    contactPhone: undefined,
    status: undefined,
    auditStatus: undefined
  },
  form: {},
  rules: {
    merchantName: [{ required: true, message: "商家名称不能为空", trigger: "blur" }],
    contactPhone: [{ pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }]
  },
  auditForm: {
    merchantId: undefined,
    merchantName: undefined,
    auditStatus: '1',
    auditRemark: undefined
  }
})

const { queryParams, form, rules, auditForm } = toRefs(data)

function getList() {
  loading.value = true
  listMerchant(queryParams.value).then(res => {
    loading.value = false
    merchantList.value = res.rows
    total.value = res.total
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.merchantId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    merchantId: undefined,
    merchantName: undefined,
    contactName: undefined,
    contactPhone: undefined,
    address: undefined,
    status: '0',
    auditStatus: '0',
    remark: undefined
  }
  proxy.resetForm("merchantRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加商家"
}

function handleUpdate(row) {
  reset()
  const merchantId = row.merchantId || ids.value
  getMerchant(merchantId).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改商家"
  })
}

function submitForm() {
  proxy.$refs["merchantRef"].validate(valid => {
    if (valid) {
      if (form.value.merchantId != undefined) {
        updateMerchant(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMerchant(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const merchantIds = row.merchantId || ids.value
  proxy.$modal.confirm('是否确认删除商家编号为"' + merchantIds + '"的数据项？').then(() => {
    return delMerchant(merchantIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === "0" ? "营业中" : "已打烊"
  changeMerchantStatus({ merchantId: row.merchantId, status: row.status }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

function handleAudit(row) {
  const merchantId = row.merchantId || ids.value
  auditForm.value = {
    merchantId: merchantId,
    merchantName: row.merchantName,
    auditStatus: '1',
    auditRemark: undefined
  }
  auditOpen.value = true
}

function submitAudit() {
  auditMerchant(auditForm.value).then(() => {
    proxy.$modal.msgSuccess("审核完成")
    auditOpen.value = false
    getList()
  })
}

function cancel() {
  open.value = false
  reset()
}

onMounted(() => {
  getList()
})
</script>
