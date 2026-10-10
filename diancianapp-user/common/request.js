/**
 * utils/request.js - RuoYi 后端 REST 调用
 * 优先用本地 axios,无则降级到 uni.request
 */
import { storage } from './storage.js';

const BASE_URL = 'http://localhost:8080'; // 用户改成自己的后端地址

function http(path, { method = 'GET', data, query, header = {} } = {}) {
  return new Promise((resolve, reject) => {
    const token = storage.getToken();
    if (token) header['Authorization'] = 'Bearer ' + token;
    const url = BASE_URL + path + (query ? '?' + Object.entries(query).map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&') : '');
    uni.request({
      url,
      method,
      data,
      header: { 'Content-Type': 'application/json', ...header },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200 || res.data.code === 0) resolve(res.data);
          else reject(new Error(res.data.msg || '业务错误'));
        } else if (res.statusCode === 401) {
          storage.clearAll();
          uni.reLaunch({ url: '/pages/login/login' });
          reject(new Error('未登录或登录已过期'));
        } else {
          reject(new Error('HTTP ' + res.statusCode));
        }
      },
      fail: (err) => reject(err)
    });
  });
}

export const request = {
  get:  (p, opts) => http(p, { ...opts, method: 'GET' }),
  post: (p, data, opts) => http(p, { ...opts, method: 'POST', data }),
  put:  (p, data, opts) => http(p, { ...opts, method: 'PUT',  data }),
  del:  (p, opts) => http(p, { ...opts, method: 'DELETE' }),

  // ============ 用户端 API ============
  login: (phone) => request.post('/takeout/user/login', { phone }),
  me:    () => request.get('/takeout/user/me'),
  changeRole: (userId, role) => request.put(`/takeout/user/changeRole/${userId}/${role}`),
  roleDict: () => request.get('/takeout/user/roleDict'),

  // ============ 订单端 API ============
  listKitchenOrders: () => request.get('/takeout/kitchen/list'),
  grabOrder: (orderId) => request.post(`/takeout/rider/grab/${orderId}`),
  myOrders: () => request.get('/takeout/rider/my'),
};
