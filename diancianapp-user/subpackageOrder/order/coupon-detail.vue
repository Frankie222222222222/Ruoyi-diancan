<template>
	<view class="wrap">
		<view class="wrap__title">请将二维码出示给核销员</view>
		<view class="wrap__image">
			<u-image :src="ImageData" width="400" height="400" border-radius="200"></u-image>
		</view>
		<view class="wrap__tips">
			请勿出示给陌生人，以防泄露！
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				ImageData:'/static/logo.jpg'
			}
		}
	}
</script>

<style lang="scss">
	.wrap {

		&__title {
			font-size: 36rpx;
			color: #333;
			text-align: center;
			font-weight: bold;
			margin-top: 30%;
		}

		&__image {
			display: flex;
			justify-content: center;
			margin: 80rpx 0 30rpx 0;
		}

		&__tips {
			font-size: 24rpx;
			color: $u-type-info;
			text-align: center;
		}
	}
</style>