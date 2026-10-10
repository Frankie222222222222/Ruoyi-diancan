<template>
	<view class="wrap">

		<view v-if="swiperList.length">
			<u-swiper :list="swiperList" height="600" border-radius="0" mode="none"></u-swiper>
		</view>

		<view class="wrap__userinfo" @click="handleLogin">
			<view class="wrap__userinfo__left">
				<view>
					<u-image src="/static/logo.jpg" width="60" height="60" border-radius="50%" lazy-load></u-image>
				</view>
				<view class="wrap__userinfo__left__nickname">
					尊敬的用户
				</view>
			</view>
			<view class="wrap__userinfo__right">
				<view>注册/登录</view>
			</view>
		</view>

		<view class="wrap__menu">
			<view class="wrap__menu__box" @click="handlePointSingle('takein')">
				<view>
					<u-image src="/static/img/home/icon-1.jpg" width="180" height="180" lazy-load></u-image>
				</view>
				<view class="wrap__menu__box__name">门店堂食</view>
				<view class="wrap__menu__box__desc">提前点免排队</view>
			</view>

			<view class="wrap__menu__group"></view>

			<view class="wrap__menu__box" @click="handlePointSingle('takeout')">
				<view>
					<u-image src="/static/img/home/icon-2.jpg" width="180" height="180" lazy-load></u-image>
				</view>
				<view class="wrap__menu__box__name">外卖配送</view>
				<view class="wrap__menu__box__desc">外卖及时送达</view>
			</view>
		</view>

		<view class="wrap__pointsmall" @click="handleMyPromotion">
			<view>
				<u-image src="/static/img/home/promotion.png" width="180" height="180" lazy-load></u-image>
			</view>
			<view>
				<view class="wrap__pointsmall__mypoint">我的推广</view>
				<view class="wrap__pointsmall__pointdesc">
					推广奖励实时到账，支持余额提现
					<u-icon name="arrow-right" size="24"></u-icon>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	const ORDER_TYPE = {
		TAKE_IN: 'takein',
		TAKE_OUT: 'takeout'
	};

	export default {
		data() {
			return {
				userinfo: {},
				swiperList: [{
					image: '/static/img/home/banner.jpg'
				}]
			}
		},
		methods: {
			handleLogin() {
				uni.navigateTo({
					url: `/pages/login/login`
				});
			},
			handlePointSingle(type) {
				this.$store.commit('SET_ORDER_TYPE', type);
				uni.navigateTo({
					url: `/subpackageHome/pointSingle/point-single`
				});
			},
			handleMyPromotion() {
				uni.navigateTo({
					url: `/subpackageHome/pointsMall/points-mall`
				});
			}
		}
	}
</script>

<style lang="scss">
	@import '@/common/scss/home/home.scss';
</style>