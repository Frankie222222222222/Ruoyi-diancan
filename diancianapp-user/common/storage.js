/**
 * utils/storage.js - uniStorage 持久化封装
 */
const KEY_TOKEN = 'TAKEOUT_TOKEN';
const KEY_USER = 'TAKEOUT_USER';
const KEY_ROLE = 'TAKEOUT_ROLE';

export const storage = {
  getToken: () => uni.getStorageSync(KEY_TOKEN) || '',
  setToken: (token) => uni.setStorageSync(KEY_TOKEN, token || ''),
  clearToken: () => uni.removeStorageSync(KEY_TOKEN),

  getUser: () => uni.getStorageSync(KEY_USER) || null,
  setUser: (u) => uni.setStorageSync(KEY_USER, u || null),
  clearUser: () => uni.removeStorageSync(KEY_USER),

  getRole: () => uni.getStorageSync(KEY_ROLE) || '',
  setRole: (r) => uni.setStorageSync(KEY_ROLE, r || ''),
  clearRole: () => uni.removeStorageSync(KEY_ROLE),

  clearAll() {
    this.clearToken();
    this.clearUser();
    this.clearRole();
  }
};
