<template>
  <div class="app-container">
    <el-card>
      <el-alert
        title="打印/扫码设置 - 功能开发中"
        type="info"
        description="本模块用于配置门店小票打印机、扫码点餐桌号二维码、订单自动打印策略。当前为占位页,后续将对接后端 /takeout/print/config 接口。"
        show-icon
        :closable="false"
      />
    </el-card>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon style="vertical-align: middle"><Printer /></el-icon> 小票打印机</span>
          </template>
          <el-form label-width="120px" :model="printerForm" disabled>
            <el-form-item label="打印机名称">
              <el-input v-model="printerForm.name" placeholder="如:厨房-1号" />
            </el-form-item>
            <el-form-item label="打印机IP">
              <el-input v-model="printerForm.ip" placeholder="192.168.1.100" />
            </el-form-item>
            <el-form-item label="打印份数">
              <el-input-number v-model="printerForm.copies" :min="1" :max="5" />
            </el-form-item>
            <el-form-item label="自动打印">
              <el-switch v-model="printerForm.autoPrint" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span><el-icon style="vertical-align: middle"><Postcard /></el-icon> 扫码点餐</span>
          </template>
          <el-form label-width="120px" :model="qrcodeForm" disabled>
            <el-form-item label="桌号前缀">
              <el-input v-model="qrcodeForm.prefix" placeholder="如:A" />
            </el-form-item>
            <el-form-item label="桌号范围">
              <el-input v-model="qrcodeForm.range" placeholder="A01-A20" />
            </el-form-item>
            <el-form-item label="点餐入口">
              <el-input v-model="qrcodeForm.entry" placeholder="小程序路径" />
            </el-form-item>
            <el-form-item label="强制关注">
              <el-switch v-model="qrcodeForm.forceFollow" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>
        <span>后续计划</span>
      </template>
      <ol>
        <li>对接后端 <code>/takeout/print/config</code> 接口</li>
        <li>支持多打印机管理(厨房/前台/吧台)</li>
        <li>桌号二维码批量生成与下载</li>
        <li>订单状态变更自动触发打印</li>
      </ol>
    </el-card>
  </div>
</template>

<script setup name="TakeoutPrint">
import { reactive } from 'vue'

const printerForm = reactive({
  name: '',
  ip: '',
  copies: 1,
  autoPrint: true
})

const qrcodeForm = reactive({
  prefix: '',
  range: '',
  entry: '',
  forceFollow: false
})
</script>

<style lang="scss" scoped>
code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  color: #d63384;
  font-family: 'Consolas', monospace;
}
</style>
