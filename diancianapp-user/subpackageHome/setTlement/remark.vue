<template>
	<view class="container">
		<view v-if="orderType != 'takeshop'">
			<view class="font-size-medium">快捷标签</view>
			<view class="remarks">
				<view class="remark" :class="{'active': remark.active}" v-for="(remark, index) in remarks" :key="index"
					@tap="choose(index)">
					{{ remark.label }}
				</view>
			</view>
		</view>
		<view class="textarea-box">
			<textarea class="textarea" v-model="remark" placeholder-class="placeholder" :maxlength="50"
				@input="handleTextareaInput"></textarea>
			<view class="tips">{{ remark.length }} / 50</view>
		</view>
		<button class="w-100 font-size-extra-lg border-radius-lg" style="margin-top: 50rpx;" @tap="submit">提交</button>
	</view>
</template>

<script>
	import {
		mapState
	} from 'vuex'
	export default {
		data() {
			return {
				remarks: [{
						label: '对骑手',
						active: 0
					},
					{
						label: '对商家',
						active: 0
					},
					{
						label: '不要敲门',
						active: 0
					},
					{
						label: '放前台',
						active: 0
					},
					{
						label: '放门卫',
						active: 0
					},
					{
						label: '不要打电话',
						active: 0
					},
					{
						label: '请提前电话联系',
						active: 0
					},
					{
						label: '堂食无需打包',
						active: 0
					},
					{
						label: '我要自取，不要配送',
						active: 0
					}
				],
				remark: ''
			}
		},
		computed: {
			...mapState(['orderType']),
		},
		onShow() {
			this.remark = this.$store.state.remark
			this.remarks.forEach(item => (item.label == this.remark) && (item.active = 1))
		},
		methods: {
			choose(index) {
				this.remarks.forEach(item => item.active = 0)
				this.remarks[index].active = 1
				this.remark = this.remarks[index].label
			},
			handleTextareaInput(e) {
				if (e.detail.value.length >= 50) {
					uni.showToast({
						icon: 'none',
						title: '最多备注50个字符'
					})
					return
				}
			},
			submit() {
				this.$store.commit('SET_REMARK', this.remark)
				uni.navigateBack()
			}
		}
	}
</script>

<style lang="scss">
	.container {
		background-color: #FFFFFF;
		padding: 40rpx;

		button {
			background-color: #0A3D28;
			color: white;
		}
	}

	.remarks {
		margin: 30rpx 0 40rpx 0;
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		font-size: $font-size-base;

		.remark {
			color: $text-color-assist;
			padding: 10rpx 26rpx;
			border: 1rpx solid rgba($color: $border-color, $alpha: 0.6);
			margin-right: 20rpx;
			margin-top: 10rpx;

			&.active {
				color: #0A3D28;
				border-color: #0A3D28;
			}
		}
	}

	.textarea-box {
		.textarea {
			width: 100%;
			border: 1rpx solid rgba($color: $border-color, $alpha: 0.6);
			padding: 20rpx;
			font-size: $font-size-medium;
		}
	}
</style>