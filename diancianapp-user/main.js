import App from './App'
import api from './common/request'

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
	...App
})
app.$mount()
// #endif

// #ifdef VUE3
import {
	createSSRApp
} from 'vue'
import uView from './uni_modules/vk-uview-ui';
import store from './store';
import util from './common/util.js';

export function createApp() {
	const app = createSSRApp(App)

	app.use(uView)
	app.use(store)
	app.config.globalProperties.$util = util
	app.config.globalProperties.$api = api;

	return {
		app
	}
}
// #endif