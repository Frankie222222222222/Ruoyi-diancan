<template>
	<view class="wrap">
		<view class="wrap__riderInfo">
			<view>
				<u-avatar src="/static/logo.jpg" size="80"></u-avatar>
			</view>
			<view class="wrap__riderInfo__user">
				<view>Kaiyuan_Q</view>
				<view>188****8888</view>
			</view>
		</view>

		<view class="wrap__ordervolume">
			<u-grid :col="4" :border="false">
				<u-grid-item>
					<view class="wrap__ordervolume__number">0</view>
					<view class="wrap__ordervolume__title">总完成(单)</view>
				</u-grid-item>
				<u-grid-item>
					<view class="wrap__ordervolume__number">0</view>
					<view class="wrap__ordervolume__title">今日完成(单)</view>
				</u-grid-item>
				<u-grid-item>
					<view class="wrap__ordervolume__number">0</view>
					<view class="wrap__ordervolume__title">昨日完成(单)</view>
				</u-grid-item>
				<u-grid-item>
					<view class="wrap__ordervolume__number">0</view>
					<view class="wrap__ordervolume__title">近七天完成(单)</view>
				</u-grid-item>
			</u-grid>
		</view>

		<view>
			<u-tabs :list="tabsList" :is-scroll="false" :current="current" @change="change" active-color="#0A3D28"
				bar-width="90" duration="0" inactive-color="#9A9A9A"></u-tabs>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';

	const tabsList = [{
		name: '待取货'
	}, {
		name: '配送中'
	}, {
		name: '已完成'
	}]

	const current = ref(0);

	const change = (event) => {
		current.value = event;
	}
</script>

<style lang="scss" scoped>
	.wrap {

		&__riderInfo {
			display: flex;
			background-color: white;
			padding: 30rpx 30rpx 0 30rpx;

			&__user {
				margin-left: 30rpx;

				view:nth-child(1) {
					font-size: 32rpx;
					color: #333;
					font-weight: bold;
					margin-bottom: 5rpx;
				}

				view:nth-child(2) {
					font-size: 24rpx;
					color: $u-type-info;
				}
			}
		}

		&__ordervolume {

			&__number {
				font-size: 32rpx;
				color: #333;
				font-weight: bold;
			}

			&__title {
				font-size: 24rpx;
				margin-top: 10rpx;
				color: $u-type-info;
			}
		}
	}
</style>