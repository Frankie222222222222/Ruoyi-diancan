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
	} from 'vue'

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

	const pickupList = ref([{
		"status": '0',
		"commodity_list": [{
			"name": "招牌酱肉包",
			"price": 5.99,
			"number": 1,
			"image": "/static/img/menu/menu-1.jpg",
			"is_single": false,
			"materials_text": ""
		}],
		"shop_num": 1,
		"price": 5.99
	}, {
		"status": '1',
		"commodity_list": [{
			"id": 12,
			"name": "火腿包",
			"price": 3.99,
			"number": 1,
			"image": "/static/img/menu/menu-2.jpg",
			"is_single": false,
			"materials_text": ""
		}, {
			"name": "招牌酱肉包",
			"price": 5.99,
			"number": 1,
			"image": "/static/img/menu/menu-1.jpg",
			"is_single": false,
			"materials_text": ""
		}],
		"shop_num": 2,
		"price": 9.98
	}, {
		"status": '2',
		"commodity_list": [{
			"id": 13,
			"name": "酸菜油滋啦包",
			"price": 4.59,
			"number": 1,
			"image": "/static/img/menu/menu-3.jpg",
			"is_single": false,
			"materials_text": ""
		}, {
			"name": "火腿包",
			"price": 3.99,
			"number": 1,
			"image": "/static/img/menu/menu-2.jpg",
			"is_single": false,
			"materials_text": ""
		}, {
			"name": "招牌酱肉包",
			"price": 5.99,
			"number": 1,
			"image": "/static/img/menu/menu-1.jpg",
			"is_single": false,
			"materials_text": ""
		}],
		"shop_num": 3,
		"price": 14.57
	}]);
	const takeoutList = ref([{
		"commodity_list": [{
			"name": "招牌酱肉包",
			"price": 5.99,
			"number": 1,
			"image": "/static/img/menu/menu-1.jpg",
			"is_single": false,
			"materials_text": ""
		}],
		"shop_num": 1,
		"price": 5.99,
		"orderstatus": 0,
		"delivery_status": 0
	}, {
		"commodity_list": [{
			"name": "火腿包",
			"price": 3.99,
			"number": 1,
			"image": "/static/img/menu/menu-2.jpg",
			"is_single": false,
			"materials_text": ""
		}],
		"shop_num": 1,
		"price": 3.99,
		"orderstatus": 0,
		"delivery_status": 1
	}, {
		"commodity_list": [{
			"name": "酸菜油滋啦包",
			"price": 4.59,
			"number": 1,
			"image": "/static/img/menu/menu-3.jpg",
			"is_single": false,
			"materials_text": ""
		}],
		"shop_num": 1,
		"price": 4.59,
		"orderstatus": 0,
		"delivery_status": 2
	}, {
		"commodity_list": [{
			"name": "透汁鲜肉+透汁牛肉+小米粥+小菜",
			"price": 16.66,
			"number": 1,
			"image": "/static/img/menu/menu-4.jpg",
			"is_single": false,
			"materials_text": ""
		}],
		"shop_num": 1,
		"price": 16.66,
		"orderstatus": 2,
		"delivery_status": 0
	}]);
	const couponList = ref([{
		"image": "/static/img/order/egg-img.jpg",
		"name": "精品富硒鸡蛋",
		"price": 0,
		"shop_num": 1,
		"status": '0'
	}, {
		"image": "/static/img/order/egg-img.jpg",
		"name": "精品富硒鸡蛋",
		"price": 0,
		"shop_num": 1,
		"status": '1'
	}]);
	const current = ref(0);

	const change = (index) => {
		current.value = index;
	}

	const orderDetail = (param) => {
		const type = current.value == 0 ? 'takein' : 'takeout'
		uni.navigateTo({
			url: `/subpackageOrder/order/order-detail?type=${type}`
		})
	}

	const checkCoupon = (param) => {
		uni.navigateTo({
			url: `/subpackageOrder/order/coupon-detail?id=${param.id}`
		})
	}
</script>

<style lang="scss" scoped>
	@import '@/common/scss/order/order.scss';
</style>