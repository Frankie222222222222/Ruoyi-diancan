<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="用户昵称" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="handleQuery" />
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
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['takeout:user:remove']">删除</el-button>
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
          <el-avatar :src="scope.row.avatar" :size="32">{{ (scope.row.nickname || 'U').charAt(0) }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" prop="gender" width="70">
        <template #default="scope">
          <el-tag :type="scope.row.gender === '1' ? 'primary' : 'danger'" size="small">
            {{ scope.row.gender === '1' ? '男' : (scope.row.gender === '2' ? '女' : '未知') }}
          </el-tag>
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
      <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:user:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="TakeoutUser">
import { listUser, delUser } from "@/api/takeout/user"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, nickname: undefined, phone: undefined, status: undefined }
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
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(sel) {
  ids.value = sel.map(i => i.userId)
  single.value = sel.length != 1
  multiple.value = !sel.length
}
function handleDelete(row) {
  const id = row.userId || ids.value
  proxy.$modal.confirm('确认删除用户编号为 "' + id + '" 的数据？').then(() => delUser(id)).then(() => {
    proxy.$modal.msgSuccess("删除成功"); getList()
  }).catch(() => {})
}

onMounted(getList)
</script>
