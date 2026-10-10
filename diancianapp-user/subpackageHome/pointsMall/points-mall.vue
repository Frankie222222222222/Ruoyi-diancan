<template>
	<view class="wrap">
		<view class="wrap__img">
			<u-image src="/static/img/home/promotion-img.png" width="100%" height="800" @click="inviteFriends"
				lazy-load></u-image>

			<view class="wrap__img__btn">
				<view>立即提现</view>
				<view @tap.stop="withdrawRecord">提现记录</view>
			</view>
		</view>

		<view class="wrap__grid">
			<u-grid :col="2" :border="false">
				<u-grid-item>
					<view class="wrap__grid__title">已邀请好友</view>
					<view class="wrap__grid__desc">5</view>
				</u-grid-item>
				<u-grid-item>
					<view class="wrap__grid__title">已到账收益</view>
					<view class="wrap__grid__desc">5.00</view>
				</u-grid-item>
			</u-grid>
		</view>

		<view class="wrap__invitationList">
			<view v-for="(item,index) in promotionList" :key="index" class="wrap__invitationList__list">
				<view class="wrap__invitationList__list__left">
					<view>
						<u-avatar :src="item.avatar" size="80"></u-avatar>
					</view>
					<view class="wrap__invitationList__list__left__info">
						<view>{{item.nickname}}</view>
						<view>
							已经邀请并注册成功，奖励
							<text>{{item.amount}}元</text>
							已到账
						</view>
					</view>
				</view>
				<view class="wrap__invitationList__list__right">
					<view>+{{item.amount}}.00</view>
					<view>{{item.createtime}}</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		onMounted,
		getCurrentInstance
	} from 'vue'

	const {
		proxy
	} = getCurrentInstance();

	const promotionList = ref([]);

	const withdrawRecord = () => {
		uni.navigateTo({
			url: `/subpackageHome/pointsMall/withdraw-history`
		})
	}

	const inviteFriends = () => {
		uni.navigateTo({
			url: `/pages/friendRegistration/friend-registration`
		})
	}

	onMounted(async () => {
		promotionList.value = await proxy.$api('promotion')
	})
</script>

<style lang="scss" scoped>
	@import '@/common/scss/home/points-mall.scss';
</style>