<template>
  <div class="amap-picker">
    <el-input
      v-model="keyword"
      placeholder="搜索地点 / 点击地图选点"
      clearable
      @keyup.enter="search"
    >
      <template #append>
        <el-button @click="search" :loading="searching">
          <el-icon><Search /></el-icon>
        </el-button>
      </template>
    </el-input>
    <div ref="mapEl" class="amap-picker__map"></div>
    <div class="amap-picker__row">
      <el-input v-model="address" placeholder="详细地址" readonly />
      <el-input-number v-model="lat" :precision="6" :step="0.0001" placeholder="纬度" />
      <el-input-number v-model="lng" :precision="6" :step="0.0001" placeholder="经度" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  modelAddress: { type: String, default: '' },
  modelLat: { type: Number, default: null },
  modelLng: { type: Number, default: null }
})
const emit = defineEmits(['update:modelAddress', 'update:modelLat', 'update:modelLng', 'select'])

const mapEl = ref(null)
const keyword = ref('')
const address = ref(props.modelAddress || '')
const lat = ref(props.modelLat)
const lng = ref(props.modelLng)
const searching = ref(false)
let map = null
let marker = null
let AMap = null
let geocoder = null

watch(() => [props.modelLat, props.modelLng, props.modelAddress], ([nlat, nlng, naddr]) => {
  if (nlat !== null && nlat !== undefined) lat.value = nlat
  if (nlng !== null && nlng !== undefined) lng.value = nlng
  if (naddr) address.value = naddr
})

function emitAll() {
  emit('update:modelAddress', address.value)
  emit('update:modelLat', lat.value)
  emit('update:modelLng', lng.value)
  emit('select', { address: address.value, lat: lat.value, lng: lng.value })
}

function pickPoint(la, ln) {
  lat.value = la
  lng.value = ln
  if (marker) marker.setMap(null)
  marker = new AMap.Marker({ position: [la, ln], map })
  // 反向地理编码
  if (geocoder) {
    geocoder.getAddress([la, ln], (status, result) => {
      if (status === 'complete' && result.regeocode) {
        address.value = result.regeocode.formattedAddress
        emitAll()
      } else {
        emitAll()
      }
    })
  } else {
    emitAll()
  }
}

async function search() {
  if (!keyword.value) return
  searching.value = true
  try {
    AMap.plugin('AMap.PlaceSearch', () => {
      const ps = new AMap.PlaceSearch({ city: '全国' })
      ps.search(keyword.value, (status, result) => {
        searching.value = false
        if (status === 'complete' && result.poiList && result.poiList.pois.length) {
          const p = result.poiList.pois[0]
          map.setCenter([p.location.lng, p.location.lat])
          pickPoint(p.location.lat, p.location.lng)
          address.value = p.address
          emitAll()
        }
      })
    })
  } catch (e) {
    searching.value = false
  }
}

onMounted(async () => {
  // 延迟引入 AMap(在 main.js 全局引入后可省略)
  if (window.AMap) {
    AMap = window.AMap
  } else {
    // 动态加载
    await new Promise((resolve, reject) => {
      const s = document.createElement('script')
      s.src = 'https://webapi.amap.com/maps?v=2.0&key=' + (import.meta.env.VITE_AMAP_KEY || '')
      s.onload = resolve
      s.onerror = reject
      document.head.appendChild(s)
    })
    AMap = window.AMap
  }
  if (!mapEl.value) return
  const center = (lat.value && lng.value) ? [lng.value, lat.value] : [120.130890, 30.271660]
  map = new AMap.Map(mapEl.value, { zoom: 14, center })
  AMap.plugin(['AMap.Marker', 'AMap.Geocoder', 'AMap.ToolBar'], () => {
    geocoder = new AMap.Geocoder({ city: '全国' })
    map.addControl(new AMap.ToolBar())
  })
  map.on('click', (e) => pickPoint(e.lnglat.lat, e.lnglat.lng))
  if (lat.value && lng.value) pickPoint(lat.value, lng.value)
})

onBeforeUnmount(() => { if (map) map.destroy() })
</script>

<style lang="scss" scoped>
.amap-picker {
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__map {
    width: 100%;
    height: 360px;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
  }

  &__row {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr;
    gap: 12px;
  }
}
</style>
