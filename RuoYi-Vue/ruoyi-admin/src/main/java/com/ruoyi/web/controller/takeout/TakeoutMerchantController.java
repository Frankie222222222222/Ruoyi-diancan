package com.ruoyi.web.controller.takeout;

import java.util.Arrays;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.takeout.domain.TakeoutMerchant;
import com.ruoyi.takeout.service.ITakeoutMerchantService;

/**
 * 外卖商家管理 Controller
 *
 * 路由前缀：/takeout/merchant
 * 权限标识：takeout:merchant:list / query / add / edit / remove / export / status / audit
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/merchant")
public class TakeoutMerchantController extends BaseController
{
    @Autowired
    private ITakeoutMerchantService merchantService;

    /**
     * 查询商家列表
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutMerchant merchant)
    {
        startPage();
        List<TakeoutMerchant> list = merchantService.selectMerchantList(merchant);
        return getDataTable(list);
    }

    /**
     * 导出商家列表
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:export')")
    @Log(title = "商家管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TakeoutMerchant merchant)
    {
        List<TakeoutMerchant> list = merchantService.selectMerchantList(merchant);
        ExcelUtil<TakeoutMerchant> util = new ExcelUtil<TakeoutMerchant>(TakeoutMerchant.class);
        util.exportExcel(response, list, "商家数据");
    }

    /**
     * 根据商家ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:query')")
    @GetMapping(value = "/{merchantId}")
    public AjaxResult getInfo(@PathVariable("merchantId") Long merchantId)
    {
        return success(merchantService.selectMerchantById(merchantId));
    }

    /**
     * 新增商家
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:add')")
    @Log(title = "商家管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody TakeoutMerchant merchant)
    {
        if (!merchantService.checkMerchantNameUnique(merchant))
        {
            return error("新增商家'" + merchant.getMerchantName() + "'失败，商家名称已存在");
        }
        if (!merchantService.checkBindUserUnique(merchant))
        {
            return error("新增商家失败，绑定的系统用户已被其他商家占用");
        }
        int rows = merchantService.insertMerchant(merchant);
        return rows > 0 ? success(merchant.getMerchantId()) : error();
    }

    /**
     * 修改商家
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:edit')")
    @Log(title = "商家管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody TakeoutMerchant merchant)
    {
        if (!merchantService.checkMerchantNameUnique(merchant))
        {
            return error("修改商家'" + merchant.getMerchantName() + "'失败，商家名称已存在");
        }
        if (!merchantService.checkBindUserUnique(merchant))
        {
            return error("修改商家失败，绑定的系统用户已被其他商家占用");
        }
        return toAjax(merchantService.updateMerchant(merchant));
    }

    /**
     * 修改营业状态
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:status')")
    @Log(title = "商家管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody TakeoutMerchant merchant)
    {
        return toAjax(merchantService.updateMerchantStatus(merchant));
    }

    /**
     * 删除商家（支持批量）
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:remove')")
    @Log(title = "商家管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{merchantIds}")
    public AjaxResult remove(@PathVariable("merchantIds") Long[] merchantIds)
    {
        return toAjax(merchantService.deleteMerchantByIds(merchantIds));
    }

    /**
     * 商家审核（通过 / 驳回）
     */
    @PreAuthorize("@ss.hasPermi('takeout:merchant:audit')")
    @Log(title = "商家管理", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody TakeoutMerchant merchant)
    {
        return toAjax(merchantService.auditMerchant(merchant));
    }
}