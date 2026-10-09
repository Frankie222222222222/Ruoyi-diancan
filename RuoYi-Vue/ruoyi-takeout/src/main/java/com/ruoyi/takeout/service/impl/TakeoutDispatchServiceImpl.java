package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.TakeoutDispatch;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.enums.OrderStatusEnum;
import com.ruoyi.takeout.mapper.TakeoutDispatchMapper;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.service.ITakeoutDispatchService;
import com.ruoyi.takeout.service.ITakeoutOrderService;
import com.ruoyi.takeout.service.ITakeoutRiderService;

/**
 * 派单 Service 业务实现
 *
 * @author ruoyi
 */
@Service
public class TakeoutDispatchServiceImpl implements ITakeoutDispatchService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutDispatchServiceImpl.class);

    private static final int ERR_DISPATCH_NOT_FOUND  = 8001;
    private static final int ERR_NO_ACTIVE_DISPATCH  = 8002;
    private static final int ERR_ALREADY_CLAIMED     = 8003;
    private static final int ERR_DISPATCH_FINISHED   = 8004;
    private static final int ERR_NOT_YOUR_DISPATCH   = 8005;
    private static final int ERR_RIDER_BUSY          = 8006;
    private static final int ERR_INVALID_STATUS      = 8007;

    @Autowired
    private TakeoutDispatchMapper dispatchMapper;

    @Autowired
    private TakeoutOrderMapper orderMapper;

    @Autowired
    @Lazy
    private ITakeoutOrderService orderService;

    @Autowired
    private ITakeoutRiderService riderService;

    @Override
    public List<TakeoutDispatch> selectDispatchList(TakeoutDispatch dispatch)
    {
        return dispatchMapper.selectDispatchList(dispatch);
    }

    @Override
    public TakeoutDispatch selectDispatchById(Long dispatchId)
    {
        return dispatchMapper.selectDispatchById(dispatchId);
    }

    @Override
    public TakeoutDispatch selectActiveDispatchByOrderId(Long orderId)
    {
        return dispatchMapper.selectActiveDispatchByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createDispatchForOrder(Long orderId, Long riderId, String dispatchType)
    {
        TakeoutDispatch exist = dispatchMapper.selectActiveDispatchByOrderId(orderId);
        if (exist != null)
        {
            log.warn("订单已有进行中的派单 orderId={}, dispatchId={}", orderId, exist.getDispatchId());
            return 0;
        }
        TakeoutDispatch d = new TakeoutDispatch();
        d.setOrderId(orderId);
        d.setRiderId(riderId);
        d.setDispatchType(StringUtils.isEmpty(dispatchType) ? "0" : dispatchType);
        d.setStatus(riderId == null ? "0" : "1"); // 有骑手=已接单,无骑手=待接单(抢单模式)
        d.setAssignTime(DateUtils.getNowDate());
        d.setCreateBy(SecurityUtils.getUsername());
        return dispatchMapper.insertDispatch(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int claimOrder(Long orderId, Long riderId)
    {
        TakeoutDispatch exist = dispatchMapper.selectActiveDispatchByOrderId(orderId);
        if (exist != null)
        {
            throw new ServiceException("[ERR_" + ERR_ALREADY_CLAIMED + "] 订单已被其他骑手抢走", ERR_ALREADY_CLAIMED);
        }
        // 校验骑手是否有进行中的配送
        int active = dispatchMapper.countActiveByRiderId(riderId);
        if (active >= 3)
        {
            throw new ServiceException("[ERR_" + ERR_RIDER_BUSY + "] 已有进行中的配送单(>3)，请先完成", ERR_RIDER_BUSY);
        }
        TakeoutDispatch d = new TakeoutDispatch();
        d.setOrderId(orderId);
        d.setRiderId(riderId);
        d.setDispatchType("1"); // 抢单
        d.setStatus("1"); // 已接单
        d.setAssignTime(DateUtils.getNowDate());
        d.setAcceptTime(DateUtils.getNowDate());
        d.setCreateBy(SecurityUtils.getUsername());
        int rows = dispatchMapper.insertDispatch(d);
        if (rows > 0)
        {
            // 同步推进订单状态到 DELIVERING
            try
            {
                orderService.changeOrderStatus(orderId, OrderStatusEnum.DELIVERING.getCode());
            }
            catch (Exception e)
            {
                log.warn("派单后同步订单状态失败 orderId={}, err={}", orderId, e.getMessage());
            }
            riderService.incrementDeliveries(riderId);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int acceptDispatch(Long dispatchId, Long riderId)
    {
        TakeoutDispatch d = dispatchMapper.selectDispatchById(dispatchId);
        if (d == null)
        {
            throw new ServiceException("[ERR_" + ERR_DISPATCH_NOT_FOUND + "] 派单不存在", ERR_DISPATCH_NOT_FOUND);
        }
        if (!"0".equals(d.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_STATUS + "] 当前派单状态不可接单: " + d.getStatus(), ERR_INVALID_STATUS);
        }
        if (riderId != null && !riderId.equals(d.getRiderId()))
        {
            throw new ServiceException("[ERR_" + ERR_NOT_YOUR_DISPATCH + "] 该派单已指派给其他骑手", ERR_NOT_YOUR_DISPATCH);
        }
        TakeoutDispatch upd = new TakeoutDispatch();
        upd.setDispatchId(dispatchId);
        upd.setRiderId(riderId);
        upd.setStatus("1");
        upd.setAcceptTime(DateUtils.getNowDate());
        upd.setUpdateBy(SecurityUtils.getUsername());
        int rows = dispatchMapper.updateDispatchStatus(upd);
        if (rows > 0)
        {
            orderService.changeOrderStatus(d.getOrderId(), OrderStatusEnum.DELIVERING.getCode());
            if (riderId != null)
            {
                riderService.incrementDeliveries(riderId);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pickup(Long dispatchId)
    {
        TakeoutDispatch d = dispatchMapper.selectDispatchById(dispatchId);
        if (d == null)
        {
            throw new ServiceException("[ERR_" + ERR_DISPATCH_NOT_FOUND + "] 派单不存在", ERR_DISPATCH_NOT_FOUND);
        }
        if (!"1".equals(d.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_STATUS + "] 当前派单状态不可取餐: " + d.getStatus(), ERR_INVALID_STATUS);
        }
        TakeoutDispatch upd = new TakeoutDispatch();
        upd.setDispatchId(dispatchId);
        upd.setStatus("2");
        upd.setPickupTime(DateUtils.getNowDate());
        upd.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatchStatus(upd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeDispatch(Long dispatchId)
    {
        TakeoutDispatch d = dispatchMapper.selectDispatchById(dispatchId);
        if (d == null)
        {
            throw new ServiceException("[ERR_" + ERR_DISPATCH_NOT_FOUND + "] 派单不存在", ERR_DISPATCH_NOT_FOUND);
        }
        if (!"2".equals(d.getStatus()) && !"1".equals(d.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_STATUS + "] 当前派单状态不可完成: " + d.getStatus(), ERR_INVALID_STATUS);
        }
        TakeoutDispatch upd = new TakeoutDispatch();
        upd.setDispatchId(dispatchId);
        upd.setStatus("3");
        upd.setCompleteTime(DateUtils.getNowDate());
        upd.setUpdateBy(SecurityUtils.getUsername());
        int rows = dispatchMapper.updateDispatchStatus(upd);
        if (rows > 0)
        {
            orderService.changeOrderStatus(d.getOrderId(), OrderStatusEnum.DELIVERED.getCode());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelDispatch(Long dispatchId, String reason)
    {
        TakeoutDispatch d = dispatchMapper.selectDispatchById(dispatchId);
        if (d == null)
        {
            throw new ServiceException("[ERR_" + ERR_DISPATCH_NOT_FOUND + "] 派单不存在", ERR_DISPATCH_NOT_FOUND);
        }
        if ("3".equals(d.getStatus()) || "4".equals(d.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_DISPATCH_FINISHED + "] 派单已结束，无法取消", ERR_DISPATCH_FINISHED);
        }
        TakeoutDispatch upd = new TakeoutDispatch();
        upd.setDispatchId(dispatchId);
        upd.setStatus("4");
        upd.setCancelTime(DateUtils.getNowDate());
        upd.setCancelReason(reason);
        upd.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatchStatus(upd);
    }

    @Override
    public int deleteDispatchByIds(Long[] dispatchIds)
    {
        return dispatchMapper.deleteDispatchByIds(dispatchIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reassignDispatch(Long dispatchId, Long newRiderId, String reason)
    {
        TakeoutDispatch d = dispatchMapper.selectDispatchById(dispatchId);
        if (d == null)
        {
            throw new ServiceException("[ERR_" + ERR_DISPATCH_NOT_FOUND + "] 派单不存在", ERR_DISPATCH_NOT_FOUND);
        }
        // 只能改派进行中的（active = 0,1,2）
        if (!"0".equals(d.getStatus()) && !"1".equals(d.getStatus()) && !"2".equals(d.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_DISPATCH_FINISHED + "] 派单已结束，无法改派", ERR_DISPATCH_FINISHED);
        }
        if (newRiderId == null)
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_STATUS + "] 新骑手不能为空", ERR_INVALID_STATUS);
        }
        if (newRiderId.equals(d.getRiderId()))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_STATUS + "] 新骑手与原骑手相同", ERR_INVALID_STATUS);
        }
        // 校验新骑手是否忙
        int active = dispatchMapper.countActiveByRiderId(newRiderId);
        if (active >= 3)
        {
            throw new ServiceException("[ERR_" + ERR_RIDER_BUSY + "] 新骑手已有进行中的配送单(>=3)，请先完成", ERR_RIDER_BUSY);
        }
        TakeoutDispatch upd = new TakeoutDispatch();
        upd.setDispatchId(dispatchId);
        upd.setRiderId(newRiderId);
        // 改派后回到"待接单"状态，让新骑手重新接单
        upd.setStatus("0");
        upd.setAcceptTime(null);
        upd.setPickupTime(null);
        upd.setCompleteTime(null);
        String remark = "[改派自骑手ID=" + d.getRiderId() + "] " + (StringUtils.isEmpty(reason) ? "" : reason);
        upd.setRemark(remark);
        upd.setUpdateBy(SecurityUtils.getUsername());
        int rows = dispatchMapper.updateDispatchStatus(upd);
        if (rows > 0)
        {
            // 同步把订单状态回退到商家接单(2)，等待新骑手接单后变配送中
            try
            {
                orderService.changeOrderStatus(d.getOrderId(), "2");
            }
            catch (Exception e)
            {
                log.warn("改派后回退订单状态失败 orderId={}, err={}", d.getOrderId(), e.getMessage());
            }
        }
        return rows;
    }
}
