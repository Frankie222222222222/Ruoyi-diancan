<template>
	<view class="wrap">
		<view class="wrap__tabs">
			<u-tabs :list="tabsList" :is-scroll="false" v-model="current" @change="change" active-color="#0A3D28"
				bar-width="100" duration="0" inactive-color="#9A9A9A"></u-tabs>
		</view>

		<view v-if="current === 0">
			<view v-for="(item,index) in pickupList" :key="item.id" class="wrap__list" @click="orderDetail(item)">
				<view class="wrap__list__top">
					<view>七香嫂包子铺</view>
					<view>{{item.status == '0' ? '待付款' : item.status == '1' ? '已付款' : '已退款'}}</view>
				</view>
				<view class="wrap__list__shopinfo" v-for="(itemt,indext) in item.commodity_list" :key="indext">
					<view class="wrap__list__shopinfo__left">
						<view class="wrap__list__shopinfo__left__image">
							<u-image :src="itemt.image" width="180" height="140" border-radius="8"></u-image>
						</view>
						<view class="wrap__list__shopinfo__left__desc">
							<view>
								<view class="wrap__list__shopinfo__left__desc__shopname">{{itemt.name}}</view>
								<view class="wrap__list__shopinfo__left__desc__content" v-if="itemt.materials_text">
									{{itemt.materials_text}}
								</view>
							</view>
							<view class="wrap__list__shopinfo__left__desc__price">
								<text>￥</text>
								<text>{{itemt.price}}</text>
							</view>
						</view>
					</view>
					<view class="wrap__list__shopinfo__right">x{{itemt.number}}</view>
				</view>
				<view class="wrap__list__prices">
					共{{item.shop_num}}件商品，合计：
					<text>￥</text>
					<text>{{item.price}}</text>
				</view>
				<view class='wrap__list__bottom'>
					<view>再来一单</view>
				</view>
			</view>
		</view>

		<view v-else-if="current === 1">
			<view v-for="(item,index) in takeoutList" :key="item.id" class="wrap__list" @click="orderDetail(item)">
				<view class="wrap__list__top">
					<view>七香嫂包子铺</view>
					<view>
						{{item.orderstatus == 2 ? '已退款' : item.delivery_status == 0 ? '商家已接单' : item.delivery_status == 1 ? '配送中' : '已完成'}}
					</view>
				</view>
				<view class="wrap__list__shopinfo" v-for="(itemt,indext) in item.commodity_list" :key="indext">
					<view class="wrap__list__shopinfo__left">
						<view class="wrap__list__shopinfo__left__image">
							<u-image :src="itemt.image" width="180" height="140" border-radius="8"></u-image>
						</view>
						<view class="wrap__list__shopinfo__left__desc">
							<view>
								<view class="wrap__list__shopinfo__left__desc__shopname">{{itemt.name}}</view>
								<view class="wrap__list__shopinfo__left__desc__content" v-if="itemt.materials_text">
									{{itemt.materials_text}}
								</view>
							</view>
							<view class="wrap__list__shopinfo__left__desc__price">
								<text>￥</text>
								<text>{{itemt.price}}</text>
							</view>
						</view>
					</view>
					<view class="wrap__list__shopinfo__right">x{{itemt.number}}</view>
				</view>
				<view class="wrap__list__prices">
					共{{item.shop_num}}件商品，合计：
					<text>￥</text>
					<text>{{item.price}}</text>
				</view>
				<view class='wrap__list__bottom'>
					<view>再来一单</view>
				</view>
			</view>
		</view>

		<view v-else>
			<view v-for="(item,index) in couponList" :key="item.id" class="wrap__list">
				<view class="wrap__list__top">
					<view>七香嫂包子铺</view>
					<view>{{item.status == '0' ? '待核销' : '已核销'}}</view>
				</view>
				<view class="wrap__list__shopinfo">
					<view class="wrap__list__shopinfo__left">
						<view class="wrap__list__shopinfo__left__image">
							<u-image :src="item.image" width="180" height="140"></u-image>
						</view>
						<view class="wrap__list__shopinfo__left__desc">
							<view class="wrap__list__shopinfo__left__desc__shopname">{{item.name}}</view>
							<view class="wrap__list__shopinfo__left__desc__price">
								<text>￥</text>
								<text>{{item.price}}</text>
							</view>
						</view>
					</view>
					<view class="wrap__list__shopinfo__right">x1</view>
				</view>
				<view class="wrap__list__prices">
					共1件商品，合计：
					<text>￥</text>
					<text>{{item.price}}</text>
				</view>
				<view class="wrap__list__couponBottom">
					<view v-if="item.status == '0'" @click="checkCoupon(item)">查看劵码</view>
					<view>删除订单</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted
	} from 'vue'
	import { useStore } from 'vuex'
	import { unicloud } from '@/common/unicloud.js'

	const store = useStore()
	const tabsList = ref([{
			name: '自取订单'
		},
		{
			name: '外卖订单'
		},
		{
			name: '劵码订单'
		}
	])

	// 三 tab 都接 myOrders,按 status 数组区分
	const pickupList = ref([])   // 堂食待付款/已付款
	const takeoutList = ref([])  // 外卖配送中/已完成
	const couponList = ref([])   // 劵码(暂留空,后续接核销流水)
	const loading = ref(false)

	const STATUS_PICKUP = ['PENDING', 'COOKING', 'READY', 'DONE', 'CANCELLED']
	const STATUS_TAKEOUT = ['READY', 'DELIVERING', 'DONE', 'CANCELLED']

	const current = ref(0);

	const change = (index) => {
		current.value = index;
	}

	const orderDetail = (param) => {
		const type = current.value == 0 ? 'takein' : 'takeout'
		const orderId = param._id || param.id
		uni.navigateTo({
			url: `/subpackageOrder/order/order-detail?type=${type}&orderId=${orderId}`
		})
	}

	const checkCoupon = (param) => {
		uni.navigateTo({
			url: `/subpackageOrder/order/coupon-detail?id=${param.id}`
		})
	}

	const statusText = (o) => {
		const map = {
			PENDING: '待付款',
			COOKING: '制作中',
			READY: '待取餐/待配送',
			DELIVERING: '配送中',
			DONE: '已完成',
			CANCELLED: '已取消',
		}
		return map[o.status] || o.status
	}

	const loadOrders = async () => {
		const userInfo = store.state.userInfo
		if (!userInfo || !userInfo._id) {
			uni.showToast({ title: '请先登录', icon: 'none' })
			return
		}
		loading.value = true
		try {
			// 堂食
			const p = await unicloud.myOrdersC({ userId: userInfo._id, status: STATUS_PICKUP, orderType: 'takein' })
			pickupList.value = (p.data || []).map(o => ({ ...o, status: o.status }))
			// 外卖
			const t = await unicloud.myOrdersC({ userId: userInfo._id, status: STATUS_TAKEOUT, orderType: 'takeout' })
			takeoutList.value = (t.data || []).map(o => ({ ...o, status: o.status }))
		} catch (e) {
			console.error('[order] loadOrders error:', e)
			uni.showToast({ title: e.message || '加载失败', icon: 'none' })
		} finally {
			loading.value = false
		}
	}

	onMounted(() => {
		loadOrders()
	})
</script>

<style lang="scss" scoped>
	@import '@/common/scss/order/order.scss';
</style>