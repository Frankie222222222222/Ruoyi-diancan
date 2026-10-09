package com.ruoyi.takeout.service.impl;

import java.math.BigDecimal;
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
import com.ruoyi.takeout.domain.TakeoutComplaint;
import com.ruoyi.takeout.enums.OrderStatusEnum;
import com.ruoyi.takeout.mapper.TakeoutComplaintMapper;
import com.ruoyi.takeout.service.ITakeoutComplaintService;
import com.ruoyi.takeout.service.ITakeoutOrderService;

/**
 * 投诉工单 Service 业务实现
 *
 * @author ruoyi
 */
@Service
public class TakeoutComplaintServiceImpl implements ITakeoutComplaintService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutComplaintServiceImpl.class);

    private static final int ERR_COMPLAINT_NOT_FOUND = 11001;
    private static final int ERR_COMPLAINT_DONE      = 11002;
    private static final int ERR_INVALID_STATUS      = 11003;

    @Autowired
    private TakeoutComplaintMapper complaintMapper;

    @Autowired
    @Lazy
    private ITakeoutOrderService orderService;

    @Override
    public List<TakeoutComplaint> selectComplaintList(TakeoutComplaint complaint)
    {
        return complaintMapper.selectComplaintList(complaint);
    }

    @Override
    public TakeoutComplaint selectComplaintById(Long complaintId)
    {
        return complaintMapper.selectComplaintById(complaintId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitComplaint(TakeoutComplaint complaint)
    {
        if (StringUtils.isEmpty(complaint.getReason()))
        {
            throw new ServiceException("投诉原因不能为空");
        }
        if (StringUtils.isEmpty(complaint.getStatus()))
        {
            complaint.setStatus("0");
        }
        complaint.setCreateBy(SecurityUtils.getUsername());
        return complaintMapper.insertComplaint(complaint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processComplaint(Long complaintId, String status, BigDecimal approvedAmount, String handleRemark)
    {
        TakeoutComplaint exist = complaintMapper.selectComplaintById(complaintId);
        if (exist == null)
        {
            throw new ServiceException("[ERR_" + ERR_COMPLAINT_NOT_FOUND + "] 工单不存在", ERR_COMPLAINT_NOT_FOUND);
        }
        if ("2".equals(exist.getStatus()) || "3".equals(exist.getStatus()) || "4".equals(exist.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_COMPLAINT_DONE + "] 工单已处理完成", ERR_COMPLAINT_DONE);
        }
        if (!"1".equals(status) && !"2".equals(status) && !"3".equals(status))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_STATUS + "] 非法的处理状态: " + status, ERR_INVALID_STATUS);
        }
        TakeoutComplaint upd = new TakeoutComplaint();
        upd.setComplaintId(complaintId);
        upd.setStatus(status);
        upd.setApprovedAmount(approvedAmount);
        upd.setHandleRemark(handleRemark);
        upd.setHandleBy(SecurityUtils.getUsername());
        upd.setHandleTime(DateUtils.getNowDate());
        if ("2".equals(status) && approvedAmount != null)
        {
            upd.setRefundTime(DateUtils.getNowDate());
            upd.setRefundFlowNo("RF" + System.currentTimeMillis());
        }
        int rows = complaintMapper.processComplaint(upd);
        if (rows > 0 && "2".equals(status))
        {
            // 联动：退款成功 → 订单状态置为 REFUNDED
            try
            {
                orderService.changeOrderStatus(exist.getOrderId(), OrderStatusEnum.REFUNDED.getCode());
            }
            catch (Exception e)
            {
                log.warn("联动订单退款状态失败 orderId={}, err={}", exist.getOrderId(), e.getMessage());
            }
        }
        return rows;
    }

    @Override
    public int appealComplaint(Long complaintId, String appealContent)
    {
        TakeoutComplaint exist = complaintMapper.selectComplaintById(complaintId);
        if (exist == null)
        {
            throw new ServiceException("[ERR_" + ERR_COMPLAINT_NOT_FOUND + "] 工单不存在", ERR_COMPLAINT_NOT_FOUND);
        }
        TakeoutComplaint upd = new TakeoutComplaint();
        upd.setComplaintId(complaintId);
        upd.setAppealContent(appealContent);
        upd.setAppealTime(DateUtils.getNowDate());
        upd.setAppealStatus("0"); // 待审核
        return complaintMapper.updateAppeal(upd);
    }

    @Override
    public int deleteComplaintByIds(Long[] complaintIds)
    {
        return complaintMapper.deleteComplaintByIds(complaintIds);
    }
}
