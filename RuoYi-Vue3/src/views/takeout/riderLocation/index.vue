<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span>骑手位置历史(实时地图)</span>
        <el-select v-model="selectedRider" placeholder="选择骑手" filterable style="width: 200px; margin-left: 16px" @change="loadLocations">
          <el-option v-for="r in riderList" :key="r.riderId" :label="`${r.name}(#${r.riderId})`" :value="r.riderId" />
        </el-select>
        <el-button type="primary" link @click="loadLocations" style="margin-left: 8px">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </template>
      <RiderMap :rider-id="selectedRider" :mock-mode="false" />
    </el-card>
    <el-card style="margin-top: 16px">
      <el-table v-loading="loading" :data="locationList" stripe>
        <el-table-column label="派单ID" prop="dispatchId" width="100" />
        <el-table-column label="订单号" prop="orderNo" min-width="160" />
        <el-table-column label="纬度" prop="lat" width="120" />
        <el-table-column label="经度" prop="lng" width="120" />
        <el-table-column label="地址" prop="address" min-width="220" show-overflow-tooltip />
        <el-table-column label="更新时间" prop="updateTime" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="TakeoutRiderLocation">
import { ref, onMounted } from 'vue'
import { listRider } from '@/api/takeout/rider'
import RiderMap from '@/components/Map/RiderMap.vue'

const loading = ref(false)
const riderList = ref([])
const selectedRider = ref(null)
const locationList = ref([])

async function loadRiders() {
  try {
    const res = await listRider()
    riderList.value = res.rows || []
    if (riderList.value.length) selectedRider.value = riderList.value[0].riderId
  } catch (_) {}
}
async function loadLocations() {
  if (!selectedRider.value) return
  loading.value = true
  try {
    // 此处简化:用 dispatch 列表 + 骑手位置 map,真实需要后端提供 /takeout/riderLocation/history/{riderId}
    locationList.value = []
  } finally { loading.value = false }
}

onMounted(() => { loadRiders() })
</script>
