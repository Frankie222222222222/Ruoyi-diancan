<template>
	<view class="page-my">
		<view class="card">
			<image class="avatar" :src="userInfo?.avatar || '/static/logo.jpg'"></image>
			<view class="info">
				<view class="name">{{ userInfo?.nickname || '未登录' }}</view>
				<view class="phone">{{ userInfo?.phone || '' }}</view>
				<view :class="['role-tag', 'role-tag--' + (userInfo?.role || 'user')]">
					{{ roleLabel }}
				</view>
			</view>
		</view>

		<view class="menu">
			<view class="menu__item" @click="goMyOrders">
				<text>📋 我的订单</text>
				<text class="arrow">›</text>
			</view>
			<view class="menu__item" @click="goAddress">
				<text>📍 收货地址</text>
				<text class="arrow">›</text>
			</view>
			<view class="menu__item" @click="switchRole">
				<text>🔄 切换角色(开发用)</text>
				<text class="arrow">›</text>
			</view>
		</view>

		<button class="logout-btn" @click="logout">退出登录</button>
	</view>
</template>

<script>
import { useStore } from 'vuex'
import { ROLE_LABEL } from '@/common/role.js'

export default {
	setup() {
		const store = useStore()
		return { store }
	},
	computed: {
		userInfo() { return this.store.state.userInfo },
		roleLabel() { return ROLE_LABEL[this.store.state.role] || '游客' },
	},
	onShow() {},
	methods: {
		goMyOrders() { uni.showToast({ title: 'TODO: 我的订单', icon: 'none' }) },
		goAddress()  { uni.showToast({ title: 'TODO: 收货地址', icon: 'none' }) },
		switchRole() {
			uni.showActionSheet({
				itemList: ['顾客(user)', '后厨(kitchen)', '骑手(rider)'],
				success: (res) => {
					const map = ['user', 'kitchen', 'rider']
					const newRole = map[res.tapIndex]
					const user = { ...this.userInfo, role: newRole }
					this.store.commit('SET_ROLE', newRole)
					this.store.commit('SET_USER', user)
					uni.reLaunch({ url: this.store.getters.homePath })
				}
			})
		},
		logout() {
			uni.showModal({
				title: '提示', content: '确定要退出登录吗?',
				success: (r) => { if (r.confirm) this.store.dispatch('logout') }
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.page-my { padding: 20rpx; }
.card { display: flex; background: #fff; border-radius: 12rpx; padding: 30rpx; align-items: center; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 60rpx; }
.info { margin-left: 30rpx; flex: 1; }
.name { font-size: 32rpx; font-weight: 600; }
.phone { color: #999; font-size: 24rpx; margin-top: 6rpx; }
.role-tag { display: inline-block; padding: 4rpx 16rpx; border-radius: 6rpx; font-size: 22rpx; margin-top: 10rpx; }
.role-tag--user    { background: #ecf5ff; color: #409EFF; }
.role-tag--kitchen { background: #fdf6ec; color: #E6A23C; }
.role-tag--rider   { background: #ecf5ff; color: #409EFF; }
.role-tag--admin   { background: #fef0f0; color: #F56C6C; }

.menu { background: #fff; border-radius: 12rpx; margin-top: 20rpx; }
.menu__item { display: flex; justify-content: space-between; padding: 28rpx 30rpx; border-bottom: 1rpx solid #f5f5f5; }
.menu__item:last-child { border-bottom: none; }
.arrow { color: #ccc; }

.logout-btn { background: #F56C6C; color: #fff; margin-top: 40rpx; border-radius: 12rpx; padding: 24rpx 0; font-size: 28rpx; }
</style>
