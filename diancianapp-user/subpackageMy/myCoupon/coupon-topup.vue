<template>
	<view class="wrap">
		<view class="wrap__coupon">
			<view class="wrap__coupon__left">
				<view class="wrap__coupon__left__title">优惠劵</view>
				<view class="wrap__coupon__left__titlebottom">
					0.00
				</view>
			</view>
			<view class="wrap__coupon__right" @click="test">
				<view>抵扣记录</view>
			</view>
		</view>

		<view class="wrap__tips">
			<view class="wrap__tips__title">使用方式</view>
			<view class="wrap__tips__desc">
				用户购买商品支付时，选择余额支付即可使用优惠券进行抵扣，优惠券抵扣比例为支付金额的 10%。
			</view>
		</view>

		<view class="wrap__tips">
			<view class="wrap__tips__title">获取方式</view>
			<view class="wrap__tips__desc">用户可通过余额充值的方式获取优惠券。</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {

			}
		},
		methods: {
			test() {
				uni.showToast({
					title: '开发中...',
					icon: 'none'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.wrap {
		&__coupon {
			background-color: #EE520C;
			color: white;
			border-radius: 18rpx;
			margin: 30rpx 30rpx 0 30rpx;
			padding: 50rpx 0 50rpx 50rpx;
			display: flex;
			justify-content: space-between;
			align-items: flex-start;

			&__left {
				&__title {
					font-size: 24rpx;
				}

				&__titlebottom {
					display: flex;
					align-items: baseline;
					margin-top: 20rpx;
					font-size: 56rpx;
				}
			}

			&__right {
				color: #313131;
				font-size: 24rpx;

				view:nth-child(1) {
					background-color: #FFFFFF;
					border-top-left-radius: 28rpx;
					border-bottom-left-radius: 28rpx;
					padding: 10rpx 15rpx 10rpx 25rpx;
				}
			}
		}

		&__grid {
			margin: 0 30rpx 0 30rpx;

			&__title {
				display: flex;
				align-items: baseline;
				color: #333;
				font-weight: bold;
				font-size: 36rpx;

				text {
					font-weight: normal;
					font-size: 24rpx;
				}
			}

			&__desc {
				margin-top: 10rpx;
				font-size: 24rpx;
			}
		}

		&__tips {
			margin: 50rpx 30rpx 0 30rpx;

			&__title {
				font-size: 32rpx;
				color: #333;
				font-weight: bold;
			}

			&__desc {
				font-size: 24rpx;
				color: $u-type-info;
				margin-top: 10rpx;
			}
		}

	}
</style>