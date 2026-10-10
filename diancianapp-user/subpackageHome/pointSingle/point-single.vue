<template>
	<view class="container">
		<u-navbar :border-bottom="false">
			<u-search placeholder="搜索" disabled @tap="showSearch = true" :show-action="false"></u-search>
		</u-navbar>
		<view class="header">
			<!-- 搜索栏 end -->
			<view class="center">
				<view class="store">
					<view class="title">
						<view class="address">七香嫂包子铺</view>
						<view class="business">营业时间：早5:00 - 晚18:00</view>
					</view>
					<view class="buttons">
						<button type="default" class="button" :class="{active: orderType == 'takein'}" plain
							hover-class="none" @tap="switchOrderType">
							自取
						</button>
						<button type="default" class="button" :class="{active: orderType == 'takeout'}" plain
							hover-class="none" @tap="switchOrderType">
							外卖
						</button>
					</view>
				</view>
			</view>
		</view>
		<view class="main">
			<scroll-view class="menu-bar" scroll-y scroll-with-animation>
				<view class="wrapper">
					<view class="menu-item" @tap="handleMenuSelected(category.id)"
						:class="{active: currentCategoryId == category.id}"
						v-for="(category, index) in filterCategories" :key="index">
						<image :src="category.category_image_url" class="image" mode="widthFix"
							v-if="category.category_image_url"></image>
						<view class="title">{{ category.name }}</view>
					</view>
				</view>
			</scroll-view>

			<scroll-view class="product-section" scroll-y scroll-with-animation :scroll-top="productsScrollTop"
				@scroll="productsScroll">
				<view class="wrapper">
					<view id="ads">
						<swiper class="ads1" :indicator-dots="true" :autoplay="true" :interval="3000" :duration="1000"
							circular>
							<swiper-item v-for="(ad, index) in ads1" :key="index">
								<image :src="ad" class="w-100" mode="widthFix"></image>
							</swiper-item>
						</swiper>
					</view>

					<view class="products-list" v-for="(category, index) in filterCategories" :key="index"
						:id="`products-${category.id}`">
						<view class="category-name">{{ category.name }}</view>
						<view class="products">
							<view class="product" v-for="(product, key) in category.products" :key="key"
								@tap="showProductDetailModal(product)">
								<view>
									<image :src="product.images[0].url" mode="widthFix" class="image"></image>
								</view>
								<view class="content">
									<view class="name">{{ product.name }}</view>
									<view class="sold">已售{{ product.sold }}</view>
									<view class="labels" v-if="product.labels.length > 0">
										<view class="label"
											:style="{color: label.label_color, background: util.hexToRgba(label.label_color, 0.2)}"
											v-for="label in product.labels" :key="label.id">{{ label.name }}</view>
									</view>
									<view class="price">
										<view class="prices">
											<text>￥</text>
											<text>{{ product.price }}</text>
										</view>
										<actions :materials-btn="!product.is_single"
											@materials="showProductDetailModal(product)"
											:number="productCartNum(product.id)" @add="handleAddToCart(product)"
											@minus="handleMinusFromCart(product)" />
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
		<product-modal :product="product" :visible="productModalVisible" @cancel="closeProductDetailModal"
			@add-to-cart="handleAddToCartInModal" :order-type="orderType" />
		<cart-bar :cart="cart" @add="handleAddToCart" @minus="handleMinusFromCart" @clear="clearCart" @pay="pay" />
		<search :show="showSearch" :categories="filterCategories" @hide="showSearch=false"
			@choose="showProductDetailModal">
		</search>
	</view>
</template>

<script>
	import {
		mapState,
		mapMutations
	} from 'vuex'
	import Actions from '@/components/actions/actions.vue'
	import CartBar from '@/components/cartbar/cartbar.vue'
	import ProductModal from '@/components/product-modal/product-modal.vue'
	import cartPopup from '@/components/cart-popup/cart-popup.vue'
	import Search from '@/components/search/search.vue'
	import util from '@/common/util.js'

	export default {
		components: {
			Actions,
			CartBar,
			ProductModal,
			cartPopup,
			Search
		},
		data() {
			return {
				util,
				categories: [],
				cart: [],
				product: {},
				currentCategoryId: 0,
				ads1: [
					"/static/img/home/ads.jpg"
				],
				productModalVisible: false,
				cartPopupShow: false,
				productsScrollTop: 0,
				showSearch: false
			}
		},
		computed: {
			...mapState(['orderType']),
			productCartNum() {
				return id => this.cart.reduce((acc, cur) => {
					if (cur.id === id) {
						return acc += cur.number
					}
					return acc
				}, 0)
			},
			// 根据订单类型过滤分类和商品
			filterCategories() {
				return this.categories.map(category => {
					const filterProducts = category.products.filter(product => {
						if (this.orderType === 'takein') {
							return product.is_pickup === 1;
						} else {
							return product.is_takeout === 1;
						}
					});
					return {
						...category,
						products: filterProducts
					};
				}).filter(category => {
					return category.products.length > 0;
				});
			}
		},
		watch: {
			filterCategories: {
				handler(newVal) {
					if (newVal.length > 0) {
						// 检查当前选中的ID是否还在过滤后的列表中
						const exists = newVal.some(cat => cat.id === this.currentCategoryId);
						if (!exists) {
							// 如果不存在，自动选中第一个
							this.currentCategoryId = newVal[0].id;
							// 选中后重新计算滚动位置（可选，确保右侧商品列表也滚动到对应位置）
							this.$nextTick(async () => {
								await this.calcSize();
								const targetCategory = this.categories.find(item => item.id === this
									.currentCategoryId);
								if (targetCategory && targetCategory.top !== undefined) {
									this.productsScrollTop = targetCategory.top;
								}
							});
						}
					}
				},
				immediate: true, // 立即执行，确保页面加载时就触发
				deep: true
			}
		},
		async onLoad() {
			const res = await this.$api('menu');
			this.categories = res;
			await this.$nextTick(async () => await this.calcSize())
			this.currentCategoryId = this.filterCategories.length && this.filterCategories[0].id
		},
		methods: {
			...mapMutations(['SET_ORDER_TYPE']),
			switchOrderType() {
				if (this.cart.length != 0) {
					uni.showModal({
						title: '温馨提示',
						content: '切换购买方式后，购物车中的商品将被清空，是否确认继续操作？',
						success: (res) => {
							if (res.confirm) {
								this.cart = [];
								if (this.cart.length == 0) {
									this.switchOrderChange();
									return;
								}
								this.$u.toast('切换失败')
							}
						}
					})
					return;
				}

				this.switchOrderChange();
			},
			switchOrderChange() {
				if (this.orderType === 'takein') {
					this.SET_ORDER_TYPE('takeout')
				} else {
					this.SET_ORDER_TYPE('takein')
				}

				this.$nextTick(async () => await this.calcSize())
			},
			handleAddToCart(product) {
				const index = this.cart.findIndex(item => {
					if (!product.is_single) {
						return (item.id == product.id) && (item.materials_text == product.materials_text)
					} else {
						return item.id === product.id
					}
				})

				if (index > -1) {
					this.cart[index].number += (product.number || 1)
					return
				}

				this.cart.push({
					id: product.id,
					cate_id: product.category_id,
					name: product.name,
					price: product.price,
					number: product.number || 1,
					image: product.images[0].url,
					is_single: product.is_single,
					materials_text: product.materials_text || ''
				})
			},
			handleMinusFromCart(product) {
				let index
				if (product.is_single) {
					index = this.cart.findIndex(item => item.id == product.id)
				} else {
					index = this.cart.findIndex(item => (item.id == product.id) && (item.materials_text == product
						.materials_text))
				}
				this.cart[index].number -= 1
				if (this.cart[index].number <= 0) {
					this.cart.splice(index, 1)
				}
			},
			showProductDetailModal(product) {
				product.materials = product.materials || [];

				if (product.materials && product.materials.length > 0) {
					product.materials.forEach(group => {
						if (group.values && group.values.length > 0) {
							group.values.forEach((val, idx) => {
								if (idx === 0) {
									val.is_selected = 1;
								} else {
									val.is_selected = 0;
								}

								if (val.is_exclusive === undefined) {
									val.is_exclusive = 0;
								}
							});
						}
					});
				}

				this.product = product
				this.productModalVisible = true
			},
			handleAddToCartInModal(product) {
				const price = product.price;
				this.handleAddToCart(product)
				this.closeProductDetailModal()
			},
			closeProductDetailModal() {
				this.productModalVisible = false
				this.product = {}
			},
			openCartDetailsPopup() {
				this.$refs['cartPopup'].open()
			},
			clearCart() {
				this.cart = []
			},
			handleMenuSelected(id) {
				this.$nextTick(() => {
					const targetCategory = this.categories.find(item => item.id === id)
					if (targetCategory && targetCategory.top !== undefined) {
						this.productsScrollTop = targetCategory.top
					}
					this.currentCategoryId = id
				})
			},
			productsScroll({
				detail
			}) {
				const {
					scrollTop
				} = detail
				let tabs = this.categories.filter(item => item.top <= scrollTop).reverse()
				if (tabs.length > 0) {
					this.currentCategoryId = tabs[0].id
				}
			},
			async calcSize() {
				let h = 0
				const adsData = await new Promise(resolve => {
					uni.createSelectorQuery().select('#ads')
						.fields({
							size: true
						}, data => resolve(data))
						.exec()
				})
				if (adsData) {
					h += Math.floor(adsData.height)
				}

				for (const filterItem of this.filterCategories) {
					const originalItem = this.categories.find(item => item.id === filterItem.id)
					if (!originalItem) continue;

					const productData = await new Promise(resolve => {
						uni.createSelectorQuery().select(`#products-${filterItem.id}`)
							.fields({
								size: true
							}, data => resolve(data))
							.exec()
					})

					if (productData) {
						originalItem.top = h
						h += Math.floor(productData.height)
						originalItem.bottom = h
					}
				}
			},
			pay() {
				uni.setStorageSync('cart', this.cart);
				uni.navigateTo({
					url: '/subpackageHome/setTlement/pay'
				})
			}
		}
	}
</script>

<style lang="scss">
	@import './index.scss';
</style>