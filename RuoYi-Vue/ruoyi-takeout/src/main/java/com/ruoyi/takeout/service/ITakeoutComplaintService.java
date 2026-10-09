package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutComplaint;

/**
 * 投诉工单 Service接口
 *
 * @author ruoyi
 */
public interface ITakeoutComplaintService
{
    /** 查询投诉列表 */
    List<TakeoutComplaint> selectComplaintList(TakeoutComplaint complaint);

    /** 查询单个投诉 */
    TakeoutComplaint selectComplaintById(Long complaintId);

    /** 用户提交投诉/退款申请 */
    int submitComplaint(TakeoutComplaint complaint);

    /** 客服处理投诉 */
    int processComplaint(Long complaintId, String status, java.math.BigDecimal approvedAmount, String handleRemark);

    /** 用户申诉 */
    int appealComplaint(Long complaintId, String appealContent);

    /** 删除投诉(逻辑) */
    int deleteComplaintByIds(Long[] complaintIds);
}
