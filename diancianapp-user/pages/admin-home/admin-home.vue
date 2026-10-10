<template>
	<view class="page-admin">
		<view class="header">
			<text class="header__title">⚙️ 管理后台</text>
			<text class="header__role">管理员</text>
		</view>

		<view class="grid">
			<view class="grid__item" @click="goUserList">
				<text class="grid__icon">👥</text>
				<text class="grid__label">用户管理</text>
			</view>
			<view class="grid__item" @click="goOrderList">
				<text class="grid__icon">📋</text>
				<text class="grid__label">订单管理</text>
			</view>
			<view class="grid__item" @click="goDishList">
				<text class="grid__icon">🍱</text>
				<text class="grid__label">菜品管理</text>
			</view>
			<view class="grid__item" @click="goRoleConfig">
				<text class="grid__icon">🎭</text>
				<text class="grid__label">角色配置</text>
			</view>
		</view>

		<view class="info">
			<text>提示: 大部分管理功能请访问 RuoYi 后台</text>
			<text class="info__url">http://localhost:8080</text>
		</view>
	</view>
</template>

<script>
	export default {
		methods: {
			goUserList()  { uni.showToast({ title: 'TODO: 用户管理', icon: 'none' }) },
			goOrderList() { uni.showToast({ title: 'TODO: 订单管理', icon: 'none' }) },
			goDishList()  { uni.showToast({ title: 'TODO: 菜品管理', icon: 'none' }) },
			goRoleConfig(){ uni.showToast({ title: 'TODO: 角色配置', icon: 'none' }) },
		}
	}
</script>

<style lang="scss" scoped>
.page-admin { padding: 20rpx; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 30rpx; background: #fff; border-radius: 12rpx; }
.header__title { font-size: 36rpx; font-weight: 600; }
.header__role { background: #F56C6C; color: #fff; padding: 6rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; }

.grid { display: flex; flex-wrap: wrap; gap: 20rpx; margin: 20rpx 0; }
.grid__item { width: calc(50% - 10rpx); background: #fff; border-radius: 12rpx; padding: 40rpx 0; text-align: center; }
.grid__icon { display: block; font-size: 60rpx; }
.grid__label { display: block; font-size: 28rpx; color: #333; margin-top: 10rpx; }

.info { background: #fff; border-radius: 12rpx; padding: 30rpx; text-align: center; color: #999; font-size: 24rpx; }
.info__url { display: block; color: #409EFF; margin-top: 10rpx; }
</style>
