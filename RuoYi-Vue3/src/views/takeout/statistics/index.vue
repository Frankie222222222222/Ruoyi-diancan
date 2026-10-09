<template>
  <div class="app-container">
    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb16">
      <el-col :span="24">
        <el-card shadow="never">
          <el-button type="warning" icon="MagicStick" @click="generateTestData" :loading="genLoading">一键生成测试数据（8 张表 × 8 条）</el-button>
          <el-button type="success" icon="Refresh" @click="loadAll">刷新全部数据</el-button>
          <span style="margin-left: 12px; color: #909399; font-size: 12px">提示：测试数据生成器仅供本地调试使用</span>
        </el-card>
      </el-col>
    </el-row>

    <!-- 概览卡片 -->
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单趋势 -->
    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="card-header">
          <span>订单趋势（最近 {{ trendDays }} 天）</span>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
            <el-radio-button :value="7">7天</el-radio-button>
            <el-radio-button :value="14">14天</el-radio-button>
            <el-radio-button :value="30">30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="trendChartRef" class="chart-box"></div>
    </el-card>

    <!-- Top 三联图 -->
    <el-row :gutter="16" class="mt-16">
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header><span>营收 Top {{ topLimit }} 商家</span></template>
          <div ref="merchantChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header><span>销量 Top {{ topLimit }} 菜品</span></template>
          <div ref="dishChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header><span>配送 Top {{ topLimit }} 骑手</span></template>
          <div ref="riderChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="TakeoutStatistics">
import * as echarts from 'echarts'
import {
  getDashboard,
  getOrderTrend,
  getTopMerchants,
  getTopDishes,
  getTopRiders
} from "@/api/takeout/statistics"
import request from '@/utils/request'

const loading = ref(false)
const genLoading = ref(false)
const trendDays = ref(7)
const topLimit = ref(10)

// 4 个概览卡片（占位值，启动后被覆盖）
const cards = ref([
  { label: '订单总数', value: '-' },
  { label: '总营收 (元)', value: '-' },
  { label: '入驻商家', value: '-' },
  { label: '在岗骑手', value: '-' }
])

// 图表引用
const trendChartRef = ref(null)
const merchantChartRef = ref(null)
const dishChartRef = ref(null)
const riderChartRef = ref(null)

let trendChart, merchantChart, dishChart, riderChart

// ========== 加载概览数据 ==========
async function loadDashboard() {
  try {
    const { data } = await getDashboard()
    if (!data) return
    const map = {
      '订单总数': data.orderCount ?? data.totalOrders ?? 0,
      '总营收 (元)': data.revenue ?? data.totalRevenue ?? 0,
      '入驻商家': data.merchantCount ?? data.merchants ?? 0,
      '在岗骑手': data.riderCount ?? data.riders ?? 0
    }
    cards.value = cards.value.map(c => ({ ...c, value: formatNumber(map[c.label]) }))
  } catch (e) {
    console.error('loadDashboard failed', e)
  }
}

// ========== 订单趋势 ==========
async function loadTrend() {
  try {
    const { data } = await getOrderTrend(trendDays.value)
    const list = data || []
    const dates = list.map(x => x.date || x.day || x.orderDate)
    const counts = list.map(x => x.count ?? x.orderCount ?? 0)
    const amounts = list.map(x => x.amount ?? x.totalAmount ?? 0)
    drawTrendChart(dates, counts, amounts)
  } catch (e) {
    console.error('loadTrend failed', e)
  }
}

function drawTrendChart(dates, counts, amounts) {
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '金额 (元)'], top: 0 },
    grid: { left: 40, right: 40, bottom: 30, top: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '金额' }
    ],
    series: [
      { name: '订单数', type: 'line', smooth: true, data: counts, areaStyle: {}, itemStyle: { color: '#409EFF' } },
      { name: '金额 (元)', type: 'line', smooth: true, yAxisIndex: 1, data: amounts, itemStyle: { color: '#67C23A' } }
    ]
  })
}

// ========== Top 商家 ==========
async function loadTopMerchants() {
  try {
    const { data } = await getTopMerchants(topLimit.value)
    const list = data || []
    const names = list.map(x => x.merchantName || x.name).reverse()
    const values = list.map(x => x.revenue ?? x.totalAmount ?? x.amount ?? 0).reverse()
    drawBarChart(merchantChart, merchantChartRef, names, values, '营收 (元)')
  } catch (e) { console.error('loadTopMerchants failed', e) }
}

// ========== Top 菜品 ==========
async function loadTopDishes() {
  try {
    const { data } = await getTopDishes(topLimit.value)
    const list = data || []
    const names = list.map(x => x.dishName || x.name).reverse()
    const values = list.map(x => x.sales ?? x.quantity ?? x.count ?? 0).reverse()
    drawBarChart(dishChart, dishChartRef, names, values, '销量')
  } catch (e) { console.error('loadTopDishes failed', e) }
}

// ========== Top 骑手 ==========
async function loadTopRiders() {
  try {
    const { data } = await getTopRiders(topLimit.value)
    const list = data || []
    const names = list.map(x => x.riderName || x.name || x.realName).reverse()
    const values = list.map(x => x.deliveryCount ?? x.count ?? x.orders ?? 0).reverse()
    drawBarChart(riderChart, riderChartRef, names, values, '配送单数')
  } catch (e) { console.error('loadTopRiders failed', e) }
}

function drawBarChart(chart, refEl, names, values, name) {
  if (!chart) chart = echarts.init(refEl.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 80, right: 20, top: 20, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names, axisLabel: { fontSize: 11 } },
    series: [{
      name,
      type: 'bar',
      data: values,
      itemStyle: { color: '#409EFF', borderRadius: [0, 4, 4, 0] },
      label: { show: true, position: 'right', fontSize: 11 }
    }]
  })
}

// ========== 生成测试数据 ==========
function generateTestData() {
  proxy.$modal.confirm('确定要生成测试数据吗？将向 8 张表各插入 8 条 demo 数据（不会清空已有数据）。').then(() => {
    genLoading.value = true
    request({ url: '/takeout/testData/generate', method: 'get' }).then(res => {
      proxy.$modal.msgSuccess(res.msg || '生成成功')
      genLoading.value = false
      setTimeout(() => loadAll(), 500)
    }).catch(err => {
      genLoading.value = false
      proxy.$modal.msgError('生成失败: ' + (err.msg || err.message || '未知错误'))
    })
  }).catch(() => {})
}

function loadAll() {
  loadDashboard()
  loadTrend()
  loadTopMerchants()
  loadTopDishes()
  loadTopRiders()
}

// ========== 工具 ==========
function formatNumber(v) {
  if (v === null || v === undefined) return '-'
  const n = Number(v)
  if (isNaN(n)) return v
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

// ========== 生命周期 ==========
function handleResize() {
  trendChart?.resize()
  merchantChart?.resize()
  dishChart?.resize()
  riderChart?.resize()
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  merchantChart?.dispose()
  dishChart?.dispose()
  riderChart?.dispose()
})
</script>

<style scoped>
.app-container { padding: 16px; }
.stat-card { text-align: center; }
.stat-label { color: #909399; font-size: 13px; margin-bottom: 8px; }
.stat-value { color: #303133; font-size: 24px; font-weight: 600; }
.mt-16 { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.chart-box { width: 100%; height: 360px; }
</style>
