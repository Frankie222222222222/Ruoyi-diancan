'use strict';
/**
 * zhouhao 云函数 - 后厨订单管理
 *
 * 入参 action:
 *   - 'createOrder'        : 创建订单
 *   - 'listKitchenOrders'  : 查询后厨订单(待处理/制作中)
 *   - 'updateOrderStatus'  : 更新订单状态(接单/出餐)
 *   - 'orderDetail'        : 订单详情
 *   - 'dishList'           : 顾客端菜单(分类 + 菜品,按 status/del_flag 过滤)
 *   - 'myOrders'           : 顾客端我的订单(支持按状态/订单类型过滤)
 *   - 'cancelOrder'        : 顾客端取消订单(PENDING 状态可取消)
 *
 * 数据库集合:
 *   - takeout_user
 *   - takeout_order
 *   - takeout_dish
 *   - takeout_dish_category
 *
 * 注意: v2 (2026-10-10) 角色分流版
 * 注意: v2.1 (2026-10-11) 新增 dishList/myOrders/cancelOrder
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
      case 'dishList':
        return await dishList(event);
      case 'myOrders':
        return await myOrders(event);
      case 'cancelOrder':
        return await cancelOrder(event);
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

  // 软删除过滤(neq '2' OR 不存在,这里用 neq('2') 简化)
  const where = { del_flag: dbCmd.neq('2') };
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

/* ============== 5. 顾客端菜单 ============== */
/**
 * 返回结构: {
 *   categories: [{ _id, name, sort }],
 *   dishes: [{ _id, name, category, price, image, description, sales, stock }]
 * }
 * 只返回 status=0(上架) 且 del_flag=0(未删除) 的数据
 */
async function dishList(event) {
  const catRes = await db.collection('takeout_dish_category')
    .where({ del_flag: dbCmd.neq('2') })
    .orderBy('sort', 'asc')
    .get();
  // soft-delete 用字符串 '2',neq('2') 才是真过滤
  const catList = (catRes.data || []).filter(c => c.del_flag !== '2');

  const dishRes = await db.collection('takeout_dish')
    .where({ status: '0', del_flag: dbCmd.neq('2') })
    .orderBy('sales', 'desc')
    .limit(500)
    .get();
  const dishList = (dishRes.data || []).filter(d => d.del_flag !== '2');

  return { code: 0, msg: 'ok', data: { categories: catList, dishes: dishList } };
}

/* ============== 6. 顾客端我的订单 ============== */
/**
 * 入参: { userId, status?, orderType?, limit? }
 *   status:    数组或字符串(可选),按主状态 status 过滤
 *   orderType: 'takein' | 'takeout'(可选) - 堂食/外卖
 *   limit:     默认 50
 */
async function myOrders(event) {
  const { userId, status, orderType, limit = 50 } = event;
  if (!userId) {
    return { code: 4001, msg: 'userId 必填', data: null };
  }
  const where = { user_id: userId, del_flag: dbCmd.neq('2') };
  if (status) {
    where.status = Array.isArray(status) ? dbCmd.in(status) : status;
  }
  if (orderType) {
    // 堂食/外卖目前以 address 是否为空区分,可按业务实际调整
    where.order_type = orderType;
  }
  const res = await db.collection('takeout_order')
    .where(where)
    .orderBy('create_time', 'desc')
    .limit(Math.min(limit, 200))
    .get();
  return { code: 0, msg: 'ok', data: (res.data || []).filter(o => o.del_flag !== '2') };
}

/* ============== 7. 顾客端取消订单 ============== */
/**
 * 只有 PENDING 状态可取消,其它状态需走退款流程
 */
async function cancelOrder(event) {
  const { orderId, userId, reason } = event;
  if (!orderId || !userId) {
    return { code: 4001, msg: 'orderId / userId 必填', data: null };
  }
  // 校验订单属于该用户
  const own = await db.collection('takeout_order').doc(orderId).get();
  const order = (own.data || [])[0];
  if (!order) return { code: 4007, msg: '订单不存在', data: null };
  if (order.user_id !== userId) return { code: 4008, msg: '无权操作此订单', data: null };
  if (order.status !== 'PENDING') {
    return { code: 4009, msg: '当前状态不可取消(' + order.status + ')', data: null };
  }
  await db.collection('takeout_order').doc(orderId).update({
    status: 'CANCELLED',
    cancel_reason: reason || '用户主动取消',
    cancel_time: Date.now(),
    update_time: Date.now(),
  });
  return { code: 0, msg: 'ok', data: { orderId, status: 'CANCELLED' } };
}
