'use strict';
/**
 * user-login 云函数 - 手机号登录 / 注册
 *
 * action:
 *   - 'loginByPhone' : 用手机号登录(演示版,无验证码)
 *   - 'getUserById'  : 通过 ID 查询用户
 *   - 'getRoleDict'  : 获取角色字典
 *
 * 注意: v2 角色分流版 - 返回 role 字段
 */

const db = uniCloud.database();
const dbCmd = db.command;

const VALID_ROLES = ['user', 'kitchen', 'rider', 'admin'];

exports.main = async (event, context) => {
  const { action } = event;
  console.log('[user-login] action =', action);

  try {
    switch (action) {
      case 'loginByPhone':
        return await loginByPhone(event);
      case 'getUserById':
        return await getUserById(event);
      case 'getRoleDict':
        return getRoleDict();
      default:
        return { code: 400, msg: '未知 action: ' + action };
    }
  } catch (e) {
    console.error('[user-login] error:', e);
    return { code: 500, msg: e.message, data: null };
  }
};

/* ============== 1. 手机号登录 ============== */
async function loginByPhone(event) {
  const { phone, nickname, avatar } = event;
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
    return { code: 4001, msg: '手机号格式错误', data: null };
  }

  // 1. 查用户
  const res = await db.collection('takeout_user')
    .where({ phone, del_flag: dbCmd.neq('2') })
    .limit(1)
    .get();

  let user;
  if (res.data && res.data.length > 0) {
    user = res.data[0];
    if (user.status !== '0') {
      return { code: 4004, msg: '账号已禁用', data: null };
    }
    // 更新最后登录时间
    await db.collection('takeout_user').doc(user._id).update({
      last_login_time: Date.now(),
    });
  } else {
    // 2. 自动注册(仅顾客角色)
    const addRes = await db.collection('takeout_user').add({
      phone,
      nickname: nickname || ('用户' + phone.substring(7)),
      avatar: avatar || '',
      gender: '0',
      city: '',
      total_orders: 0,
      total_spend: 0,
      reg_time: Date.now(),
      last_login_time: Date.now(),
      status: '0',
      del_flag: '0',
      role: 'user', // 默认顾客
    });
    user = {
      _id: addRes.id,
      phone,
      nickname: nickname || ('用户' + phone.substring(7)),
      role: 'user',
    };
  }

  return {
    code: 0,
    msg: '登录成功',
    data: {
      userId: user._id,
      phone: user.phone,
      nickname: user.nickname,
      avatar: user.avatar,
      gender: user.gender,
      city: user.city,
      role: user.role || 'user', // v2 核心:角色
    }
  };
}

/* ============== 2. 通过 ID 查询 ============== */
async function getUserById(event) {
  const { userId } = event;
  if (!userId) {
    return { code: 4001, msg: 'userId 必填', data: null };
  }
  const res = await db.collection('takeout_user').doc(userId).get();
  if (!res.data || res.data.length === 0) {
    return { code: 4003, msg: '用户不存在', data: null };
  }
  const u = res.data[0];
  return {
    code: 0,
    msg: 'ok',
    data: {
      userId: u._id,
      phone: u.phone,
      nickname: u.nickname,
      avatar: u.avatar,
      gender: u.gender,
      city: u.city,
      role: u.role || 'user',
    }
  };
}

/* ============== 3. 角色字典 ============== */
function getRoleDict() {
  return {
    code: 0,
    msg: 'ok',
    data: [
      { code: 'user',    label: '顾客',   color: 'default' },
      { code: 'kitchen', label: '后厨',   color: 'warning' },
      { code: 'rider',   label: '骑手',   color: 'primary' },
      { code: 'admin',   label: '管理员', color: 'danger'  },
    ]
  };
}
