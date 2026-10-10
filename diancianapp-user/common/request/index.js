import promotion from './promotion.js'
import order from './order.js'
import menu from './menu.js'

const json = {
	promotion,
	order,
	menu
}

export default (name, loading = true) => {
	if (loading) {
		uni.showLoading()
	}

	return new Promise(resolve => {
		setTimeout(() => {
			uni.hideLoading()
			resolve(json[name])
		}, 500)
	})
}