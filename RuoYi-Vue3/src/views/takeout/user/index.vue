<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="请输入用户昵称" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="正常" value="0" />
          <el-option label="禁用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" plain icon="View" :disabled="single" @click="handleView" v-hasPermi="['takeout:user:query']">查看详情</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Lock" :disabled="single" @click="handleDisable" v-hasPermi="['takeout:user:edit']">禁用/启用</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="用户名" align="center" prop="username" width="120" />
      <el-table-column label="昵称" align="center" prop="nickname" width="120" />
      <el-table-column label="手机号" align="center" prop="phone" width="130" />
      <el-table-column label="头像" align="center" prop="avatar" width="80">
        <template #default="scope">
          <el-avatar :src="scope.row.avatar" :size="32">{{ (scope.row.nickname || scope.row.username || 'U').charAt(0) }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" prop="gender" width="70">
        <template #default="scope">
          <span>{{ genderDict[scope.row.gender] || '未知' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单数" align="center" prop="orderCount" width="80" />
      <el-table-column label="消费总额" align="center" prop="totalSpent" width="120">
        <template #default="scope">¥ {{ scope.row.totalSpent ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'danger' : 'success'">{{ scope.row.status === '1' ? '禁用' : '正常' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['takeout:user:query']">详情</el-button>
          <el-button link :type="scope.row.status === '1' ? 'success' : 'warning'" :icon="scope.row.status === '1' ? 'Unlock' : 'Lock'" @click="handleToggleStatus(scope.row)" v-hasPermi="['takeout:user:edit']">
            {{ scope.row.status === '1' ? '启用' : '禁用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 用户详情弹窗 -->
    <el-dialog title="用户详情" v-model="viewOpen" width="640px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ viewForm.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ viewForm.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ viewForm.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewForm.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderDict[viewForm.gender] || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewForm.status === '1' ? 'danger' : 'success'">{{ viewForm.status === '1' ? '禁用' : '正常' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单数">{{ viewForm.orderCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="消费总额">¥ {{ viewForm.totalSpent ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ parseTime(viewForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="头像" :span="2">
          <el-avatar :src="viewForm.avatar" :size="64">{{ (viewForm.nickname || 'U').charAt(0) }}</el-avatar>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutUser">
import { listUser, getUser, updateUser } from "@/api/takeout/user"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

// 详情弹窗
const viewOpen = ref(false)
const viewForm = ref({})

// 字典
const genderDict = { '1': '男', '2': '女' }

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    nickname: undefined,
    phone: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listUser(queryParams.value).then(res => {
    loading.value = false
    userList.value = res.rows || []
    total.value = res.total || 0
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
  ids.value = selection.map(item => item.userId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 查看详情 */
function handleView(row) {
  const userId = row.userId || ids.value[0]
  getUser(userId).then(res => {
    viewForm.value = res.data || row
    viewOpen.value = true
  })
}

/** 切换状态（禁用/启用） */
function handleToggleStatus(row) {
  const newStatus = row.status === '1' ? '0' : '1'
  const action = newStatus === '0' ? '启用' : '禁用'
  proxy.$modal.confirm(`确认${action}用户 [${row.nickname || row.username}] ？`).then(() => {
    return updateUser({ userId: row.userId, status: newStatus })
  }).then(() => {
    proxy.$modal.msgSuccess(`${action}成功`)
    getList()
  }).catch(() => {})
}

/** 工具栏批量禁用/启用 */
function handleDisable() {
  const row = userList.value.find(u => u.userId === ids.value[0])
  handleToggleStatus(row)
}

onMounted(getList)
</script>
