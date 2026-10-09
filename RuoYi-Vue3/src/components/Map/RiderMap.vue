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
    <div ref="mapContainer" class="rider-map-container" v-show="!mapError"></div>
    <div v-if="mapError" class="rider-map-error">
      <el-alert type="error" :closable="false" show-icon>
        <template #title>地图加载失败</template>
        <div class="error-msg">{{ mapError }}</div>
        <div class="error-tip">
          排查步骤:
          <ol>
            <li>打开浏览器 F12 → Console,查看红色错误(常见:INVALID_USER_KEY / USERKEY_PLAT_NOMATCH)</li>
            <li>检查高德开放平台 key 是否勾选"Web 端(JS API)"平台</li>
            <li>检查 key 的"域名白名单"是否包含 <code>localhost</code> 或留空</li>
            <li>确认浏览器能访问 <code>https://webapi.amap.com</code>(网络代理可能封禁)</li>
          </ol>
        </div>
      </el-alert>
    </div>
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
const mapError = ref('')  // 地图加载失败原因(空 = 正常)

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
  if (!mapInstance || !window.AMap) {
    // 地图还没初始化好(或加载失败),跳过 marker 绘制,避免在错误状态上叠加异常
    return
  }
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

/**
 * 轮询等待 window.AMap 加载完成(高德 CDN 可能慢,首次进 dispatch 时 script 还没好)
 * 解决"刚切到 dispatch tab 时 window.AMap 还是 undefined"导致地图空白的问题
 */
function waitForAMap(timeoutMs = 8000) {
  return new Promise((resolve, reject) => {
    if (window.AMap) return resolve(window.AMap)
    const start = Date.now()
    const timer = setInterval(() => {
      if (window.AMap) { clearInterval(timer); return resolve(window.AMap) }
      if (Date.now() - start > timeoutMs) {
        clearInterval(timer)
        return reject(new Error('等待高德 JS API 超时(8s),可能 CDN 被封或 key 不可用'))
      }
    }, 100)
  })
}

onMounted(async () => {
  await nextTick()
  if (!mapContainer.value) {
    console.error('[RiderMap] mapContainer 还未挂载到 DOM')
    return
  }
  let AMap
  try {
    AMap = await waitForAMap()
  } catch (e) {
    console.error('[RiderMap] 高德 JS API 未加载,请检查 index.html 是否引入 / 网络是否可达', e)
    mapError.value = e.message
    return
  }
  // 默认中心:杭州西湖(占位坐标,真实场景可用浏览器定位)
  try {
    mapInstance = new AMap.Map(mapContainer.value, {
      zoom: 12,
      center: [120.130890, 30.271660],
      viewMode: '2D'
    })
  } catch (e) {
    console.error('[RiderMap] AMap.Map 构造失败(常见:key 平台不匹配 / 配额耗尽 / 域名白名单不包含 localhost)', e)
    mapError.value = 'AMap.Map 构造失败:' + (e?.message || e)
    return
  }

  // 瓦片等异步资源就绪后,触发一次 resize 修掉"容器被 v-show 包过"导致的白板
  mapInstance.on('complete', () => {
    try { mapInstance && mapInstance.resize() } catch (_) {}
  })
  // 兜底:1s 后再 resize 一次,防 complete 没触发
  setTimeout(() => { try { mapInstance && mapInstance.resize() } catch (_) {} }, 1000)

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
.rider-map-error {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  min-height: 280px;
  background: #fef0f0;
}
.error-msg {
  font-size: 13px;
  color: #f56c6c;
  margin: 4px 0 8px;
  word-break: break-all;
}
.error-tip {
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
}
.error-tip code {
  background: #f5f7fa;
  padding: 1px 4px;
  border-radius: 2px;
  font-size: 12px;
}
.error-tip ol {
  margin: 4px 0 0 16px;
  padding: 0;
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
