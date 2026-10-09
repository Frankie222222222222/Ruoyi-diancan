<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="商家" prop="merchantId">
        <el-select v-model="queryParams.merchantId" placeholder="全部商家" clearable style="width: 180px">
          <el-option v-for="m in merchantOptions" :key="m.merchantId" :label="m.merchantName" :value="m.merchantId" />
        </el-select>
      </el-form-item>
      <el-form-item label="评分" prop="score">
        <el-select v-model="queryParams.score" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="n in 5" :key="n" :label="n + ' 星'" :value="String(n)" />
        </el-select>
      </el-form-item>
      <el-form-item label="回复状态" prop="replyStatus">
        <el-select v-model="queryParams.replyStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="未回复" value="0" />
          <el-option label="已回复" value="1" />
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
      <el-table-column label="评价ID" align="center" prop="ratingId" width="80" />
      <!-- 订单号 instead of 订单ID -->
      <el-table-column label="订单号" align="center" prop="orderNo" min-width="150" :show-overflow-tooltip="true" />
      <!-- 用户名 instead of 用户ID -->
      <el-table-column label="用户" align="center" prop="userNickname" min-width="100" :show-overflow-tooltip="true" />
      <!-- 商家名 instead of 商家ID -->
      <el-table-column label="商家" align="center" prop="merchantName" min-width="160" :show-overflow-tooltip="true" />
      <!-- 骑手名 instead of 骑手ID -->
      <el-table-column label="骑手" align="center" prop="riderName" min-width="100" :show-overflow-tooltip="true">
        <template #default="{ row }">{{ row.riderName || '-' }}</template>
      </el-table-column>
      <!-- 综合评分 + 分项星星 -->
      <el-table-column label="商家评分" align="center" prop="merchantScore" width="200">
        <template #default="scope">
          <el-rate v-model="scope.row.merchantScore" disabled show-score text-color="#ff9900" :score-template="scope.row.merchantScore + ' 分'" />
        </template>
      </el-table-column>
      <!-- 口味/包装/配送 分项小星星 -->
      <el-table-column label="分项" align="center" width="160">
        <template #default="scope">
          <span class="sub-score">
            <span class="sub-label">口味</span>
            <el-rate v-model="scope.row.tasteScore" disabled size="small" :colors="['#99A9BF','#F7BA2A','#FF9900']" style="margin-left:2px" />
          </span>
          <span class="sub-score">
            <span class="sub-label">包装</span>
            <el-rate v-model="scope.row.packagingScore" disabled size="small" :colors="['#99A9BF','#F7BA2A','#FF9900']" style="margin-left:2px" />
          </span>
          <span class="sub-score">
            <span class="sub-label">配送</span>
            <el-rate v-model="scope.row.deliveryScore" disabled size="small" :colors="['#99A9BF','#F7BA2A','#FF9900']" style="margin-left:2px" />
          </span>
        </template>
      </el-table-column>
      <el-table-column label="评价内容" align="center" prop="content" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="商家回复" align="center" prop="reply" :show-overflow-tooltip="true" min-width="180">
        <template #default="{ row }">
          <span :class="{ 'text-muted': !row.reply }">{{ row.reply || '暂无回复' }}</span>
        </template>
      </el-table-column>
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

    <el-dialog title="回复评价" v-model="replyOpen" width="600px" append-to-body>
      <el-form ref="replyRef" :model="replyForm" label-width="80px">
        <el-form-item label="订单号">{{ replyForm.orderNo || '-' }}</el-form-item>
        <el-form-item label="商家">{{ replyForm.merchantName || '-' }}</el-form-item>
        <el-form-item label="用户">{{ replyForm.userNickname || '-' }}</el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="replyForm.merchantScore" disabled />
        </el-form-item>
        <el-form-item label="原评价">
          <div style="line-height:1.6; color:#606266">{{ replyForm.content || '-' }}</div>
        </el-form-item>
        <el-divider />
        <el-form-item label="回复内容" prop="reply">
          <el-input v-model="replyForm.reply" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入回复内容" />
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
import { listRating, listMerchantSimple, delRating, replyRating } from "@/api/takeout/rating"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const ratingList = ref([])
const merchantOptions = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const replyOpen = ref(false)
const replyForm = ref({ ratingId: undefined, orderNo: undefined, merchantName: undefined, userNickname: undefined, merchantScore: undefined, content: undefined, reply: undefined })

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    merchantId: undefined,
    score: undefined,
    replyStatus: undefined
  }
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

function getMerchantOptions() {
  listMerchantSimple().then(res => {
    merchantOptions.value = res.data || []
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
  replyForm.value = {
    ratingId: row.ratingId,
    orderNo: row.orderNo,
    merchantName: row.merchantName,
    userNickname: row.userNickname,
    merchantScore: row.merchantScore,
    content: row.content,
    reply: row.reply || ''
  }
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

onMounted(() => { getList(); getMerchantOptions() })
</script>

<style scoped>
.sub-score {
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 22px;
}
.sub-label {
  font-size: 12px;
  color: #909399;
  width: 28px;
  flex-shrink: 0;
}
.text-muted {
  color: #c0c4cc;
  font-style: italic;
}
/* 缩小 el-rate 在分项列里的星星尺寸 */
:deep(.el-rate--small .el-rate__icon) {
  font-size: 12px;
}
</style>
