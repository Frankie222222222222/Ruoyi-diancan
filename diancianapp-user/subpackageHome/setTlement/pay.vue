<template>
	<view class="container">
		<view class="u-m-l-20 u-m-r-20 u-m-t-20">
			<u-alert-tips type="warning" description="如果觉得还不错的话，麻烦免费插件给个五星好评，您的鼓励是我更新的动力，感谢🙏！"></u-alert-tips>
		</view>
		<view class="u-m-l-20 u-m-r-20 u-m-t-20">
			<u-alert-tips type="error" title="合作请备注" description="QQ：2234207170，VX：Kaiyuan_Q"></u-alert-tips>
		</view>


		<view v-if="orderType == 'takeout'" class="container__addressbox">
			<view v-if="addressInfo?.address" class="container__addressbox__addressinfo" @click="addressManage">
				<view class="container__addressbox__addressinfo__info">
					<view class="container__addressbox__addressinfo__info__top">{{addressInfo.address}}
						{{addressInfo.house_number}}
					</view>
					<view class="container__addressbox__addressinfo__info__bottom">
						<view>{{addressInfo.name}}</view>
						<view>{{addressInfo.phone}}</view>
					</view>
				</view>
				<view>
					<u-icon name="arrow-right" color="#909399"></u-icon>
				</view>
			</view>
			<view v-else class="container__addressbox__add-address" @click="addressManage">
				<u-icon name="plus" size="30" class="mr-10"></u-icon>
				请选择收货地址
			</view>

			<view v-if="orderType == 'takeout' && addressInfo?.address" class="delivery-options">
				<view class="delivery-option" :class="{'delivery-option--active': deliveryType === 'immediately'}"
					@click="selectDeliveryType('immediately')">
					<view>立即送</view>
					<view class="delivery-option__right">
						<view>马上配送</view>
					</view>
				</view>

				<view class="delivery-option" :class="{'delivery-option--active': deliveryType === 'reservation'}"
					@click="selectDeliveryType('reservation')">
					<view>预约送</view>
					<view class="delivery-option__right">
						<view v-if="reservationTime">{{reservationTime}}</view>
						<view v-else>选择时间</view>
						<view><u-icon name="arrow-right"
								:color="deliveryType === 'reservation' ? '#FFFFFF' : '#909399'"></u-icon></view>
					</view>
				</view>
			</view>
		</view>

		<view v-if="showReservationPicker">
			<time-picker v-model:isShow="showReservationPicker" :isBtn="false" :isFast="false" :isAutoClose="true"
				title="选择预计送达时间" subTitle="请选择您方便的送达时间" @change="pickerChange" :minHour="6" :maxHour="18"
				dayRange="7"></time-picker>
		</view>

		<view class="bg-white pt-30 mb-20">
			<view class="font-size-medium font-weight-bold pl-30">商品列表</view>
			<view v-for="(item,index) in cart" :key="index" class="container__shopbox">
				<view class="container__shopbox__left">
					<view>
						<u-image :src="item.image" border-radius="18" width="100" height="100"></u-image>
					</view>
					<view class="container__shopbox__left__info">
						<view>{{item.name}}</view>
						<view>{{item.materials_text}}</view>
						<view>x{{item.number}}</view>
					</view>
				</view>
				<view class="container__shopbox__right">
					<text>￥</text>
					<text>{{item.price}}</text>
				</view>
			</view>
			<list-cell arrow last>
				<view class="w-100 d-flex align-items-center justify-content-between overflow-hidden">
					<view class="flex-shrink-0">备注</view>
					<view>
						<u-input placeholder="请填写您的要求" disabled v-model="remark" @click="addRemark"
							input-align="right"></u-input>
					</view>
				</view>
			</list-cell>
			<list-cell last>
				<view class="w-100 d-flex justify-content-end align-items-center">
					<text class="font-size-sm">共{{ cartNum }}件商品，小计</text>
					<view class="price">
						<text>￥</text>
						<text>{{ cartAmount }}</text>
					</view>
				</view>
			</list-cell>
		</view>
		<list-cell last>
			<view class="w-100 d-flex align-items-center justify-content-between">
				<view>支付方式</view>
				<view class="d-flex align-items-center">
					<image src="/static/img/home/weixin-pay.png" class="wx-pay-icon">
					</image>
					<view>微信</view>
				</view>
			</view>
		</list-cell>
		<view class="footer">
			<view class="mr-30 total">
				<text>合计：</text>
				<text>￥</text>
				<text>{{ cartAmount }}</text>
			</view>
			<button type="primary">支付</button>
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref,
		watchEffect,
		getCurrentInstance,
		onUnmounted,
		onMounted
	} from 'vue'
	import {
		useStore
	} from 'vuex'
	import TimePicker from '@/uni_modules/hbxw-timepicker/components/hbxw-timepicker/hbxw-timepicker.vue'

	const store = useStore()

	const cart = ref([])

	const appointmentSet = ref({})
	const deliveryType = ref('immediately')
	const reservationTime = ref('')
	const showReservationPicker = ref(false)

	const addressInfo = computed(() => store.state.addressInfo)
	const orderType = computed(() => store.state.orderType)
	const remark = computed(() => store.state.remark)

	const instance = getCurrentInstance()
	const {
		$api
	} = instance.appContext.config.globalProperties

	const remarkValue = computed({
		get: () => store.state.remark,
		set: (value) => store.commit('setRemark', value)
	})

	onUnmounted(() => {
		store.commit('SET_REMARK', '')
	})

	const cartNum = computed(() => {
		return cart.value.reduce((acc, cur) => acc + cur.number, 0)
	})

	const cartAmount = computed(() => {
		const originalTotal = cart.value.reduce((acc, cur) => acc + cur.number * cur.price, 0)
		const fixedTotal = Math.floor(originalTotal * 100) / 100
		return fixedTotal
	})

	watchEffect(() => {
		const cartKey = 'cart';
		const cartData = (uni.getStorageSync(cartKey) || []).map(item => ({
			...item,
			is_refund: 0
		}))
		cart.value = cartData
		uni.setStorageSync(cartKey, cartData)
	})

	const selectDeliveryType = (type) => {
		if (type === 'immediately') {
			deliveryType.value = 'immediately'
			reservationTime.value = ''
		} else {
			showReservationPicker.value = true
		}
	}

	const pickerChange = (event) => {
		const {
			result,
			form
		} = event

		if (form === 'hour' || form === 'sure') {
			if (result) {
				if (result.displayText) {
					reservationTime.value = `${result.displayText} ${result.hoursStr}`
				} else {
					const date = new Date(result.year, result.month - 1, result.day)
					const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
					const weekDay = weekDays[date.getDay()]
					reservationTime.value = `${result.month}月${result.day}日（${weekDay}） ${result.hoursStr}`
				}

				deliveryType.value = 'reservation'
				showReservationPicker.value = false
			}
		}

		if (form === 'close') {
			showReservationPicker.value = false
		}
	}

	const addRemark = () => {
		uni.navigateTo({
			url: `/subpackageHome/setTlement/remark`
		})
	}

	const addressManage = () => {
		uni.navigateTo({
			url: `/subpackageMy/myAddress/address-manage?methods=pay`
		})
	}
</script>

<style lang="scss" scoped>
	@import '@/common/scss/home/pay.scss';
</style>