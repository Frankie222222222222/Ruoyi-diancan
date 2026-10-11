<template>
  <div class="kpi-card" :class="['kpi-card--' + tone, { 'kpi-card--loading': loading }]">
    <div class="kpi-card__head">
      <span class="kpi-card__title">{{ title }}</span>
      <el-icon v-if="icon" class="kpi-card__icon"><component :is="icon" /></el-icon>
    </div>
    <div class="kpi-card__value">{{ displayValue }}</div>
    <div v-if="trend !== null && trend !== undefined" class="kpi-card__trend">
      <el-tag :type="trend >= 0 ? 'success' : 'danger'" size="small" effect="plain">
        {{ trend >= 0 ? '+' : '' }}{{ trend }}%
      </el-tag>
      <span class="kpi-card__trend-label">较上周期</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  prefix: { type: String, default: '' },
  suffix: { type: String, default: '' },
  precision: { type: Number, default: 0 },
  icon: { type: [String, Object, Function], default: null },
  trend: { type: Number, default: null },
  tone: { type: String, default: 'primary' }, // primary | success | warning | danger | info
  loading: { type: Boolean, default: false }
})

const displayValue = computed(() => {
  if (props.value === null || props.value === undefined) return '--'
  const num = Number(props.value)
  if (Number.isNaN(num)) return props.value
  return props.prefix + num.toFixed(props.precision) + props.suffix
})
</script>

<style lang="scss" scoped>
.kpi-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;

  &:hover { box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08); }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #909399;
    font-size: 14px;
  }

  &__title { font-weight: 500; }
  &__icon  { font-size: 20px; }

  &__value {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    line-height: 1.2;
  }

  &__trend {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #909399;
  }

  &--primary &__value { color: #0A3D28; }
  &--success &__value { color: #1D984F; }
  &--warning &__value { color: #E6A23C; }
  &--danger  &__value { color: #F56C6C; }
  &--info    &__value { color: #909399; }

  &--loading { opacity: 0.6; }
}
</style>
