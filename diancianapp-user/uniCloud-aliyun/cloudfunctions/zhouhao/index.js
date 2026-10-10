'use strict';
/**
 * zhouhao 云函数 - 后厨订单管理
 *
 * 入参 action:
 *   - 'createOrder'        : 创建订单
 *   - 'listKitchenOrders'  : 查询后厨订单(待处理/制作中)
 *   - 'updateOrderStatus'  : 更新订单状态(接单/出餐)
 *   - 'orderDetail'        : 订单详情
 *
 * 数据库集合:
 *   - takeout_user
 *   - takeout_order
 *   - takeout_dish
 *
 * 注意: v2 (2026-10-10) 角色分流版
 */

const db = uniCloud.database();
const dbCmd = db.command;

exports.main = async (event, context) => {
  const { action } = event;
  console.log('[zhouhao] action =', action, 'event =', JSON.stringify(event).slice(0, 500));

  try {
    switch (action) {
      case 'createOrder':
        return await createOrder(event);
      case 'listKitchenOrders':
        return await listKitchenOrders(event);
      case 'updateOrderStatus':
        return await updateOrderStatus(event);
      case 'orderDetail':
        return await orderDetail(event);
      default:
        return { code: 400, msg: '未知 action: ' + action, data: null };
    }
  } catch (e) {
    console.error('[zhouhao] error:', e);
    return { code: 500, msg: e.message || '服务器内部错误', data: null };
  }
};

/* ============== 1. 创建订单 ============== */
async function createOrder(event) {
  const { userId, items, address, remark } = event;

  if (!userId) {
    return { code: 4001, msg: 'userId 必填', data: null };
  }
  if (!Array.isArray(items) || items.length === 0) {
    return { code: 4002, msg: 'items 不能为空', data: null };
  }

  // 1. 校验用户
  const userRes = await db.collection('takeout_user')
    .where({ _id: userId, del_flag: '0' })
    .limit(1)
    .get();
  if (!userRes.data || userRes.data.length === 0) {
    return { code: 4003, msg: '用户不存在', data: null };
  }
  const user = userRes.data[0];
  if (user.status !== '0') {
    return { code: 4004, msg: '用户已禁用', data: null };
  }

  // 2. 计算总价 + 校验菜品
  const dishIds = items.map(it => it.dishId);
  const dishRes = await db.collection('takeout_dish')
    .where({ _id: dbCmd.in(dishIds), status: '0', del_flag: '0' })
    .get();
  const dishMap = {};
  (dishRes.data || []).forEach(d => { dishMap[d._id] = d; });

  let totalAmount = 0;
  const orderItems = [];
  for (const it of items) {
    const dish = dishMap[it.dishId];
    if (!dish) {
      return { code: 4005, msg: '菜品不存在: ' + it.dishId, data: null };
    }
    if (!it.qty || it.qty <= 0) {
      return { code: 4006, msg: '数量非法', data: null };
    }
    const subtotal = Number(dish.price) * it.qty;
    totalAmount += subtotal;
    orderItems.push({
      dishId: dish._id,
      dishName: dish.name,
      price: dish.price,
      qty: it.qty,
      subtotal: subtotal,
    });
  }

  // 3. 生成订单号
  const orderNo = 'ORD' + Date.now() + Math.floor(Math.random() * 1000);
  const now = Date.now();

  // 4. 写库
  const addRes = await db.collection('takeout_order').add({
    order_no: orderNo,
    user_id: userId,
    user_nickname: user.nickname,
    user_phone: user.phone,
    items: orderItems,
    total_amount: totalAmount,
    address: address || null,
    remark: remark || '',
    status: 'PENDING',        // PENDING → KITCHEN → READY → DELIVERING → DONE
    kitchen_status: 'PENDING', // 后厨子状态
    rider_status: 'WAITING',  // 骑手子状态
    pay_status: 'UNPAID',
    create_time: now,
    update_time: now,
  });

  return {
    code: 0,
    msg: '下单成功',
    data: {
      orderId: addRes.id,
      orderNo: orderNo,
      totalAmount: totalAmount,
    }
  };
}

/* ============== 2. 后厨订单列表 ============== */
async function listKitchenOrders(event) {
  const { status, limit = 50 } = event;

  // 默认返回需要后厨处理的状态
  const where = { del_flag: dbCmd.neq('2') || '0' };
  if (status && Array.isArray(status)) {
    where.kitchen_status = dbCmd.in(status);
  } else if (status) {
    where.kitchen_status = status;
  } else {
    where.kitchen_status = dbCmd.in(['PENDING', 'COOKING']);
  }

  const res = await db.collection('takeout_order')
    .where(where)
    .orderBy('create_time', 'asc')
    .limit(Math.min(limit, 200))
    .get();

  return { code: 0, msg: 'ok', data: res.data || [] };
}

/* ============== 3. 更新订单状态 ============== */
async function updateOrderStatus(event) {
  const { orderId, kitchenStatus, riderStatus, orderStatus } = event;
  if (!orderId) {
    return { code: 4001, msg: 'orderId 必填', data: null };
  }

  const update = { update_time: Date.now() };
  if (kitchenStatus) update.kitchen_status = kitchenStatus;
  if (riderStatus) update.rider_status = riderStatus;
  if (orderStatus) update.status = orderStatus;

  // 联动:出餐时把主状态推到 READY(等骑手接)
  if (kitchenStatus === 'READY' && !orderStatus) {
    update.status = 'READY';
  }
  // 联动:后厨接单时
  if (kitchenStatus === 'COOKING' && !orderStatus) {
    update.status = 'COOKING';
  }

  const res = await db.collection('takeout_order').doc(orderId).update(update);
  if (res.updated === 0) {
    return { code: 4007, msg: '订单不存在', data: null };
  }
  return { code: 0, msg: 'ok', data: { orderId, ...update } };
}

/* ============== 4. 订单详情 ============== */
async function orderDetail(event) {
  const { orderId } = event;
  if (!orderId) {
    return { code: 4001, msg: 'orderId 必填', data: null };
  }
  const res = await db.collection('takeout_order').doc(orderId).get();
  if (!res.data || res.data.length === 0) {
    return { code: 4007, msg: '订单不存在', data: null };
  }
  return { code: 0, msg: 'ok', data: res.data[0] };
}
