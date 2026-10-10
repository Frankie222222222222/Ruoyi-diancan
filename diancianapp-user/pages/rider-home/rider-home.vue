<template>
	<view class="page-rider">
		<view class="header">
			<text class="header__title">🛵 骑手工作台</text>
			<text class="header__role">骑手</text>
		</view>

		<view class="tabs">
			<view :class="['tabs__item', { active: tab === 0 }]" @click="switchTab(0)">可抢订单({{ available.length }})</view>
			<view :class="['tabs__item', { active: tab === 1 }]" @click="switchTab(1)">我的订单({{ mine.length }})</view>
		</view>

		<!-- Tab 1: 可抢 -->
		<view v-if="tab === 0" class="orders">
			<view v-if="!available.length" class="empty">暂无可抢订单</view>
			<view v-for="o in available" :key="o._id" class="order-card">
				<view class="order-card__head">
					<text class="order-card__no">{{ o.order_no }}</text>
					<text class="order-card__time">{{ formatTime(o.create_time) }}</text>
				</view>
				<view class="order-card__addr">📍 {{ o.address?.address || '' }} {{ o.address?.house_number || '' }}</view>
				<view class="order-card__items">
					<view v-for="(it, i) in o.items" :key="i" class="item">
						<text>{{ it.dishName }} x {{ it.qty }}</text>
					</view>
				</view>
				<view class="order-card__total">合计: <text class="num">¥{{ o.total_amount }}</text></view>
				<button class="btn btn--grab" @click="grab(o)">抢单</button>
			</view>
		</view>

		<!-- Tab 2: 我的 -->
		<view v-if="tab === 1" class="orders">
			<view v-if="!mine.length" class="empty">暂无我的订单</view>
			<view v-for="o in mine" :key="o._id" class="order-card">
				<view class="order-card__head">
					<text class="order-card__no">{{ o.order_no }}</text>
					<text class="order-card__status">{{ statusLabel(o.rider_status) }}</text>
				</view>
				<view class="order-card__addr">📍 {{ o.address?.address || '' }}</view>
				<view class="order-card__total">¥{{ o.total_amount }}</view>
				<button v-if="o.rider_status === 'GRABBED'" class="btn btn--primary" @click="markPicked(o)">已取餐</button>
				<button v-if="o.rider_status === 'PICKED'" class="btn btn--success" @click="markDelivered(o)">已送达</button>
			</view>
		</view>
	</view>
</template>

<script>
import { unicloud } from '@/common/unicloud.js'
import { storage } from '@/common/storage.js'

export default {
	data() {
		return {
			tab: 0,
			available: [],
			mine: [],
			loading: false,
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
				const user = storage.getUser()
				if (!user) return
				const [a, m] = await Promise.all([
					unicloud.listAvailableOrders(),
					unicloud.myOrders({ riderId: user.userId }),
				])
				this.available = a.data || []
				this.mine = m.data || []
			} catch (e) {
				uni.showToast({ title: e.message, icon: 'none' })
			} finally {
				this.loading = false
			}
		},
		switchTab(i) { this.tab = i },

		async grab(o) {
			const user = storage.getUser()
			await unicloud.grabOrder({ orderId: o._id, riderId: user.userId })
			uni.showToast({ title: '抢单成功', icon: 'success' })
			this.refresh()
		},

		async markPicked(o) {
			await unicloud.updateOrderStatus({ orderId: o._id, riderStatus: 'PICKED' })
			this.refresh()
		},

		async markDelivered(o) {
			const user = storage.getUser()
			await unicloud.markDelivered({ orderId: o._id, riderId: user.userId })
			uni.showToast({ title: '已送达', icon: 'success' })
			this.refresh()
		},

		statusLabel(s) {
			return { WAITING: '等待中', GRABBED: '已抢单', PICKED: '配送中', DELIVERED: '已送达' }[s] || s
		},

		formatTime(t) {
			if (!t) return ''
			const d = new Date(t)
			return `${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
		}
	}
}
</script>

<style lang="scss" scoped>
.page-rider { padding: 20rpx; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 30rpx; background: #fff; border-radius: 12rpx; }
.header__title { font-size: 36rpx; font-weight: 600; }
.header__role { background: #409EFF; color: #fff; padding: 6rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; }

.tabs { display: flex; margin: 20rpx 0; background: #fff; border-radius: 12rpx; padding: 6rpx; }
.tabs__item { flex: 1; text-align: center; padding: 20rpx 0; border-radius: 8rpx; font-size: 28rpx; color: #666; }
.tabs__item.active { background: #409EFF; color: #fff; }

.orders { display: flex; flex-direction: column; gap: 20rpx; }
.empty { text-align: center; color: #999; padding: 60rpx; }
.order-card { background: #fff; border-radius: 12rpx; padding: 20rpx; }
.order-card__head { display: flex; justify-content: space-between; align-items: center; }
.order-card__no { font-weight: 600; }
.order-card__time { color: #999; font-size: 24rpx; }
.order-card__status { background: #409EFF; color: #fff; padding: 4rpx 16rpx; border-radius: 6rpx; font-size: 22rpx; }
.order-card__addr { color: #666; font-size: 26rpx; margin: 10rpx 0; }
.order-card__items { font-size: 24rpx; color: #999; }
.item { padding: 2rpx 0; }
.order-card__total { border-top: 1rpx solid #eee; padding-top: 16rpx; text-align: right; }
.order-card__total .num { color: #F56C6C; font-size: 32rpx; font-weight: 600; }
.btn { width: 100%; padding: 20rpx 0; border-radius: 8rpx; border: none; color: #fff; margin-top: 20rpx; font-size: 28rpx; }
.btn--grab { background: #F56C6C; }
.btn--primary { background: #409EFF; }
.btn--success { background: #67C23A; }
</style>
