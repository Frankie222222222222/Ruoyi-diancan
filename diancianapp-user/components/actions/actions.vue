<template>
	<view class="actions">
		<template v-if="!materialsBtn">
			<image src="/static/img/home/round_minus.png" class="minus-btn" v-show="number" @tap.stop="minus">
			</image>
			<view v-show="number" class="number">{{ number }}</view>
			<image src="/static/img/home/round_add_normal.png" class="add-btn" @tap.stop="add"></image>
		</template>
		<template v-else>
			<view class="materials-box">
				<view class="materials-btn" @tap="$emit('materials')">选规格</view>
				<view class="number-badge" v-show="number">
					<view class="number">{{ number }}</view>
				</view>
			</view>
		</template>
	</view>
</template>

<script>
	export default {
		name: 'Actions',
		props: {
			number: {
				type: Number,
				default: 0
			},
			materialsBtn: {
				type: Boolean,
				default: false
			}
		},
		methods: {
			add() {
				this.$emit('add')
			},
			minus() {
				this.$emit('minus')
			}
		}
	}
</script>

<style lang="scss" scoped>
	.actions {
		margin-right: 20rpx;
		display: flex;
		align-items: center;

		.add-btn,
		.minus-btn {
			width: 34rpx;
			height: 34rpx;
		}

		.number {
			width: 44rpx;
			height: 44rpx;
			margin: 0 20rpx;
			display: flex;
			justify-content: center;
			align-items: center;
			font-size: 26rpx;
			color: #333 !important;
		}

		.materials-box {
			position: relative;
			display: flex;
			align-items: baseline;

			.materials-btn {
				border-radius: 50rpx;
				background-color: #0A3D28;
				width: 100rpx;
				font-size: 20rpx;
				color: white;
				padding: 10rpx 0;
				text-align: center;
			}

			.number-badge {
				z-index: 4;
				position: absolute;
				right: -16rpx;
				top: -14rpx;
				background-color: $bg-color-white;
				border-radius: 100%;
				width: 1.1rem;
				height: 1.1rem;
				display: flex;
				align-items: center;
				justify-content: center;

				.number {
					font-size: 20rpx;
					flex-shrink: 0;
					background-color: #FF362D;
					color: $bg-color-white !important;
					width: 0.9rem;
					height: 0.9rem;
					line-height: 0.9rem;
					text-align: center;
					border-radius: 100%;
				}
			}
		}
	}
</style>