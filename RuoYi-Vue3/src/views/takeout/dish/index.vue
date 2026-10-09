<template>
  <div class="app-container">
    <!-- 查询区 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="菜品名称" prop="dishName">
        <el-input
          v-model="queryParams.dishName"
          placeholder="请输入菜品名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属商家" prop="merchantId">
        <el-select v-model="queryParams.merchantId" placeholder="请选择商家" clearable style="width: 240px">
          <el-option
            v-for="m in merchantOptions"
            :key="m.merchantId"
            :label="m.merchantName"
            :value="m.merchantId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="菜品分类" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="请选择菜品分类" clearable style="width: 240px">
          <el-option
            v-for="item in categoryOptions"
            :key="item.categoryId"
            :label="item.categoryName"
            :value="item.categoryId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="菜品状态" clearable style="width: 240px">
          <el-option label="上架" value="1" />
          <el-option label="下架" value="0" />
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
          v-hasPermi="['takeout:dish:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['takeout:dish:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['takeout:dish:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="dishList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="菜品编号" align="center" prop="dishId" width="80" />
      <el-table-column label="菜品图片" align="center" width="80">
        <template #default="scope">
          <image-preview v-if="scope.row.image" :src="scope.row.image" :width="40" :height="40" />
          <span v-else style="color: #c0c4cc">无</span>
        </template>
      </el-table-column>
      <el-table-column label="菜品名称" align="center" prop="dishName" :show-overflow-tooltip="true" />
      <el-table-column label="分类" align="center" prop="categoryName" width="120" />
      <el-table-column label="所属商家" align="center" prop="merchantName" width="160" :show-overflow-tooltip="true" />
      <el-table-column label="价格(元)" align="center" prop="price" width="100">
        <template #default="scope">
          <span>¥ {{ scope.row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存" align="center" prop="stock" width="80" />
      <el-table-column label="销量" align="center" prop="sales" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.sales > 50 ? 'success' : (scope.row.sales > 0 ? 'warning' : 'info')">
            {{ scope.row.sales }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['takeout:dish:edit']"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['takeout:dish:edit']">修改</el-button>
          <el-button link type="primary" icon="Tickets" @click="openOrderSource(scope.row)">订单来源</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:dish:remove']">删除</el-button>
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
      <el-form :model="form" :rules="rules" ref="dishRef" label-width="100px">
        <el-form-item label="菜品名称" prop="dishName">
          <el-input v-model="form.dishName" placeholder="请输入菜品名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择菜品分类" style="width: 100%">
            <el-option
              v-for="item in categoryOptions"
              :key="item.categoryId"
              :label="item.categoryName"
              :value="item.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属商家" prop="merchantId">
          <el-select v-model="form.merchantId" placeholder="请选择所属商家" style="width: 100%">
            <el-option
              v-for="m in merchantOptions"
              :key="m.merchantId"
              :label="m.merchantName"
              :value="m.merchantId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品图片" prop="image">
          <image-upload v-model="form.image" :limit="1" :file-size="5" :file-type="['png', 'jpg', 'jpeg', 'gif', 'bmp']" />
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :precision="2" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="1">上架</el-radio>
            <el-radio value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入菜品描述" maxlength="500" />
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

    <!-- 订单来源弹窗 -->
    <el-dialog title="订单来源" v-model="sourceOpen" width="900px" append-to-body>
      <div style="margin-bottom: 12px">
        <el-tag>菜品：{{ sourceDishName }}</el-tag>
        <el-tag type="info" style="margin-left: 8px">共 {{ sourceOrders.length }} 个订单</el-tag>
      </div>
      <el-table v-loading="sourceLoading" :data="sourceOrders" size="small" max-height="500">
        <el-table-column label="订单号" prop="orderNo" :show-overflow-tooltip="true" />
        <el-table-column label="商家" prop="merchantName" :show-overflow-tooltip="true" width="160" />
        <el-table-column label="收货人" prop="receiverName" width="100" />
        <el-table-column label="电话" prop="receiverPhone" width="130" />
        <el-table-column label="订单金额" prop="totalAmount" width="110">
          <template #default="scope">¥ {{ scope.row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ statusDict[scope.row.status] || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutDish">
import { listDish, getDish, addDish, updateDish, delDish, listOrdersByDishId } from "@/api/takeout/dish"
import { optionselectCategory } from "@/api/takeout/dishCategory"
import { listMerchant } from "@/api/takeout/merchant"
import { getOrderStatusDict } from "@/api/takeout/order"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const dishList = ref([])
const categoryOptions = ref([])
const merchantOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

// 订单来源弹窗
const sourceOpen = ref(false)
const sourceLoading = ref(false)
const sourceOrders = ref([])
const sourceDishName = ref('')
const statusDict = ref({})

// 状态 tag 颜色
function statusTagType(s) {
  switch (s) {
    case '0': return 'info'
    case '1': return 'primary'
    case '2': return 'warning'
    case '3': return 'warning'
    case '4': return 'success'
    case '5': return 'success'
    case '6': return 'danger'
    case '7': return 'danger'
    default: return ''
  }
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dishName: undefined,
    categoryId: undefined,
    merchantId: undefined,
    status: undefined
  },
  form: {},
  rules: {
    dishName: [{ required: true, message: "菜品名称不能为空", trigger: "blur" }],
    categoryId: [{ required: true, message: "请选择菜品分类", trigger: "change" }],
    merchantId: [{ required: true, message: "请选择所属商家", trigger: "change" }],
    price: [{ required: true, message: "价格不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getCategoryOptions() {
  optionselectCategory().then(res => {
    categoryOptions.value = res.data
  })
}

function getMerchantOptions() {
  listMerchant({ pageNum: 1, pageSize: 200 }).then(res => {
    merchantOptions.value = (res.rows || []).map(m => ({ merchantId: m.merchantId, merchantName: m.merchantName }))
  })
}

function getList() {
  loading.value = true
  listDish(queryParams.value).then(res => {
    loading.value = false
    dishList.value = res.rows
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
  ids.value = selection.map(item => item.dishId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    dishId: undefined,
    dishName: undefined,
    categoryId: undefined,
    merchantId: undefined,
    image: undefined,
    price: 0,
    stock: 0,
    sales: 0,
    status: '1',
    description: undefined,
    remark: undefined
  }
  proxy.resetForm("dishRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加菜品"
}

function handleUpdate(row) {
  reset()
  const dishId = row.dishId || ids.value
  getDish(dishId).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改菜品"
  })
}

function submitForm() {
  proxy.$refs["dishRef"].validate(valid => {
    if (valid) {
      if (form.value.dishId != undefined) {
        updateDish(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDish(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const dishIds = row.dishId || ids.value
  proxy.$modal.confirm('是否确认删除菜品编号为"' + dishIds + '"的数据项？').then(() => {
    return delDish(dishIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === "1" ? "上架" : "下架"
  updateDish(row).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {
    row.status = row.status === "1" ? "0" : "1"
  })
}

function cancel() {
  open.value = false
  reset()
}

function openOrderSource(row) {
  sourceDishName.value = row.dishName
  sourceOrders.value = []
  sourceOpen.value = true
  sourceLoading.value = true
  listOrdersByDishId(row.dishId).then(res => {
    sourceOrders.value = res.data || []
    sourceLoading.value = false
  }).catch(() => { sourceLoading.value = false })
}

onMounted(() => {
  getCategoryOptions()
  getMerchantOptions()
  getOrderStatusDict().then(res => { statusDict.value = res.data || {} })
  getList()
})
</script>
