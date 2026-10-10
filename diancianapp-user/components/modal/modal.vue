<template>
	<view @touchmove.stop.prevent>
		<!-- 移除缩放相关的 modal-scale 类，只保留滑动动画的控制 -->
		<view class="modal-box" :style="{width:width,padding:padding,borderRadius:radius}"
			:class="[show?'modal-show':'']">
			<view v-if="custom">
				<slot></slot>
			</view>
			<view v-else>
				<view class="modal-title" v-if="title">{{title}}</view>
				<view class="modal-content" :class="[title?'':'mtop']" :style="{color:color,fontSize:size+'rpx'}">
					<slot></slot>
				</view>
				<view class="modalBtn-box" :class="[button.length!=2?'flex-column':'']">
					<block v-for="(item,index) in button" :key="index">
						<button class="modal-btn" :class="[
										''+(item.type || 'primary')+(item.plain?'-outline':''),
										button.length!=2?'btn-width':'',
										button.length>2?'mbtm':'',
										shape=='circle'?'circle-btn':'',
										'btn-' + (item.size || 'default'),										
									]" :hover-class="''+(item.plain?'outline':(item.type || 'primary'))+'-hover'" :data-index="index"
							@tap="handleClick">{{item.text || "确定"}}</button>
					</block>
				</view>
			</view>
		</view>
		<view class="modal-mask" :class="[show?'mask-show':'']" @tap="handleClickCancel"></view>
	</view>
</template>

<script>
	export default {
		name: "Modal",
		props: {
			show: {
				type: Boolean,
				default: false
			},
			custom: {
				type: Boolean,
				default: false
			},
			width: {
				type: String,
				default: "80%"
			},
			padding: {
				type: String,
				default: "30rpx"
			},
			radius: {
				type: String,
				default: "12rpx"
			},
			title: {
				type: String,
				default: ""
			},
			content: {
				type: String,
				default: ""
			},
			color: {
				type: String,
				default: "#343434"
			},
			size: {
				type: Number,
				default: 28
			},
			shape: {
				type: String,
				default: 'square'
			},
			button: {
				type: Array,
				default: function() {
					return [{
						text: "取消",
						type: "red",
						plain: true
					}, {
						text: "确定",
						type: "red",
						plain: false
					}]
				}
			},
			maskClosable: {
				type: Boolean,
				default: true
			},
			// 保留 fadein 属性，但不再控制动画，避免干扰
			fadein: {
				type: Boolean,
				default: false
			}
		},
		data() {
			return {};
		},
		methods: {
			handleClick(e) {
				if (!this.show) return;
				const dataset = e.currentTarget.dataset;
				this.$emit('click', {
					index: Number(dataset.index)
				});
			},
			handleClickCancel() {
				if (!this.maskClosable) return;
				this.$emit('cancel'); // 触发 cancel 事件
			}
		}
	}
</script>

<style lang="scss">
	.modal-box {
		position: fixed;
		left: 50%;
		bottom: 0;
		margin: 0;
		background: #fff;
		z-index: 1000;
		transition: all 0.3s ease-in-out;
		opacity: 0;
		box-sizing: border-box;
		visibility: hidden;
		/* 初始状态：隐藏在屏幕底部 */
		transform: translateX(-50%) translateY(100%);
	}

	/* 显示状态：滑入到屏幕底部（水平居中） */
	.modal-show {
		opacity: 1;
		visibility: visible;
		transform: translateX(-50%) translateY(0);
	}

	/* 完全删除缩放相关的类，避免干扰 */
	.modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: rgba(0, 0, 0, 0.6);
		z-index: 999;
		transition: all 0.3s ease-in-out;
		opacity: 0;
		visibility: hidden;
	}

	.mask-show {
		visibility: visible;
		opacity: 1;
	}

	/* 以下原有样式保持不变 */
	.modal-title {
		text-align: center;
		font-size: 34rpx;
		color: #333;
		padding-top: 20rpx;
		font-weight: bold;
	}

	.modal-content {
		color: #999;
		font-size: 28rpx;
		padding-top: 20rpx;
		padding-bottom: 60rpx;
	}

	.mtop {
		margin-top: 30rpx;
	}

	.mbtm {
		margin-bottom: 30rpx;
	}

	.modalBtn-box {
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: space-between
	}

	.flex-column {
		flex-direction: column;
	}

	.modal-btn {
		width: 46%;
		height: 68rpx;
		line-height: 68rpx;
		position: relative;
		border-radius: 60rpx;
		font-size: 28rpx;
		overflow: visible;
		margin-left: 0;
		margin-right: 0;

		&.btn-default {
			font-size: 28rpx;
		}

		&.btn-lg {
			font-size: 32rpx;
		}

		&.btn-sm {
			font-size: 24rpx;
		}
	}

	.modal-btn::after {
		content: "";
		position: absolute;
		width: 200%;
		height: 200%;
		-webkit-transform-origin: 0 0;
		transform-origin: 0 0;
		-webkit-transform: scale(0.5, 0.5);
		transform: scale(0.5, 0.5);
		left: 0;
		top: 0;
		border-radius: 60rpx;
	}

	.btn-width {
		width: 80% !important;
	}

	.primary {
		background: #97AF13;
		color: #fff;
	}

	.primary-hover {
		background: #97AF13;
		color: #e5e5e5;
	}

	.primary-outline {
		color: #97AF13;
		background: none;
	}

	.primary-outline::after {
		border: 1px solid #97AF13;
	}

	.danger {
		background: #ed3f14;
		color: #fff;
	}

	.danger-hover {
		background: #d53912;
		color: #e5e5e5;
	}

	.danger-outline {
		color: #ed3f14;
		background: none;
	}

	.danger-outline::after {
		border: 1px solid #ed3f14;
	}

	.red {
		background: #e41f19;
		color: #fff;
	}

	.red-hover {
		background: #c51a15;
		color: #e5e5e5;
	}

	.red-outline {
		color: #e41f19;
		background: none;
	}

	.red-outline::after {
		border: 1px solid #e41f19;
	}

	.warning {
		background: #ff7900;
		color: #fff;
	}

	.warning-hover {
		background: #e56d00;
		color: #e5e5e5;
	}

	.warning-outline {
		color: #ff7900;
		background: none;
	}

	.warning-outline::after {
		border: 1px solid #ff7900;
	}

	.green {
		background: #19be6b;
		color: #fff;
	}

	.green-hover {
		background: #16ab60;
		color: #e5e5e5;
	}

	.green-outline {
		color: #19be6b;
		background: none;
	}

	.green-outline::after {
		border: 1px solid #19be6b;
	}

	.white {
		background: #fff;
		color: #333;
	}

	.white-hover {
		background: #f7f7f9;
		color: #666;
	}

	.white-outline {
		color: #333;
		background: none;
	}

	.white-outline::after {
		border: 1px solid #333;
	}

	.gray {
		background: #ededed;
		color: #999;
	}

	.gray-hover {
		background: #d5d5d5;
		color: #898989;
	}

	.gray-outline {
		color: #999;
		background: none;
	}

	.gray-outline::after {
		border: 1px solid #999;
	}

	.outline-hover {
		opacity: 0.6;
	}

	.circle-btn {
		border-radius: 40rpx !important;
	}

	.circle-btn::after {
		border-radius: 80rpx !important;
	}
</style>