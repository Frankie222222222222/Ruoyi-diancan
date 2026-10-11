<template>
  <el-date-picker
    v-model="model"
    type="daterange"
    range-separator="至"
    start-placeholder="开始日期"
    end-placeholder="结束日期"
    :shortcuts="shortcuts"
    :default-time="['00:00:00', '23:59:59']"
    value-format="YYYY-MM-DD HH:mm:ss"
    :style="{ width: width }"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  width: { type: String, default: '380px' },
  presetDays: { type: Number, default: 7 }
})

const emit = defineEmits(['update:modelValue', 'change'])

const model = computed({
  get: () => props.modelValue,
  set: (v) => { emit('update:modelValue', v); emit('change', v) }
})

const shortcuts = [
  {
    text: '今日',
    value: () => {
      const d = new Date()
      return [startOfDay(d), endOfDay(d)]
    }
  },
  {
    text: '近 7 天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
      return [startOfDay(start), endOfDay(end)]
    }
  },
  {
    text: '近 30 天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 29)
      return [startOfDay(start), endOfDay(end)]
    }
  },
  {
    text: '本月',
    value: () => {
      const d = new Date()
      const start = new Date(d.getFullYear(), d.getMonth(), 1)
      const end = new Date(d.getFullYear(), d.getMonth() + 1, 0)
      return [startOfDay(start), endOfDay(end)]
    }
  }
]

function startOfDay(d) {
  const x = new Date(d)
  x.setHours(0, 0, 0, 0)
  return x
}
function endOfDay(d) {
  const x = new Date(d)
  x.setHours(23, 59, 59, 999)
  return x
}
</script>
