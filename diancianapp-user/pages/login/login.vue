<template>
	<view class="wrap">
		<view class="wrap__image">
			<u-image src="/static/logo.jpg" width="150" height="150" border-radius="500"></u-image>
		</view>
		<view class="wrap__title">成为会员，立享更多优惠福利</view>
		<view class="wrap__desc">授权绑定账号 为您提供更好的服务</view>

		<!-- 测试用快捷登录 -->
		<view class="wrap__quick">
			<view class="wrap__quick-title">🧪 测试用一键登录 (演示版)</view>
			<view class="wrap__quick-grid">
				<button class="wrap__quick-btn" @click="quickLogin('13800138001')">👨‍🍳 张师傅(后厨)</button>
				<button class="wrap__quick-btn" @click="quickLogin('13900139002')">🛵 阿龙(骑手)</button>
				<button class="wrap__quick-btn" @click="quickLogin('15000150003')">👨‍🍳 Tom(后厨)</button>
				<button class="wrap__quick-btn" @click="quickLogin('15100151004')">🛵 Lily(骑手)</button>
				<button class="wrap__quick-btn" @click="quickLogin('15200152005')">🍱 李雪(顾客)</button>
				<button class="wrap__quick-btn" @click="quickLogin('15800158006')">🍱 韩梅梅(顾客)</button>
			</view>
		</view>

		<!-- 正式登录(手机号 + 验证码,演示版跳过验证码) -->
		<view class="wrap__form">
			<view class="wrap__form-title">手机号登录</view>
			<input v-model="phone" class="wrap__input" type="number" maxlength="11" placeholder="请输入手机号" />
			<button class="wrap__btn" :disabled="loading" @click="phoneLogin">{{ loading ? '登录中...' : '一键登录' }}</button>
		</view>
	</view>
</template>

<script>
	import { unicloud } from '@/common/unicloud.js'
	import { useStore } from 'vuex'

	export default {
		data() {
			return {
				phone: '',
				loading: false,
			}
		},
		setup() {
			const store = useStore()
			return { store }
		},
		methods: {
			/**
			 * 演示版:手机号直接登录
			 * 实际流程: input 验证码 → 调 sms.sendCode → 调 login
			 */
			async phoneLogin() {
				if (!/^1[3-9]\d{9}$/.test(this.phone)) {
					return uni.showToast({ title: '手机号格式错误', icon: 'none' })
				}
				return this.doLogin(this.phone)
			},

			quickLogin(phone) {
				this.phone = phone
				return this.doLogin(phone)
			},

			async doLogin(phone) {
				if (this.loading) return
				this.loading = true
				uni.showLoading({ title: '登录中...' })

				try {
					// 优先调 uniCloud 云函数(线上版)
					let userInfo
					try {
						userInfo = await unicloud.loginByPhone(phone)
						userInfo.token = 'UNICLOUD-' + phone // 云函数版无 token,做个标记
					} catch (e) {
						console.warn('uniCloud 登录失败,降级到 REST:', e.message)
						// 降级:调 RuoYi 后端 REST(本地开发用)
						const resp = await uni.request({
							url: 'http://localhost:8080/takeout/user/login',
							method: 'POST',
							data: { phone },
							header: { 'Content-Type': 'application/json' },
						})
						if (resp.statusCode !== 200 || (resp.data.code !== 200 && resp.data.code !== 0)) {
							throw new Error(resp.data.msg || '后端登录失败')
						}
						userInfo = { ...resp.data.data.userInfo, token: resp.data.data.token }
					}

					uni.hideLoading()
					uni.showToast({ title: '登录成功', icon: 'success' })

					// 写入 store + 角色分流
					this.store.dispatch('loginSuccess', { token: userInfo.token, userInfo })
				} catch (e) {
					uni.hideLoading()
					uni.showToast({ title: e.message || '登录失败', icon: 'none' })
				} finally {
					this.loading = false
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import '@/common/scss/login/login.scss';
	.wrap__quick {
		margin: 30rpx;
		padding: 20rpx;
		background: #fff7e6;
		border: 1rpx dashed #E6A23C;
		border-radius: 12rpx;
		&-title { font-size: 26rpx; color: #E6A23C; margin-bottom: 16rpx; text-align: center; }
		&-grid { display: flex; flex-wrap: wrap; gap: 16rpx; justify-content: space-between; }
		&-btn {
			width: 48%;
			font-size: 24rpx;
			background: #fff;
			color: #333;
			border: 1rpx solid #ddd;
			padding: 10rpx 0;
			border-radius: 8rpx;
		}
	}
	.wrap__form { margin: 30rpx; }
	.wrap__form-title { font-size: 28rpx; color: #333; margin-bottom: 16rpx; }
	.wrap__input {
		border: 1rpx solid #ddd; padding: 20rpx; border-radius: 8rpx;
		margin-bottom: 20rpx; font-size: 28rpx;
	}
</style>
