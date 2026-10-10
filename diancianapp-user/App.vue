<script>
	import { useStore } from 'vuex'
	export default {
		onLaunch: function() {
			console.log('App Launch')
			// 启动时恢复登录态
			const store = useStore()
			store.dispatch('restoreAuth')

			// 已登录:直接跳到对应角色的首页
			setTimeout(() => {
				if (store.getters.isLogin) {
					console.log('已登录,跳到:', store.getters.homePath)
					uni.reLaunch({ url: store.getters.homePath })
				}
			}, 100)
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/* uni.scss 全局样式变量 */
	@import "@/uni_modules/uview-ui/index.scss";
	page {
		background-color: #f5f5f5;
		font-size: 28rpx;
		color: #333;
	}
</style>
