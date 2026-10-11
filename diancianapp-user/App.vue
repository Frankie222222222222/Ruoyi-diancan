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
	/* 全局样式 token - 来源 @/common/scss/theme.scss(由 App.vue 的 import 链引入) */
	@import "@/uni_modules/uview-ui/index.scss";
	@import "@/common/scss/theme.scss";

	page {
		background-color: $bg-page;
		font-size: $font-size-medium;
		color: $text-color-base;
	}
</style>
