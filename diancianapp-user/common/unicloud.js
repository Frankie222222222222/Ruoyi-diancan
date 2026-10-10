/**
 * utils/unicloud.js - uniCloud 函数调用封装
 * 优先调本地云函数,失败/不存在时降级调 RuoYi REST
 */
import { storage } from './storage.js';

// uniCloud 部署后的空间 ID(用户需要替换)
const CLOUD_SPACE_ID = 'YOUR_UNICLOUD_SPACE_ID';

export const unicloud = {
  /**
   * 调用云函数
   * @param {string} name  函数名,如 'zhouhao'
   * @param {object} data  入参
   * @param {number} timeout
   */
  async call(name, data = {}, timeout = 10000) {
    // 微信小程序 / H5 / App 三端都支持
    return new Promise((resolve, reject) => {
      if (typeof uniCloud === 'undefined' || !uniCloud.callFunction) {
        reject(new Error('uniCloud.callFunction 不可用 - 请在 HBuilderX 中运行到真机或模拟器'));
        return;
      }
      uniCloud.callFunction({
        name,
        data,
        timeout,
        success: (res) => {
          console.log(`[unicloud.${name}] success:`, res);
          // 注意:成功 = res.result.code === 0
          if (res.result && res.result.code === 0) {
            resolve(res.result);
          } else {
            reject(new Error((res.result && res.result.msg) || '云函数返回错误'));
          }
        },
        fail: (err) => {
          console.error(`[unicloud.${name}] fail:`, err);
          reject(err);
        }
      });
    });
  },

  /**
   * 登录(演示版,无验证码)
   * @param {string} phone
   */
  async loginByPhone(phone) {
    const res = await this.call('user-login', {
      action: 'loginByPhone',
      phone,
    });
    // 持久化
    storage.setUser(res.data);
    storage.setRole(res.data.role);
    return res.data;
  },

  /**
   * zhouhao: 创建订单
   */
  async createOrder({ userId, items, address, remark }) {
    return this.call('zhouhao', { action: 'createOrder', userId, items, address, remark });
  },

  /**
   * zhouhao: 后厨订单列表
   */
  async listKitchenOrders(status) {
    return this.call('zhouhao', { action: 'listKitchenOrders', status });
  },

  /**
   * zhouhao: 接单/出餐
   */
  async updateOrderStatus({ orderId, kitchenStatus, orderStatus }) {
    return this.call('zhouhao', { action: 'updateOrderStatus', orderId, kitchenStatus, orderStatus });
  },

  /**
   * rider-dispatch: 可抢订单
   */
  async listAvailableOrders() {
    return this.call('rider-dispatch', { action: 'listAvailableOrders' });
  },

  /**
   * rider-dispatch: 抢单
   */
  async grabOrder({ orderId, riderId }) {
    return this.call('rider-dispatch', { action: 'grabOrder', orderId, riderId });
  },

  /**
   * rider-dispatch: 我的订单
   */
  async myOrders({ riderId, status }) {
    return this.call('rider-dispatch', { action: 'myOrders', riderId, status });
  },

  /**
   * rider-dispatch: 标记已送达
   */
  async markDelivered({ orderId, riderId }) {
    return this.call('rider-dispatch', { action: 'markDelivered', orderId, riderId });
  },

  /**
   * rider-dispatch: 位置上报
   */
  async updateLocation({ riderId, lat, lng, address }) {
    return this.call('rider-dispatch', { action: 'updateLocation', riderId, lat, lng, address });
  },
};
