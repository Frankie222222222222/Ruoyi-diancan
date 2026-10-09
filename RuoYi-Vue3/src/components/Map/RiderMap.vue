<template>
  <div class="rider-map-wrapper">
    <div class="rider-map-toolbar">
      <el-tag type="warning" size="small" v-if="markers.length === 0">暂无进行中骑手</el-tag>
      <el-tag type="info" size="small" v-else>共 {{ markers.length }} 个进行中骑手</el-tag>
      <el-button size="small" icon="Refresh" circle @click="reload" :loading="loading" />
      <el-switch
        v-model="autoRefresh"
        active-text="自动刷新"
        inactive-text="手动"
        size="small"
        inline-prompt
        style="margin-left: 8px"
      />
    </div>
    <div ref="mapContainer" class="rider-map-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { listActiveDispatchLocations } from '@/api/takeout/riderLocation'

defineOptions({ name: 'RiderMap' })

const mapContainer = ref(null)
const markers = ref([])
const loading = ref(false)
const autoRefresh = ref(true)

let mapInstance = null
let markerObjs = []
let infoWindow = null
let pollTimer = null

// 状态 → 颜色 (0待接单橙、1已接单蓝、2配送中绿)
const STATUS_COLOR = {
  '0': '#E6A23C',
  '1': '#409EFF',
  '2': '#67C23A'
}
const STATUS_LABEL = {
  '0': '待接单',
  '1': '已接单',
  '2': '配送中'
}

async function reload() {
  loading.value = true
  try {
    const res = await listActiveDispatchLocations()
    const rows = Array.isArray(res) ? res : (res.data || [])
    markers.value = rows
    drawMarkers(rows)
  } catch (e) {
    console.error('[RiderMap] 加载骑手位置失败', e)
  } finally {
    loading.value = false
  }
}

function drawMarkers(rows) {
  if (!mapInstance || !window.AMap) return
  // 清理旧 marker
  markerObjs.forEach(m => mapInstance.remove(m))
  markerObjs = []
  if (rows.length === 0) return

  rows.forEach(row => {
    const lng = Number(row.riderLng)
    const lat = Number(row.riderLat)
    if (Number.isNaN(lng) || Number.isNaN(lat)) return

    const color = STATUS_COLOR[row.status] || '#909399'
    const marker = new window.AMap.Marker({
      position: [lng, lat],
      content: `<div class="rider-marker" style="background:${color}">${row.riderName || row.riderId}</div>`,
      offset: new window.AMap.Pixel(-30, -30),
      extData: row
    })
    marker.on('click', () => {
      const r = row
      const updateTime = r.locationUpdateTime || '-'
      const content = `
        <div style="padding:8px;min-width:200px">
          <div style="font-weight:600;margin-bottom:6px">${r.riderName || '骑手#' + r.riderId}</div>
          <div>状态: <b style="color:${color}">${STATUS_LABEL[r.status] || r.status}</b></div>
          <div>当前订单: ${r.orderNo || '#' + r.orderId}</div>
          <div>经度: ${r.riderLng}</div>
          <div>纬度: ${r.riderLat}</div>
          <div style="color:#999;font-size:12px;margin-top:4px">上报: ${updateTime}</div>
        </div>
      `
      if (!infoWindow) infoWindow = new window.AMap.InfoWindow({ offset: new window.AMap.Pixel(0, -32) })
      infoWindow.setContent(content)
      infoWindow.open(mapInstance, [lng, lat])
    })
    markerObjs.push(marker)
    mapInstance.add(marker)
  })

  // 自适应视野(初次加载或 marker 数量变化大时)
  if (markerObjs.length > 0) {
    mapInstance.setFitView(markerObjs, false, [80, 80, 80, 80])
  }
}

onMounted(async () => {
  await nextTick()
  if (!mapContainer.value) return
  if (!window.AMap) {
    console.error('[RiderMap] 高德 JS API 未加载,请检查 index.html 是否引入')
    return
  }
  // 默认中心:杭州西湖(占位坐标,真实场景可用浏览器定位)
  mapInstance = new window.AMap.Map(mapContainer.value, {
    zoom: 12,
    center: [120.130890, 30.271660],
    viewMode: '2D'
  })
  await reload()
  // 30s 自动轮询
  if (autoRefresh.value) {
    pollTimer = setInterval(reload, 30000)
  }
})

watch(autoRefresh, (val) => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  if (val) {
    pollTimer = setInterval(reload, 30000)
  }
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
})
</script>

<style scoped>
.rider-map-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 320px;
}
.rider-map-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-bottom: 1px solid #ebeef5;
}
.rider-map-container {
  flex: 1;
  width: 100%;
  min-height: 280px;
}
:deep(.rider-marker) {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,.3);
  text-align: center;
  line-height: 1.1;
  padding: 4px;
  word-break: break-all;
}
</style>
