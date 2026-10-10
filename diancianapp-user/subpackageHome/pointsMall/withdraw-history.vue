<template>
	<view class="wrap">
		<view class="wrap__for" v-if="withdrawList.length != 0">
			<view v-for="(item,index) in withdrawList" :key="index" class="wrap__for__box">
				<view class="wrap__for__box__left">
					<view>提现成功</view>
					<view>{{item.createtime}}</view>
				</view>
				<view class="wrap__for__box__right">-{{item.apply_amount}}元</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue'

	const withdrawList = ref([{
		"apply_amount": 50,
		"createtime": '2026-01-03 19:52'
	}, {
		"apply_amount": 20,
		"createtime": '2026-01-02 09:38'
	}, {
		"apply_amount": 50,
		"createtime": '2026-01-01 13:55'
	}]);
</script>

<style lang="scss" scoped>
	.wrap {

		&__for {
			background-color: white;
			margin: 20rpx;
			padding: 1rpx 30rpx 30rpx 30rpx;
			border-radius: 18rpx;

			&__box {
				display: flex;
				justify-content: space-between;
				align-items: center;
				margin-top: 30rpx;

				&__left {
					view:nth-child(1) {
						color: #333;
						font-weight: bold;
					}

					view:nth-child(2) {
						color: $u-type-info;
						font-size: 24rpx;
						margin-top: 10rpx;
					}
				}

				&__right {
					color: #1D984F;
				}
			}
		}
	}
</style>