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
      <el-form-item label="评分" prop="score">
        <el-select v-model="queryParams.score" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="n in 5" :key="n" :label="n + ' 星'" :value="String(n)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['takeout:rating:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="ratingList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="评价ID" align="center" prop="ratingId" width="90" />
      <el-table-column label="订单ID" align="center" prop="orderId" width="90" />
      <el-table-column label="用户ID" align="center" prop="userId" width="90" />
      <el-table-column label="商家ID" align="center" prop="merchantId" width="90" />
      <el-table-column label="评分" align="center" prop="score" width="180">
        <template #default="scope">
          <el-rate v-model="scope.row.score" disabled show-score :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
        </template>
      </el-table-column>
      <el-table-column label="内容" align="center" prop="content" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="商家回复" align="center" prop="reply" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="评价时间" align="center" prop="createTime" width="170">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="ChatLineRound" @click="handleReply(scope.row)" v-hasPermi="['takeout:rating:reply']">回复</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['takeout:rating:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="回复评价" v-model="replyOpen" width="500px" append-to-body>
      <el-form ref="replyRef" :model="replyForm" label-width="80px">
        <el-form-item label="原评价">
          <div style="line-height: 1.6">{{ replyForm.content || '-' }}</div>
        </el-form-item>
        <el-form-item label="回复内容" prop="reply">
          <el-input v-model="replyForm.reply" type="textarea" :rows="3" placeholder="请输入回复" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitReply">确 定</el-button>
        <el-button @click="replyOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TakeoutRating">
import { listRating, delRating, replyRating } from "@/api/takeout/rating"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const ratingList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const replyOpen = ref(false)
const replyForm = ref({ ratingId: undefined, content: undefined, reply: undefined })

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderId: undefined, userId: undefined, merchantId: undefined, score: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listRating(queryParams.value).then(res => {
    loading.value = false
    ratingList.value = res.rows || []
    total.value = res.total || 0
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(sel) {
  ids.value = sel.map(i => i.ratingId)
  single.value = sel.length != 1
  multiple.value = !sel.length
}
function handleReply(row) {
  replyForm.value = { ratingId: row.ratingId, content: row.content, reply: row.reply || '' }
  replyOpen.value = true
}
function submitReply() {
  if (!replyForm.value.reply) { proxy.$modal.msgWarning("请输入回复内容"); return }
  replyRating(replyForm.value.ratingId, replyForm.value.reply).then(() => {
    proxy.$modal.msgSuccess("回复成功"); replyOpen.value = false; getList()
  })
}
function handleDelete(row) {
  const id = row.ratingId || ids.value
  proxy.$modal.confirm('确认删除评价编号为 "' + id + '" 的数据？').then(() => delRating(id)).then(() => {
    proxy.$modal.msgSuccess("删除成功"); getList()
  }).catch(() => {})
}

onMounted(getList)
</script>
