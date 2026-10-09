<template>
  <div class="app-container">
    <!-- 顶部统计卡 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" body-style="padding: 16px">
          <div style="color: #909399; font-size: 12px">总菜品数</div>
          <div style="font-size: 24px; font-weight: 600">{{ rankList.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" body-style="padding: 16px">
          <div style="color: #909399; font-size: 12px">总销量</div>
          <div style="font-size: 24px; font-weight: 600; color: #67C23A">{{ totalSales }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" body-style="padding: 16px">
          <div style="color: #909399; font-size: 12px">在售菜品</div>
          <div style="font-size: 24px; font-weight: 600; color: #409EFF">{{ onSaleCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" body-style="padding: 16px">
          <div style="color: #909399; font-size: 12px">低库存预警（&lt;10）</div>
          <div style="font-size: 24px; font-weight: 600; color: #E6A23C">{{ lowStockCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <el-form :model="queryParams" :inline="true" label-width="80px">
      <el-form-item label="商家" prop="merchantId">
        <el-select v-model="queryParams.merchantId" placeholder="全部商家" clearable style="width: 240px" @change="getList">
          <el-option v-for="m in merchantOptions" :key="m.merchantId" :label="m.merchantName" :value="m.merchantId" />
        </el-select>
      </el-form-item>
      <el-form-item label="Top" prop="limit">
        <el-select v-model="queryParams.limit" style="width: 120px" @change="getList">
          <el-option label="Top 10" :value="10" />
          <el-option label="Top 20" :value="20" />
          <el-option label="Top 50" :value="50" />
          <el-option label="Top 100" :value="100" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="getList">刷新</el-button>
      </el-form-item>
    </el-form>

    <!-- 销量榜 -->
    <el-table v-loading="loading" :data="rankList" border stripe highlight-current-row>
      <el-table-column label="排名" align="center" width="80">
        <template #default="scope">
          <span v-if="scope.$index === 0" style="display:inline-block;width:24px;height:24px;line-height:24px;border-radius:50%;background:#F56C6C;color:#fff;font-weight:600">{{ scope.$index + 1 }}</span>
          <span v-else-if="scope.$index === 1" style="display:inline-block;width:24px;height:24px;line-height:24px;border-radius:50%;background:#E6A23C;color:#fff;font-weight:600">{{ scope.$index + 1 }}</span>
          <span v-else-if="scope.$index === 2" style="display:inline-block;width:24px;height:24px;line-height:24px;border-radius:50%;background:#909399;color:#fff;font-weight:600">{{ scope.$index + 1 }}</span>
          <span v-else>{{ scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="菜品" align="center" min-width="200">
        <template #default="scope">
          <div style="display: flex; align-items: center; gap: 8px; justify-content: center">
            <image-preview v-if="scope.row.image" :src="scope.row.image" :width="36" :height="36" />
            <span>{{ scope.row.dishName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分类" align="center" prop="categoryName" width="120" />
      <el-table-column label="所属商家" align="center" prop="merchantName" :show-overflow-tooltip="true" width="160" />
      <el-table-column label="价格" align="center" prop="price" width="100">
        <template #default="scope">¥ {{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column label="销量" align="center" prop="sales" width="120" sortable>
        <template #default="scope">
          <el-tag :type="scope.row.sales >= 100 ? 'success' : (scope.row.sales >= 10 ? 'warning' : 'info')" effect="dark">
            {{ scope.row.sales }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="库存" align="center" prop="stock" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.stock === 0 ? 'danger' : (scope.row.stock < 10 ? 'warning' : 'success')">
            {{ scope.row.stock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '1'" type="success">在售</el-tag>
          <el-tag v-else type="info">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="销售占比" align="center" width="160">
        <template #default="scope">
          <el-progress
            :percentage="totalSales ? Math.round(scope.row.sales * 1000 / totalSales) / 10 : 0"
            :stroke-width="14"
            :show-text="true"
          />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup name="TakeoutDishSales">
import { topSales } from "@/api/takeout/dish"
import { listMerchant } from "@/api/takeout/merchant"

const rankList = ref([])
const merchantOptions = ref([])
const loading = ref(false)

const queryParams = reactive({
  merchantId: undefined,
  limit: 20
})

const totalSales = computed(() => rankList.value.reduce((s, r) => s + (Number(r.sales) || 0), 0))
const onSaleCount = computed(() => rankList.value.filter(r => r.status === '1').length)
const lowStockCount = computed(() => rankList.value.filter(r => Number(r.stock) < 10).length)

function getList() {
  loading.value = true
  topSales(queryParams).then(res => {
    rankList.value = res.data || []
    loading.value = false
  }).catch(() => { loading.value = false })
}

function loadMerchants() {
  listMerchant({ pageNum: 1, pageSize: 200 }).then(res => {
    merchantOptions.value = (res.rows || []).map(m => ({ merchantId: m.merchantId, merchantName: m.merchantName }))
  })
}

onMounted(() => {
  loadMerchants()
  getList()
})
</script>
