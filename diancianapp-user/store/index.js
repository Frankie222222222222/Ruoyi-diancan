/**
 * store/index.js - Vuex 状态管理
 * v2 (2026-10-10): 加 user / token / role 三个核心状态
 *
 * 小程序启动流程:
 *   1. App.vue 的 onLaunch 调 store.dispatch('restoreAuth')
 *   2. 根据 storage 里的 token/user/role 还原登录态
 *   3. 登录页成功 → dispatch('loginSuccess', userInfo)
 *   4. 角色分流: loginSuccess 里根据 role 跳不同首页
 */
import { createStore } from 'vuex'
import { storage } from '@/common/storage.js'
import { ROLE, homePageByRole } from '@/common/role.js'

const store = createStore({
  state: {
    // ===== 鉴权态 =====
    token: '',
    userInfo: null, // {userId, phone, nickname, avatar, role, ...}
    role: '',

    // ===== 业务态(沿用) =====
    orderType: 'takein',
    addressInfo: {
      address: '北京市东城区王府井大街',
      house_number: '88号',
      name: 'Kaiyuan_Q',
      phone: '18888888888'
    },
    remark: '',
  },

  getters: {
    isLogin: (s) => !!s.token && !!s.userInfo,
    isStaff: (s) => [ROLE.KITCHEN, ROLE.RIDER, ROLE.ADMIN].includes(s.role),
    homePath: (s) => homePageByRole(s.role),
  },

  mutations: {
    SET_TOKEN(s, token) { s.token = token || ''; storage.setToken(token); },
    SET_USER(s, user) { s.userInfo = user || null; storage.setUser(user); },
    SET_ROLE(s, role) { s.role = role || ''; storage.setRole(role); },

    SET_ORDER_TYPE(s, t) { s.orderType = t; },
    SET_ADDRESS(s, a) { s.addressInfo = a; },
    SET_REMARK(s, r) { s.remark = r; },
  },

  actions: {
    /**
     * App 启动时调用,从 storage 恢复登录态
     */
    restoreAuth({ commit }) {
      const token = storage.getToken();
      const user = storage.getUser();
      const role = storage.getRole();
      if (token) commit('SET_TOKEN', token);
      if (user) commit('SET_USER', user);
      if (role) commit('SET_ROLE', role);
    },

    /**
     * 登录成功后调用,持久化 + 跳首页
     */
    loginSuccess({ commit, getters }, { token, userInfo }) {
      commit('SET_TOKEN', token);
      commit('SET_USER', userInfo);
      commit('SET_ROLE', userInfo.role || ROLE.USER);

      // 角色分流
      uni.reLaunch({ url: getters.homePath });
    },

    /**
     * 退出登录
     */
    logout({ commit }) {
      storage.clearAll();
      commit('SET_TOKEN', '');
      commit('SET_USER', null);
      commit('SET_ROLE', '');
      uni.reLaunch({ url: '/pages/login/login' });
    },
  }
})

export default store
