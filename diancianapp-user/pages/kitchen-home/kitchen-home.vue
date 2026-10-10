<template>
	<view class="page-kitchen">
		<view class="header">
			<text class="header__title">👨‍🍳 后厨工作台</text>
			<text class="header__role">{{ roleLabel }}</text>
		</view>

		<view class="stats">
			<view class="stats__item">
				<text class="stats__num">{{ stats.pending }}</text>
				<text class="stats__label">待接单</text>
			</view>
			<view class="stats__item">
				<text class="stats__num">{{ stats.cooking }}</text>
				<text class="stats__label">制作中</text>
			</view>
			<view class="stats__item">
				<text class="stats__num">{{ stats.ready }}</text>
				<text class="stats__label">已出餐</text>
			</view>
		</view>

		<view class="orders">
			<view v-if="loading" class="empty">加载中…</view>
			<view v-else-if="!orders.length" class="empty">暂无待处理订单</view>
			<view v-for="o in orders" :key="o._id" class="order-card">
				<view class="order-card__head">
					<text class="order-card__no">{{ o.order_no }}</text>
					<text class="order-card__time">{{ formatTime(o.create_time) }}</text>
				</view>
				<view class="order-card__user">
					{{ o.user_nickname }} · {{ o.user_phone }}
				</view>
				<view class="order-card__items">
					<view v-for="(it, i) in o.items" :key="i" class="item">
						<text>{{ it.dishName }} x {{ it.qty }}</text>
						<text>¥{{ it.subtotal }}</text>
					</view>
				</view>
				<view v-if="o.remark" class="order-card__remark">📝 {{ o.remark }}</view>
				<view class="order-card__total">
					合计: <text class="num">¥{{ o.total_amount }}</text>
				</view>
				<view class="order-card__btns">
					<button v-if="o.kitchen_status === 'PENDING'" class="btn btn--primary" @click="accept(o)">接单</button>
					<button v-if="o.kitchen_status === 'COOKING'" class="btn btn--success" @click="finishCooking(o)">出餐</button>
					<span class="badge" v-if="o.kitchen_status === 'READY'">✓ 等骑手取餐</span>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { unicloud } from '@/common/unicloud.js'
import { ROLE_LABEL } from '@/common/role.js'

export default {
	data() {
		return {
			orders: [],
			loading: false,
			roleLabel: '后厨',
		}
	},
	computed: {
		stats() {
			return {
				pending: this.orders.filter(o => o.kitchen_status === 'PENDING').length,
				cooking: this.orders.filter(o => o.kitchen_status === 'COOKING').length,
				ready: this.orders.filter(o => o.kitchen_status === 'READY').length,
			}
		}
	},
	onShow() {
		this.refresh()
	},
	onPullDownRefresh() {
		this.refresh().finally(() => uni.stopPullDownRefresh())
	},
	methods: {
		async refresh() {
			this.loading = true
			try {
				const res = await unicloud.listKitchenOrders(['PENDING', 'COOKING', 'READY'])
				this.orders = res.data || []
			} catch (e) {
				uni.showToast({ title: e.message, icon: 'none' })
			} finally {
				this.loading = false
			}
		},

		async accept(o) {
			await unicloud.updateOrderStatus({ orderId: o._id, kitchenStatus: 'COOKING' })
			uni.showToast({ title: '已接单', icon: 'success' })
			this.refresh()
		},

		async finishCooking(o) {
			await unicloud.updateOrderStatus({ orderId: o._id, kitchenStatus: 'READY' })
			uni.showToast({ title: '已出餐', icon: 'success' })
			this.refresh()
		},

		formatTime(t) {
			if (!t) return ''
			const d = new Date(t)
			return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
		}
	}
}
</script>

<style lang="scss" scoped>
.page-kitchen { padding: 20rpx; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 30rpx; background: #fff; border-radius: 12rpx; }
.header__title { font-size: 36rpx; font-weight: 600; }
.header__role { background: #E6A23C; color: #fff; padding: 6rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; }

.stats { display: flex; margin: 20rpx 0; gap: 20rpx; }
.stats__item { flex: 1; background: #fff; padding: 30rpx; border-radius: 12rpx; text-align: center; }
.stats__num { display: block; font-size: 48rpx; font-weight: 600; color: #E6A23C; }
.stats__label { display: block; font-size: 24rpx; color: #999; margin-top: 10rpx; }

.orders { display: flex; flex-direction: column; gap: 20rpx; }
.empty { text-align: center; color: #999; padding: 60rpx; }
.order-card { background: #fff; border-radius: 12rpx; padding: 20rpx; }
.order-card__head { display: flex; justify-content: space-between; }
.order-card__no { font-weight: 600; }
.order-card__time { color: #999; font-size: 24rpx; }
.order-card__user { color: #666; font-size: 24rpx; margin-top: 6rpx; }
.order-card__items { margin: 16rpx 0; }
.item { display: flex; justify-content: space-between; padding: 4rpx 0; }
.order-card__remark { color: #E6A23C; font-size: 24rpx; margin: 8rpx 0; }
.order-card__total { border-top: 1rpx solid #eee; padding-top: 16rpx; text-align: right; }
.order-card__total .num { color: #F56C6C; font-size: 32rpx; font-weight: 600; }
.order-card__btns { display: flex; gap: 20rpx; margin-top: 20rpx; }
.btn { flex: 1; font-size: 28rpx; padding: 16rpx 0; border-radius: 8rpx; border: none; color: #fff; }
.btn--primary { background: #409EFF; }
.btn--success { background: #67C23A; }
.badge { background: #67C23A; color: #fff; padding: 10rpx 20rpx; border-radius: 8rpx; font-size: 24rpx; flex: 1; text-align: center; }
</style>
