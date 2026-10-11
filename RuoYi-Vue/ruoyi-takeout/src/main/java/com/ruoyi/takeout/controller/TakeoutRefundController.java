package com.ruoyi.takeout.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.takeout.domain.TakeoutComplaint;
import com.ruoyi.takeout.service.ITakeoutComplaintService;

/**
 * 退款流水 Controller
 *
 * <p>当前实现:直接代理到 ComplaintService(type=0 = 退款申请)。
 * 后续如独立 takeout_refund 表,只需把 service 切换到独立实现,Controller 不变。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/refund")
public class TakeoutRefundController extends BaseController
{
    @Autowired
    private ITakeoutComplaintService complaintService;

    /** 列表 — 仅查退款申请 */
    @PreAuthorize("@ss.hasPermi('takeout:refund:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutComplaint query)
    {
        startPage();
        query.setType("0");
        return getDataTable(complaintService.selectComplaintList(query));
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:refund:query')")
    @GetMapping("/{complaintId}")
    public AjaxResult getInfo(@PathVariable Long complaintId)
    {
        TakeoutComplaint row = complaintService.selectComplaintById(complaintId);
        return row != null && "0".equals(row.getType()) ? success(row) : error("非退款工单");
    }

    /** 审核通过(状态 2=已退款) */
    @PreAuthorize("@ss.hasPermi('takeout:refund:process')")
    @PutMapping("/approve/{complaintId}")
    public AjaxResult approve(@PathVariable Long complaintId,
                              @RequestParam(required = false) String handleRemark)
    {
        return toAjax(complaintService.processComplaint(complaintId, "2", null, handleRemark));
    }

    /** 审核驳回(状态 3=已拒绝) */
    @PreAuthorize("@ss.hasPermi('takeout:refund:process')")
    @PutMapping("/reject/{complaintId}")
    public AjaxResult reject(@PathVariable Long complaintId,
                             @RequestParam(required = false) String handleRemark)
    {
        return toAjax(complaintService.processComplaint(complaintId, "3", null, handleRemark));
    }
}
