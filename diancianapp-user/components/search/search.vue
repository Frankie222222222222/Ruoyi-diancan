<template>
	<uni-transition :mode-class="['slide-left']" :styles="tranStyles" :show="show">
		<u-navbar :is-back="false" :border-bottom="false">
			<view class="search">
				<u-search placeholder="请输入商品名" @change="handleKeywordInput" @custom="hide" action-text="取消"
					v-model="keyword">
				</u-search>
			</view>
		</u-navbar>
		<view class="search-keyword">
			<scroll-view class="keyword-box" v-show="!isShowKeywordList" scroll-y>
				<view class="keyword-block" v-show="oldKeywordList.length>0">
					<view class="keyword-list-header">
						<view>历史搜索</view>
						<view>
							<u-icon @tap="oldDelete" name="trash" color="#909399" size="28"></u-icon>
						</view>
					</view>
					<view class="keyword">
						<view v-for="(keyword,index) in oldKeywordList" @tap="doSearch(keyword)" :key="index">
							{{keyword}}
						</view>
					</view>
				</view>
				<view class="keyword-block">
					<view class="keyword-list-header">
						<view>热门搜索</view>
						<view>
							<u-icon :name="forbid ? 'eye-fill' : 'eye-off'" @tap="hotToggle" color="#909399"
								size="28"></u-icon>
						</view>
					</view>
					<view class="keyword" v-if="forbid">
						<view v-for="(keyword,index) in hotKeywordList" @tap="doSearch(keyword)" :key="index">
							{{keyword}}
						</view>
						<view v-if="hotKeywordList.length === 0" class="empty-tip">暂无热门搜索</view>
					</view>
					<view class="hide-hot-tis" v-else>
						<view>当前热门搜索已隐藏</view>
					</view>
				</view>
			</scroll-view>

			<scroll-view class="result" scroll-y v-if="result.length">
				<template>
					<view class="wrapper" style="padding: 0 30rpx 30rpx 30rpx;">
						<view class="product" v-for="(item, index) in result" :key="index"
							@tap="handleChoose(item, true)">
							<view class="d-flex align-items-center pro-left">
								<image :src="item.images[0].url" class="pro-image" />
								<view class="pro-name">{{ item.name }}</view>
							</view>
							<view class="pro-price">
								<text>￥</text>
								<text>{{ item.price }}</text>
								<text>起</text>
							</view>
						</view>
					</view>
				</template>
			</scroll-view>

		</view>
	</uni-transition>
</template>

<script>
	import uniTransition from '@/components/uni-transition/uni-transition.vue'
	export default {
		name: 'Search',
		components: {
			uniTransition
		},
		props: {
			show: {
				type: Boolean,
				default: false
			},
			categories: {
				type: Array,
				default: () => []
			}
		},
		data() {
			return {
				tranStyles: {
					width: '100%',
					position: 'absolute',
					top: 0,
					left: 0,
					bottom: 0,
					backgroundColor: '#fff',
					zIndex: 998
				},
				keyword: '',
				result: [],
				oldKeywordList: [],
				hotKeywordList: [],
				// keywordList: [],
				forbid: true,
				isShowKeywordList: false
			}
		},
		mounted() {
			this.init();
		},
		methods: {
			init() {
				this.loadOldKeyword();
				this.loadHotKeyword();
			},
			blur() {
				uni.hideKeyboard()
			},
			loadOldKeyword() {
				uni.getStorage({
					key: 'OldKeys',
					success: (res) => {
						try {
							const OldKeys = JSON.parse(res.data);
							this.oldKeywordList = Array.isArray(OldKeys) ? OldKeys : [];
						} catch (e) {
							this.oldKeywordList = [];
						}
					},
					fail: () => {
						this.oldKeywordList = [];
					}
				});
			},
			loadHotKeyword() {
				this.hotKeywordList = ['招牌酱肉包', '火腿包', '招牌胡辣汤', '古法八宝粥', '茶叶蛋', '灵魂肉肠'];
			},
			drawCorrelativeKeyword(keywords, keyword) {
				const len = keywords.length;
				const keywordArr = [];
				for (let i = 0; i < len; i++) {
					const row = keywords[i];
					const html = row[0].replace(keyword, "<span style='color: #9f9f9f;'>" + keyword + "</span>");
					const htmlStr = '<div>' + html + '</div>';
					keywordArr.push({
						keyword: row[0],
						htmlStr: htmlStr
					});
				}
				return keywordArr;
			},
			setKeyword(index) {
				if (this.keywordList[index]) {
					this.keyword = this.keywordList[index].keyword;
				}
			},
			oldDelete() {
				uni.showModal({
					content: '确定清除历史搜索记录？',
					success: (res) => {
						if (res.confirm) {
							this.oldKeywordList = [];
							uni.removeStorage({
								key: 'OldKeys'
							});
						}
					}
				});
			},
			hotToggle() {
				this.forbid = !this.forbid;
			},
			saveKeyword(keyword) {
				if (!keyword.trim()) return;
				uni.getStorage({
					key: 'OldKeys',
					success: (res) => {
						let OldKeys = [];
						try {
							OldKeys = JSON.parse(res.data) || [];
						} catch (e) {
							OldKeys = [];
						}
						const findIndex = OldKeys.indexOf(keyword);
						if (findIndex === -1) {
							OldKeys.unshift(keyword);
						} else {
							OldKeys.splice(findIndex, 1);
							OldKeys.unshift(keyword);
						}
						OldKeys.length > 10 && OldKeys.pop();
						uni.setStorage({
							key: 'OldKeys',
							data: JSON.stringify(OldKeys)
						});
						this.oldKeywordList = OldKeys;
					},
					fail: () => {
						const OldKeys = [keyword];
						uni.setStorage({
							key: 'OldKeys',
							data: JSON.stringify(OldKeys)
						});
						this.oldKeywordList = OldKeys;
					}
				});
			},
			hide() {
				this.keyword = '';
				this.result = [];
				this.$emit('hide');
			},
			handleChoose(item, isSearch = false) {
				if (isSearch) {
					this.hide();
					this.$emit('choose', item);
					return;
				}
				this.categories.forEach(category => {
					const find = category.products?.find(product => product.id == item.productId);
					if (find) {
						this.hide();
						this.$emit('choose', find);
						return;
					}
				});
			},
			handleKeywordInput(e) {
				if (!e) {
					this.result = []
					return
				}

				let Result = []
				this.categories.forEach(category => {
					category.products.forEach(product => {
						if (product.name.indexOf(e) > -1) {
							Result.push(product)
						}
					})
				})
				this.result = Result

				this.saveKeyword(e);
			},
			doSearch(searchKeyword) {
				this.keyword = searchKeyword;
				this.handleKeywordInput(searchKeyword);
			}
		}
	}
</script>

<style lang="scss" scoped>
	.search {
		margin: 0 30rpx;
	}

	.result {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		width: 100%;
		background-color: white;
		height: calc(100vh - 120rpx);
		/* #ifdef H5 */
		height: calc(100vh - 120rpx - 100rpx);
		/* #endif */
		overflow: hidden;

		.product {
			padding: 20rpx 0;
			display: flex;
			align-items: center;
			justify-content: space-between;
			font-size: $font-size-base;

			.pro-left {
				image {
					margin-right: 30rpx;
					border-radius: 8rpx;
				}
			}

			.pro-image {
				width: 144rpx;
				height: 108rpx;
			}

			.pro-name {
				font-size: 28rpx;
				color: #333;
				font-weight: bold;
			}

			.pro-price {

				display: flex;
				align-items: baseline;
				color: #FF362D;
				font-weight: bold;

				text:nth-child(1) {
					font-size: 20rpx;
					margin-right: 5rpx;
				}

				text:nth-child(2) {
					font-size: 30rpx;
				}

				text:nth-child(3) {
					font-size: 20rpx;
				}
			}
		}
	}

	.search-keyword {
		position: relative;
	}

	// .keyword-list-box {
	// 	height: calc(100vh - 110upx);
	// 	padding-top: 10upx;
	// 	border-radius: 20upx 20upx 0 0;
	// }

	.keyword-entry-tap {
		background-color: #eee;
	}

	.keyword-entry {
		width: 94%;
		height: 80upx;
		margin: 0 3%;
		font-size: 30upx;
		color: #333;
		display: flex;
		justify-content: space-between;
		align-items: center;
		border-bottom: solid 1upx #e7e7e7;
	}

	.keyword-entry .keyword-text,
	.keyword-entry .keyword-img {
		height: 80upx;
		display: flex;
		align-items: center;
	}

	.keyword-entry .keyword-text {
		width: 90%;
	}

	.keyword-entry .keyword-img {
		width: 10%;
		justify-content: center;
	}

	.keyword-box {
		position: relative;
		max-height: 100vh;
		border-radius: 20upx 20upx 0 0;
	}

	.keyword-box .keyword-block {
		padding: 10upx 0;
	}

	.keyword-box .keyword-block .keyword-list-header {
		width: 94%;
		padding: 10upx 3%;
		font-size: 27upx;
		color: #333;
		display: flex;
		justify-content: space-between;
	}

	.keyword-box .keyword-block .keyword-list-header image {
		width: 40upx;
		height: 40upx;
	}

	.keyword-box .keyword-block .keyword {
		width: 94%;
		padding: 3px 3%;
		display: flex;
		flex-flow: wrap;
		justify-content: flex-start;

		// 新增：空提示样式
		.empty-tip {
			width: 100%;
			text-align: center;
			color: #999;
			font-size: 28rpx;
			padding: 20rpx 0;
		}
	}

	.keyword-box .keyword-block .hide-hot-tis {
		display: flex;
		justify-content: center;
		font-size: 28upx;
		color: #6b6b6b;
	}

	.keyword-box .keyword-block .keyword>view {
		display: flex;
		justify-content: center;
		align-items: center;
		border-radius: 60upx;
		padding: 0 20upx;
		margin: 10upx 20upx 10upx 0;
		height: 60upx;
		font-size: 28upx;
		color: #6b6b6b;
		background-color: #F5F5F5;
	}
</style>