<template>
	<view class="container">
		<u-form :model="form">
			<u-form-item label="联系人" prop="name" label-position="top">
				<u-input v-model="form.name" placeholder="请输入联系人" />
			</u-form-item>
			<u-form-item label="手机号" prop="phone" label-position="top">
				<u-input v-model="form.phone" placeholder="请输入手机号" type="number" maxlength="11" />
			</u-form-item>
			<u-form-item label="详细地址" prop="address" label-position="top">
				<u-input v-model="form.address" placeholder="请输入详细地址" />
			</u-form-item>
			<u-form-item label="门牌号（例:1号楼2单元301）" prop="house_number" label-position="top">
				<u-input v-model="form.house_number" placeholder="请输入门牌号" />
			</u-form-item>
		</u-form>

		<view class="container__btn">保存</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				form: {
					name: '',
					phone: '',
					address: '',
					longitude: '',
					latitude: '',
					house_number: ''
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.container {
		padding: 30rpx;

		&__btn {
			background-color: #0A3D28;
			color: white;
			border-radius: 18rpx;
			text-align: center;
			padding: 20rpx 0;
			position: fixed;
			bottom: 30rpx;
			left: 0;
			right: 0;
			width: 80%;
			margin: 0 auto;
			font-size: 30rpx;
		}
	}
</style>