<template>
	<view class="wrap">
		<view class="wrap__mybalance">
			<view class="wrap__mybalance__left">
				<view class="wrap__mybalance__left__title">余额</view>
				<view class="wrap__mybalance__left__titlebottom">
					0.00
				</view>
			</view>
			<view class="wrap__mybalance__right" @click="test">
				<view>充值记录</view>
			</view>
		</view>

		<view class="wrap__grid">
			<u-grid :col="2">
				<u-grid-item v-for="(item,index) in list" :key="index">
					<view class="wrap__grid__title">
						<text>充值</text>
						{{item.recharge_amount}}
						<text>元</text>
					</view>
					<view class="wrap__grid__desc" v-if="item.coupon_reward || item.egg_reward">
						<text v-if="item.coupon_reward && Number(item.coupon_reward) > 0">
							赠{{item.coupon_reward}}优惠券
						</text>
						<text v-if="item.egg_reward && Number(item.egg_reward) > 0">
							<text v-if="item.coupon_reward && Number(item.coupon_reward) > 0"> </text>
							+ 送{{item.egg_reward}}枚鸡蛋
						</text>
					</view>
				</u-grid-item>
			</u-grid>
		</view>

		<view class="wrap__tips">
			<view class="wrap__tips__title">温馨提示</view>
			<view class="wrap__tips__desc">1.充值成功后不支持退款，请您谨慎选择充值金额。</view>
			<view class="wrap__tips__desc">2.充值余额可用于平台内消费抵扣，不设有效期，不支持提现。</view>
			<view class="wrap__tips__desc">3.充值对应金额可获得优惠券/鸡蛋奖励，奖励将实时发放至您的账户。</view>
			<view class="wrap__tips__desc">4.如有充值问题，请联系客服：xxxxxxxxxx（工作日 9:00-18:00）。</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				list: [{
						recharge_amount: 10,
						coupon_reward: 0,
						egg_reward: 0
					}, {
						recharge_amount: 20,
						coupon_reward: 0,
						egg_reward: 0
					},
					{
						recharge_amount: 100,
						coupon_reward: 10,
						egg_reward: 0
					},
					{
						recharge_amount: 200,
						coupon_reward: 50,
						egg_reward: 0
					},
					{
						recharge_amount: 500,
						coupon_reward: 500,
						egg_reward: 10
					}
				]
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
		&__mybalance {
			background-color: #1D984F;
			color: white;
			border-radius: 18rpx;
			margin: 30rpx;
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
					font-size: 22rpx;
					margin-right: 10rpx;
				}
			}

			&__desc {
				margin-top: 10rpx;
				font-size: 24rpx;
			}
		}

		&__tips {
			margin: 30rpx;

			&__title {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
			}

			&__desc {
				color: $u-type-info;
				font-size: 24rpx;
				margin-top: 10rpx;
			}
		}
	}
</style>