package com.ruoyi.takeout.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.takeout.domain.TakeoutComplaint;
import com.ruoyi.takeout.service.ITakeoutComplaintService;

/**
 * 投诉工单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/complaint")
public class TakeoutComplaintController extends BaseController
{
    @Autowired
    private ITakeoutComplaintService complaintService;

    /** 列表 */
    @PreAuthorize("@ss.hasPermi('takeout:complaint:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutComplaint complaint)
    {
        startPage();
        return getDataTable(complaintService.selectComplaintList(complaint));
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:complaint:query')")
    @GetMapping("/{complaintId}")
    public AjaxResult getInfo(@PathVariable Long complaintId)
    {
        return success(complaintService.selectComplaintById(complaintId));
    }

    /** C端提交投诉/退款申请 */
    @Log(title = "投诉工单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult submit(@RequestBody TakeoutComplaint complaint)
    {
        int rows = complaintService.submitComplaint(complaint);
        return rows > 0 ? success(complaint.getComplaintId()) : error();
    }

    /** 客服处理 */
    @PreAuthorize("@ss.hasPermi('takeout:complaint:process')")
    @Log(title = "投诉工单", businessType = BusinessType.UPDATE)
    @PutMapping("/process/{complaintId}")
    public AjaxResult process(@PathVariable Long complaintId,
                              @RequestParam String status,
                              @RequestParam(required = false) BigDecimal approvedAmount,
                              @RequestParam(required = false) String handleRemark)
    {
        return toAjax(complaintService.processComplaint(complaintId, status, approvedAmount, handleRemark));
    }

    /** 用户申诉 */
    @Log(title = "投诉工单", businessType = BusinessType.UPDATE)
    @PutMapping("/appeal/{complaintId}")
    public AjaxResult appeal(@PathVariable Long complaintId, @RequestParam String content)
    {
        return toAjax(complaintService.appealComplaint(complaintId, content));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('takeout:complaint:remove')")
    @Log(title = "投诉工单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(complaintService.deleteComplaintByIds(ids));
    }
}
