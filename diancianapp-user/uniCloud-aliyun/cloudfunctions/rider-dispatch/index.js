'use strict';
/**
 * rider-dispatch 云函数 - 骑手抢单 + 派单
 *
 * action:
 *   - 'listAvailableOrders' : 查询可抢订单(后厨已 READY,无骑手)
 *   - 'grabOrder'           : 骑手抢单
 *   - 'myOrders'            : 我的订单(配送中)
 *   - 'markDelivered'       : 标记已送达
 *   - 'updateLocation'      : 骑手位置上报
 *   - 'riderStats'          : 骑手统计
 */

const db = uniCloud.database();
const dbCmd = db.command;

exports.main = async (event, context) => {
  const { action } = event;
  console.log('[rider-dispatch] action =', action);

  try {
    switch (action) {
      case 'listAvailableOrders':
        return await listAvailableOrders(event);
      case 'grabOrder':
        return await grabOrder(event);
      case 'myOrders':
        return await myOrders(event);
      case 'markDelivered':
        return await markDelivered(event);
      case 'updateLocation':
        return await updateLocation(event);
      case 'riderStats':
        return await riderStats(event);
      default:
        return { code: 400, msg: '未知 action: ' + action, data: null };
    }
  } catch (e) {
    console.error('[rider-dispatch] error:', e);
    return { code: 500, msg: e.message, data: null };
  }
};

/* ============== 1. 可抢订单 ============== */
async function listAvailableOrders(event) {
  const { limit = 50 } = event;
  const res = await db.collection('takeout_order')
    .where({
      status: 'READY',
      rider_id: dbCmd.exists(false),
      del_flag: dbCmd.neq('2'),
    })
    .orderBy('create_time', 'asc')
    .limit(Math.min(limit, 100))
    .get();
  return { code: 0, msg: 'ok', data: res.data || [] };
}

/* ============== 2. 抢单 ============== */
async function grabOrder(event) {
  const { orderId, riderId } = event;
  if (!orderId || !riderId) {
    return { code: 4001, msg: 'orderId + riderId 必填', data: null };
  }

  // 校验骑手身份
  const riderRes = await db.collection('takeout_user')
    .where({ _id: riderId, role: 'rider', del_flag: '0', status: '0' })
    .limit(1)
    .get();
  if (!riderRes.data || riderRes.data.length === 0) {
    return { code: 4003, msg: '骑手不存在或已禁用', data: null };
  }
  const rider = riderRes.data[0];

  // 原子抢单:用 updateAndReturn 防止并发
  const grabRes = await db.collection('takeout_order').where({
    _id: orderId,
    status: 'READY',
    rider_id: dbCmd.exists(false),
  }).update({
    rider_id: riderId,
    rider_nickname: rider.nickname,
    rider_phone: rider.phone,
    status: 'DELIVERING',
    rider_status: 'GRABBED',
    grab_time: Date.now(),
    update_time: Date.now(),
  });

  if (!grabRes.updated || grabRes.updated === 0) {
    return { code: 4008, msg: '抢单失败,订单已被抢或状态变化', data: null };
  }
  return { code: 0, msg: '抢单成功', data: { orderId, riderId } };
}

/* ============== 3. 我的订单 ============== */
async function myOrders(event) {
  const { riderId, status = ['GRABBED', 'PICKED', 'DELIVERING'] } = event;
  if (!riderId) {
    return { code: 4001, msg: 'riderId 必填', data: null };
  }
  const res = await db.collection('takeout_order')
    .where({
      rider_id: riderId,
      rider_status: dbCmd.in(status),
    })
    .orderBy('grab_time', 'desc')
    .limit(50)
    .get();
  return { code: 0, msg: 'ok', data: res.data || [] };
}

/* ============== 4. 标记已送达 ============== */
async function markDelivered(event) {
  const { orderId, riderId } = event;
  if (!orderId || !riderId) {
    return { code: 4001, msg: 'orderId + riderId 必填', data: null };
  }
  const res = await db.collection('takeout_order').where({
    _id: orderId,
    rider_id: riderId,
  }).update({
    status: 'DONE',
    rider_status: 'DELIVERED',
    deliver_time: Date.now(),
    update_time: Date.now(),
  });
  if (!res.updated) {
    return { code: 4007, msg: '订单不存在或不是你的', data: null };
  }
  return { code: 0, msg: 'ok', data: { orderId } };
}

/* ============== 5. 位置上报 ============== */
async function updateLocation(event) {
  const { riderId, lat, lng, address } = event;
  if (!riderId || lat == null || lng == null) {
    return { code: 4001, msg: 'riderId/lat/lng 必填', data: null };
  }
  const res = await db.collection('takeout_user').doc(riderId).update({
    last_lat: lat,
    last_lng: lng,
    last_location_address: address || '',
    last_location_time: Date.now(),
  });
  return res.updated ? { code: 0, msg: 'ok' } : { code: 4007, msg: '骑手不存在' };
}

/* ============== 6. 骑手统计 ============== */
async function riderStats(event) {
  const { riderId } = event;
  if (!riderId) {
    return { code: 4001, msg: 'riderId 必填', data: null };
  }
  const allRes = await db.collection('takeout_order')
    .where({ rider_id: riderId })
    .limit(500)
    .get();
  const orders = allRes.data || [];
  const today0 = new Date(new Date().toDateString()).getTime();
  const today = orders.filter(o => o.create_time >= today0);
  const done = orders.filter(o => o.status === 'DONE');

  return {
    code: 0,
    msg: 'ok',
    data: {
      total: orders.length,
      todayCount: today.length,
      doneCount: done.length,
      totalEarning: done.reduce((s, o) => s + (o.delivery_fee || 0), 0),
    }
  };
}
