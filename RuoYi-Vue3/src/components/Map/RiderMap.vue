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
      <el-divider direction="vertical" style="height: 20px" />
      <el-switch
        v-model="mockMode"
        active-text="模拟模式"
        inactive-text="真实数据"
        size="small"
        inline-prompt
        style="margin-left: 4px"
      />
      <el-tooltip content="骑手数量(仅模拟模式生效)" v-if="mockMode" placement="top">
        <el-slider
          v-model="mockRiderCount"
          :min="1"
          :max="20"
          :step="1"
          style="width: 160px; margin-left: 8px"
          show-input
          input-size="small"
        />
      </el-tooltip>
      <el-tag v-if="mockMode && merchantLngLat" type="danger" size="small" style="margin-left: 4px">商家已设定</el-tag>
      <el-button
        v-if="mockMode"
        size="small"
        :type="awaitPickMerchant ? 'primary' : 'default'"
        style="margin-left: 4px"
        @click="togglePickMerchant"
      >
        {{ awaitPickMerchant ? '点击地图选商家…' : '重选商家位置' }}
      </el-button>
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
import { ElMessage } from 'element-plus'
import { listActiveDispatchLocations } from '@/api/takeout/riderLocation'

defineOptions({ name: 'RiderMap' })

const mapContainer = ref(null)
const markers = ref([])
const loading = ref(false)
const autoRefresh = ref(true)
const mapError = ref('')  // 地图加载失败原因(空 = 正常)

// --- 模拟模式响应式 state ---
const mockMode = ref(false)
const mockRiderCount = ref(6)
const mockMarkers = ref([])
const merchantLngLat = ref(null)
const awaitPickMerchant = ref(false)

let mapInstance = null
let markerObjs = []
let infoWindow = null
let pollTimer = null

// --- 模拟模式内部状态 ---
let mockMarkerObjs = []
let merchantMarker = null
let mockMoveTimer = null
// 记录骑手上一次位置，用于计算方向箭头
const lastPosition = new Map()

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
    let rows = []
    if (mockMode.value) {
      // 模拟模式:若没设过商家,自动用地图当前中心
      if (!merchantLngLat.value && mapInstance) {
        const c = mapInstance.getCenter()
        merchantLngLat.value = [c.lng, c.lat]
      }
      if (mockMarkers.value.length !== mockRiderCount.value || mockMarkers.value.length === 0) {
        regenerateMockRiders()
      } else {
        drawMockMarkers()
      }
      rows = mockMarkers.value
    } else {
      const res = await listActiveDispatchLocations()
      rows = Array.isArray(res) ? res : (res.data || [])
    }
    markers.value = rows
    if (!mockMode.value) drawMarkers(rows)
  } catch (e) {
    console.error('[RiderMap] 加载骑手位置失败', e)
  } finally {
    loading.value = false
  }
}

/* ================== 模拟模式相关 ================== */

function togglePickMerchant() {
  if (!mapInstance || !window.AMap) {
    ElMessage.warning('地图尚未加载完成,稍后再试')
    return
  }
  awaitPickMerchant.value = !awaitPickMerchant.value
  if (awaitPickMerchant.value) {
    // 一次性监听,取地图上第一个落点作为商家
    mapInstance.once('click', onMapClickPickMerchant)
    ElMessage.info('请在地图上点击选商家位置')
  } else {
    mapInstance.off('click', onMapClickPickMerchant)
  }
}

function onMapClickPickMerchant(e) {
  merchantLngLat.value = [e.lnglat.lng, e.lnglat.lat]
  awaitPickMerchant.value = false
  ElMessage.success('商家位置已设定')
  if (mockMode.value) regenerateMockRiders()
}

function regenerateMockRiders() {
  if (!merchantLngLat.value) return
  const [cLng, cLat] = merchantLngLat.value
  const surnames = ['张', '李', '王', '赵', '钱', '孙', '周', '吴', '郑', '冯', '陈', '杨']
  const rows = []
  for (let i = 0; i < mockRiderCount.value; i++) {
    const angle = Math.random() * 2 * Math.PI
    const dist = 300 + Math.random() * 1700  // 300m - 2km
    const dLng = (dist * Math.cos(angle)) / 111320
    const dLat = (dist * Math.sin(angle)) / 110540
    rows.push({
      dispatchId: 900000 + i,
      orderId: 800000 + i,
      orderNo: 'MOCK' + (800000 + i),
      riderId: 700 + i,
      riderName: surnames[i % surnames.length] + '骑手' + (i + 1),
      riderPhone: '1390000' + String(7000 + i).padStart(4, '0'),
      status: String(i % 3),
      lng: cLng + dLng,
      lat: cLat + dLat,
      angle: Math.random() * 2 * Math.PI
    })
  }
  mockMarkers.value = rows
  drawMockMarkers()
}

function drawMockMarkers() {
  if (!mapInstance || !window.AMap) return
  // 1. 商家 marker
  if (merchantMarker) {
    mapInstance.remove(merchantMarker)
    merchantMarker = null
  }
  if (merchantLngLat.value) {
    merchantMarker = new window.AMap.Marker({
      position: merchantLngLat.value,
      content: '<div class="merchant-marker">🏪</div>',
      offset: new window.AMap.Pixel(-24, -48),
      zIndex: 200
    })
    mapInstance.add(merchantMarker)
  }
  // 2. 骑手 marker
  mockMarkerObjs.forEach(m => mapInstance.remove(m))
  mockMarkerObjs = []
  mockMarkers.value.forEach(r => {
    const color = STATUS_COLOR[r.status] || '#909399'
    const heading = computeHeading(r.riderId, r.lng, r.lat)
    const marker = new window.AMap.Marker({
      position: [r.lng, r.lat],
      content: `<div class="rider-marker" style="--marker-color:${color};--heading:${heading}deg"><span class="name">${r.riderName}</span></div>`,
      offset: new window.AMap.Pixel(-20, -27),
      extData: r
    })
    marker.on('click', () => {
      const content = `<div style="padding:8px;min-width:200px">
        <div style="font-weight:600;margin-bottom:6px">${r.riderName} (模拟)</div>
        <div>状态: <b style="color:${color}">${STATUS_LABEL[r.status] || r.status}</b></div>
        <div>订单: ${r.orderNo}</div>
        <div>经度: ${r.lng.toFixed(6)}</div>
        <div>纬度: ${r.lat.toFixed(6)}</div>
        <div style="color:#999;font-size:12px;margin-top:4px">演示数据,不写入数据库</div>
      </div>`
      if (!infoWindow) infoWindow = new window.AMap.InfoWindow({ offset: new window.AMap.Pixel(0, -32) })
      infoWindow.setContent(content)
      infoWindow.open(mapInstance, [r.lng, r.lat])
      pulseMarker(marker)
    })
    mockMarkerObjs.push(marker)
    mapInstance.add(marker)
  })
  // 3. 自适应视野
  if (merchantLngLat.value) {
    mapInstance.setCenter(merchantLngLat.value)
    mapInstance.setZoom(14)
  } else if (mockMarkerObjs.length > 0) {
    mapInstance.setFitView(mockMarkerObjs, false, [80, 80, 80, 80])
  }
}

function startMockMove() {
  stopMockMove()
  mockMoveTimer = setInterval(() => {
    if (!mockMode.value) return
    mockMarkers.value.forEach(r => {
      if (Math.random() < 0.3) {
        r.angle = Math.random() * 2 * Math.PI
      }
      const step = 1.5 + Math.random() * 1.5  // 1.5-3m / s
      const dLng = (step * Math.cos(r.angle || 0)) / 111320
      const dLat = (step * Math.sin(r.angle || 0)) / 110540
      r.lng += dLng
      r.lat += dLat
    })
    // 仅移动 marker,顺带刷新方向箭头
    mockMarkerObjs.forEach((m, idx) => {
      const r = mockMarkers.value[idx]
      if (!r) return
      m.setPosition([r.lng, r.lat])
      const heading = computeHeading(r.riderId, r.lng, r.lat)
      const dom = m.getDomElement && m.getDomElement()
      const inner = dom && dom.querySelector('.rider-marker')
      if (inner) inner.style.setProperty('--heading', heading + 'deg')
    })
  }, 1000)
}

function stopMockMove() {
  if (mockMoveTimer) {
    clearInterval(mockMoveTimer)
    mockMoveTimer = null
  }
}

/**
 * 计算骑手朝向角度（0=北，顺时针增加；用于 .rider-marker::after 的 --heading）
 * 算法：拿"上一位置→当前位置"的位移，atan2(dLng, dLat)
 * 因为高德地图 y 向下为正，所以 dLat>0 表示向南，atan2(dLng, dLat) 直接得出"从北顺时针"的角度
 */
function computeHeading(riderId, lng, lat) {
  const prev = lastPosition.get(riderId)
  let heading = 0
  if (prev) {
    const dLng = lng - prev.lng
    const dLat = lat - prev.lat
    // 阈值：移动距离 < 0.5m 视为静止，保持上次方向
    const dist = Math.sqrt(dLng * dLng + dLat * dLat)
    if (dist > 1e-5) {
      heading = Math.atan2(dLng, dLat) * 180 / Math.PI
    } else if (prev.heading !== undefined) {
      heading = prev.heading
    }
  }
  lastPosition.set(riderId, { lng, lat, heading })
  return heading
}

/**
 * 触发 marker 脉冲动画（点击反馈）
 */
function pulseMarker(markerObj) {
  try {
    const dom = markerObj && markerObj.getDomElement && markerObj.getDomElement()
    const inner = dom && dom.querySelector('.rider-marker')
    if (!inner) return
    inner.classList.add('pulse')
    setTimeout(() => inner.classList.remove('pulse'), 3000)
  } catch (_) { /* noop */ }
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
    const heading = computeHeading(row.riderId, lng, lat)
    const marker = new window.AMap.Marker({
      position: [lng, lat],
      content: `<div class="rider-marker" style="--marker-color:${color};--heading:${heading}deg"><span class="name">${row.riderName || row.riderId}</span></div>`,
      offset: new window.AMap.Pixel(-20, -27),
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
      pulseMarker(marker)
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

// 模拟模式切换:开启时启动移动定时器 + 清真实数据 + 立刻重画;关闭时停定时器 + 清空 mock + 拉真实数据
watch(mockMode, (val) => {
  if (val) {
    // 清掉真实数据 marker
    markerObjs.forEach(m => mapInstance && mapInstance.remove(m))
    markerObjs = []
    // 没设过商家,用当前地图中心
    if (mapInstance && !merchantLngLat.value) {
      merchantLngLat.value = [mapInstance.getCenter().lng, mapInstance.getCenter().lat]
    }
    // 清空旧位置缓存,防止真实数据的 heading 干扰模拟
    lastPosition.clear()
    startMockMove()
    regenerateMockRiders()
  } else {
    stopMockMove()
    // 清 mock marker
    mockMarkerObjs.forEach(m => mapInstance && mapInstance.remove(m))
    mockMarkerObjs = []
    if (merchantMarker) {
      mapInstance.remove(merchantMarker)
      merchantMarker = null
    }
    mockMarkers.value = []
    // 清空缓存,切回真实数据时重新计算 heading
    lastPosition.clear()
    // 切回真实数据
    reload()
  }
})

// 骑手数变化:重新生成(仅模拟模式)
watch(mockRiderCount, () => {
  if (mockMode.value) regenerateMockRiders()
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
  stopMockMove()
  if (merchantMarker && mapInstance) {
    mapInstance.remove(merchantMarker)
    merchantMarker = null
  }
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  // 清理骑手位置缓存
  lastPosition.clear()
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
/* 骑手头像：40px 圆 + 状态色描边 + 阴影 */
:deep(.rider-marker) {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid var(--marker-color, #909399);
  background: #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.35);
  box-sizing: border-box;
  cursor: pointer;
  transition: transform 0.15s ease;
}
:deep(.rider-marker:hover) {
  transform: scale(1.08);
}
/* 名字字标 */
:deep(.rider-marker .name) {
  position: relative;
  z-index: 1;
  color: #303133;
  letter-spacing: -0.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 36px;
}
/* 底部方向箭头：朝下的小三角，绝对定位 */
:deep(.rider-marker::after) {
  content: '';
  position: absolute;
  bottom: -7px;
  left: 50%;
  transform: translateX(-50%) rotate(var(--heading, 0deg));
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 9px solid var(--marker-color, #909399);
  transform-origin: 50% 30%;
}
/* 点击时的脉冲外圈 */
:deep(.rider-marker.pulse::before) {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  border: 2px solid var(--marker-color, #909399);
  animation: riderPulse 1.2s ease-out infinite;
  pointer-events: none;
}
@keyframes riderPulse {
  0% { transform: scale(0.8); opacity: 0.9; }
  100% { transform: scale(1.6); opacity: 0; }
}

/* 商家：店铺形小图标 */
:deep(.merchant-marker) {
  width: 48px;
  height: 48px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f56c6c;
  border-radius: 8px 8px 8px 2px;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  color: #fff;
  font-size: 22px;
  box-sizing: border-box;
  cursor: pointer;
  transition: transform 0.15s ease;
}
:deep(.merchant-marker:hover) {
  transform: scale(1.08);
}
:deep(.merchant-marker::after) {
  content: '';
  position: absolute;
  bottom: -7px;
  left: 6px;
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 7px solid #f56c6c;
}
</style>
