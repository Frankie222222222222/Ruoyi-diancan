<template>
	<uni-popup ref="popup" type="bottom" @change="change">
		<view class="cart-popup">
			<view class="header">
				<view class="order-type">
					<view class="font-weight-bold">已加购商品</view>
				</view>
				<view class="d-flex align-items-center" @tap="clear">
					<u-icon size="23" name="trash"></u-icon>
					<view>清空</view>
				</view>
			</view>
			<scroll-view scroll-y class="content">
				<view class="wrapper">
					<view class="list">
						<view class="item" v-for="(item, index) in cart" :key="index">
							<view class="left">
								<image :src="item.image" mode="widthFix" class="image"></image>
							</view>
							<view class="right">
								<view class="name-and-materials">
									<view class="name">{{ item.name }}</view>
									<view class="materials" v-if="item.materials_text">{{ item.materials_text }}</view>
								</view>
								<view class="price-and-actions">
									<view class="prices">
										<text>￥</text>
										<text>{{ item.price }}</text>
									</view>
									<actions :number="item.number" @add="add(item)" @minus="minus(item)"></actions>
								</view>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
	</uni-popup>
</template>

<script>
	import uniPopup from '@/components/uni-popup/uni-popup.vue'
	import actions from '@/components/actions/actions.vue'

	export default {
		components: {
			uniPopup,
			actions
		},
		props: {
			cart: {
				type: Array,
				default: () => []
			}
		},
		methods: {
			open() {
				this.$refs['popup'].open()
			},
			close() {
				this.$refs['popup'].close()
			},
			change({
				show
			}) {
				this.$emit('change', show)
			},
			add(item) {
				this.$emit('add', item)
			},
			minus(item) {
				this.$emit('minus', item)
			},
			clear() {
				this.$emit('clear')
			}
		}
	};
</script>

<style lang="scss" scoped>
	.cart-popup {
		background-color: $bg-color-white;
		padding-bottom: 100rpx;
	}

	.header {
		padding: 20rpx 30rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		border-bottom: 1rpx solid #F5F5F5;
		font-size: $font-size-sm;
		color: $text-color-assist;

		.order-type {
			font-size: $font-size-sm;
			color: $text-color-base;
		}

		// .delete-btn {
		// 	width: 46rpx;
		// 	height: 46rpx;
		// }
	}

	.content {
		max-height: calc(100vh - 600rpx);

		.wrapper {
			width: 100%;
			height: 100%;
			padding: 0 30rpx;
		}

		.list {
			display: flex;
			flex-direction: column;
			margin-bottom: 30rpx;

			.item {
				display: flex;
				align-items: stretch;
				padding-top: 30rpx;
				position: relative;

				&:after {
					content: ' ';
					position: absolute;
					bottom: 0;
					left: 180rpx;
					right: 0;
					// border-bottom: 1rpx solid rgba($color: $border-color, $alpha: 0.6);
				}

				.left {
					flex-shrink: 0;
					display: flex;
					align-items: center;

					.image {
						width: 180rpx;
						height: 135rpx;
						margin-right: 20rpx;
						border-radius: 8rpx;
					}
				}

				.right {
					flex: 1;
					display: flex;
					flex-direction: column;
					justify-content: space-between;
					font-size: $font-size-medium;
					color: $text-color-base;

					.name-and-materials {
						display: flex;
						flex-direction: column;
						margin-bottom: 20rpx;

						.name {
							font-weight: bold;
						}

						.materials {
							font-size: $font-size-sm;
							color: $text-color-assist;
						}
					}

					.price-and-actions {
						display: flex;
						justify-content: space-between;

						.prices{
							display: flex;
							align-items: baseline;
							color: #FF362D;
							font-weight: bold;
							
							text:nth-child(1){
								font-size: 20rpx;
								margin-right: 5rpx;
							}
							
							text:nth-child(2){
								font-size: 30rpx;
							}
						}
					}
				}
			}
		}
	}
</style>