<template>
	<view class="wrap">
		<view class="wrap__box">
			<view class="wrap__box__top">
				<span class="wrap__box__top__span">
					<u-avatar src="/static/logo.jpg" size="140"></u-avatar>
				</span>
			</view>
			<u-cell-group :border="false" bg-color="#fff">
				<u-cell-item title="姓名" bg-color="#fff" value="QXS_4vjsk4f" :arrow="false"></u-cell-item>
				<u-cell-item title="邀请码" bg-color="#fff" :arrow="false" value="4vjsk4f"></u-cell-item>
				<u-cell-item title="手机号" bg-color="#fff" :border-bottom="false" :arrow="false"
					value="13000000000"></u-cell-item>
				<u-cell-item title="注册时间" bg-color="#fff" :arrow="false" value="2026-03-03"></u-cell-item>
			</u-cell-group>
		</view>

		<view class="wrap__save">
			退出登录
		</view>
	</view>
</template>

<style lang="scss">
	@import '@/common/scss/my/set.scss';
</style>