<template>
	<view class="container">
		<view v-for="(item,index) in dataList" :key="index" class="address-item" @click="chooseAddress">
			<view class="address-info">
				{{ item.address }} {{item.house_number}}
			</view>
			<view class="address-bottom">
				<view class="address-user">
					<view>{{ item.name }}</view>
					<view>{{ item.phone }}</view>
				</view>
				<view class="address-actions">
					<view>编辑</view>
					<view>删除</view>
				</view>
			</view>
		</view>

		<view class="container__footer" @tap="add">
			+ 添加地址
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'

	const dataList = ref([{
		address: '北京市东城区王府井大街',
		house_number: '88号',
		name: 'Kaiyuan_Q',
		phone: '18888888888'
	}]);

	const add = () => {
		uni.navigateTo({
			url: `/subpackageMy/myAddress/add-address`
		})
	}

	const chooseAddress = () => {
		uni.navigateBack();
	}
</script>

<style lang="scss" scoped>
	.address-item {
		margin: 0 20rpx 20rpx 20rpx;
		padding: 20rpx;
		background-color: #fff;
		border-radius: 8rpx;
		margin-top: 20rpx;

		.address-info {
			color: #333333;
			font-size: 30rpx;
			font-weight: bold;
			margin-bottom: 20rpx;
		}

		.address-bottom {
			display: flex;
			justify-content: space-between;
			align-items: center;
		}

		.address-user {
			display: flex;
			align-items: center;
			font-size: 24rpx;
			color: $u-type-info;

			view:nth-child(1) {
				margin-right: 20rpx;
			}
		}

		.address-actions {
			display: flex;
			align-items: center;
			font-size: 24rpx;

			view:nth-child(1) {
				padding-right: 15rpx;
				margin-right: 15rpx;
				border-right: 1px solid $u-type-info;
			}
		}
	}

	.container__footer {
		padding: 30rpx;
		position: fixed;
		bottom: 30rpx;
		left: 0;
		right: 0;
		width: 80%;
		margin: 0 auto;
		border-radius: 18rpx;
		text-align: center;
		padding: 30rpx 0;
		background-color: #0A3D28;
		color: white;
		font-size: 30rpx;
	}
</style>