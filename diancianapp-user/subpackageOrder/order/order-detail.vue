<template>
	<view class="wrap">
		<view v-if="type">
			<view v-if="type == 'takein'">
				<view class="wrap__takein">
					<view>{{orderData.status != '2' ? orderData.torder : '订单已退款'}}</view>
					<view v-if="orderData.status != '2'">取餐号</view>
				</view>
			</view>

			<view v-else class="wrap__takeout">
				<view class="wrap__takeout__title">
					{{orderData.orderstatus == 2 ? '订单已退款' : orderData.delivery_status == 0 ? '商家已接单' : orderData.delivery_status == 1 ? '订单配送中' : '订单已完成'}}
				</view>
				<view class="wrap__takeout__person">
					<view>配送员 Kaiyaun_Q</view>
					<view>联系Ta</view>
				</view>
				<view class="wrap__takeout__liveshooting">
					<view>送达实拍</view>
					<view>
						<u-image src="/static/logo.jpg" width="100" height="100" border-radius="18rpx"></u-image>
					</view>
				</view>
			</view>

			<view class="wrap__list">
				<view class="wrap__list__title">商品信息</view>
				<view class="wrap__list__shopinfo" v-for="(item,index) in orderData.commodity_list" :key="index">
					<view class="wrap__list__shopinfo__left">
						<view class="wrap__list__shopinfo__left__image">
							<u-image :src="item.image" width="180" height="140" border-radius="8"></u-image>
						</view>
						<view class="wrap__list__shopinfo__left__desc">
							<view>
								<view class="wrap__list__shopinfo__left__desc__shopname">{{item.name}}</view>
								<view class="wrap__list__shopinfo__left__desc__content" v-if="item.materials_text">
									{{item.materials_text}}
								</view>
							</view>
							<view class="wrap__list__shopinfo__left__desc__price">
								<text>￥</text>
								<text>{{item.price}}</text>
							</view>
						</view>
					</view>
					<view class="wrap__list__shopinfo__right">x{{item.number}}</view>
				</view>
				<view class="wrap__list__prices">
					共{{orderData.shop_num}}件商品，合计：
					<text>￥</text>
					<text>{{orderData.price}}</text>
				</view>
			</view>

			<view class="wrap__orderinfo">
				<view class="wrap__orderinfo__title">订单信息</view>
				<view v-if="type == 'takeout'">
					<view class="wrap__orderinfo__cell">
						<view>收货人/联系方式</view>
						<view>{{orderData.name}}/{{orderData.phone}}</view>
					</view>
					<view class="wrap__orderinfo__cell">
						<view>配送地址</view>
						<view>{{orderData.address}} {{orderData.house_number}}</view>
					</view>
				</view>
				<view class="wrap__orderinfo__cell">
					<view>备注</view>
					<view>{{orderData.remark ? orderData.remark : '无'}}</view>
				</view>
				<view class="wrap__orderinfo__cell">
					<view>订单号</view>
					<view>{{orderData.out_trade_no}}</view>
				</view>
				<view class="wrap__orderinfo__cell">
					<view>交易单号</view>
					<view>{{orderData.transaction_id}}</view>
				</view>
				<view class="wrap__orderinfo__cell">
					<view>支付时间</view>
					<view>{{orderData.payment_time_text}}</view>
				</view>
			</view>

			<!-- 底部按钮 -->
			<view class="wrap__bottom">
				<view @click="moreOrder">再来一单</view>
			</view>

			<!-- 间隔槽 -->
			<u-gap height="130"></u-gap>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				type: '',
				orderData: {
					"torder": 'T1',
					'status': '1',
					"orderstatus": 0,
					"delivery_status": 1,
					"commodity_list": [{
						"id": 12,
						"name": "火腿包",
						"price": 3.99,
						"number": 1,
						"image": "/static/img/menu/menu-2.jpg",
						"is_single": false,
						"materials_text": ""
					}, {
						"name": "招牌酱肉包",
						"price": 5.99,
						"number": 1,
						"image": "/static/img/menu/menu-1.jpg",
						"is_single": false,
						"materials_text": ""
					}],
					"shop_num": 2,
					"price": 9.98,
					"name": 'Kaiyuan_Q',
					"phone": '18888888888',
					"address": '北京市东城区王府井大街',
					"house_number": "88号",
					"remark": '放门口，不要打电话',
					"out_trade_no": '38fhfhs9048ujv0sjv',
					"transaction_id": '3gfr324r32r32fd23',
					"payment_time_text": '2026-03-03 17:40'
				}
			}
		},
		onLoad(param) {
			this.type = param.type;
		}
	}
</script>

<style lang="scss" scoped>
	.wrap {

		&__takein {
			background-color: white;
			margin: 20rpx 20rpx 0 20rpx;
			padding: 20rpx;
			text-align: center;
			border-radius: 18rpx;

			view:nth-child(1) {
				font-size: 38rpx;
				color: #333;
				font-weight: bold;
			}

			view:nth-child(2) {
				font-size: 24rpx;
				color: $u-type-info;
				margin-top: 10rpx;
			}
		}

		&__takeout {
			background-color: white;
			margin: 20rpx 20rpx 0 20rpx;
			padding: 20rpx;
			border-radius: 18rpx;

			&__title {
				font-size: 38rpx;
				color: #333;
				font-weight: bold;
			}

			&__person {
				display: flex;
				justify-content: space-between;
				align-items: center;
				margin: 20rpx 0;

				view:nth-child(1) {
					font-size: 24rpx;
					color: #333;
				}

				view:nth-child(2) {
					background-color: #0A3D28;
					color: white;
					border-radius: 30rpx;
					padding: 5rpx 20rpx;
					font-size: 20rpx;
				}
			}

			&__liveshooting {
				display: flex;
				justify-content: space-between;
				align-items: flex-start;

				view:nth-child(1) {
					font-size: 24rpx;
					color: #333;
				}
			}
		}

		&__list {
			margin: 20rpx 20rpx 0 20rpx;
			background-color: white;
			border-radius: 18rpx;
			padding: 20rpx;

			&__title {
				font-size: 30rpx;
				color: #333;
				font-weight: bold;
			}

			&__shopinfo {
				width: 100%;
				display: flex;
				justify-content: space-between;
				align-items: flex-end;
				margin: 20rpx 0;

				&__left {
					display: flex;

					&__image {
						margin-right: 20rpx;
					}

					&__desc {
						display: flex;
						flex-direction: column;
						justify-content: space-around;

						&__shopname {
							font-weight: bold;
							color: #333;
							font-size: 28rpx;
						}

						&__content {
							font-size: 20rpx;
							color: $u-type-info;
							margin-top: 8rpx;
						}

						&__price {
							display: flex;
							align-items: baseline;
							font-weight: bold;
							color: #333;
							margin-top: 20rpx;

							text:nth-child(1) {
								font-size: 20rpx;
								margin-right: 5rpx;
							}

							text:nth-child(2) {
								font-size: 30rpx;
							}
						}
					}
				}

				&__right {
					font-size: 24rpx;
					color: $u-type-info;
				}
			}

			&__prices {
				display: flex;
				justify-content: flex-end;
				font-size: 24rpx;
				color: #333333;
				align-items: baseline;
				margin-bottom: 20rpx;

				text {
					color: #FF362D;
					font-weight: bold;
				}

				text:nth-child(1) {
					font-size: 20rpx;
					margin-right: 5rpx;
				}

				text:nth-child(2) {
					font-size: 30rpx;
				}
			}
		}

		&__orderinfo {
			margin: 20rpx 20rpx 0 20rpx;
			background-color: white;
			border-radius: 18rpx;
			padding: 20rpx;

			&__title {
				font-size: 30rpx;
				color: #333;
				font-weight: bold;
			}

			&__cell {
				display: flex;
				justify-content: space-between;
				align-items: center;
				margin-top: 30rpx;
				width: 100%;

				view:nth-child(1) {
					color: $u-type-info;
					width: 32%;
				}

				view:nth-child(2) {
					color: #333;
					width: 68%;
					text-align: right;
				}
			}
		}

		&__bottom {
			background-color: white;
			display: flex;
			justify-content: flex-end;
			padding: 30rpx 0;
			position: fixed;
			bottom: 0;
			left: 0;
			width: 100%;

			view {
				width: 160rpx;
				text-align: center;
				border: 1px solid $u-type-info;
				color: $u-type-info;
				border-radius: 30rpx;
				padding: 8rpx 0;
				font-size: 24rpx;
				margin-right: 20rpx;
			}
		}
	}
</style>