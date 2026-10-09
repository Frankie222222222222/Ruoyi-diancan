package com.ruoyi.takeout.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.takeout.domain.TakeoutDispatch;
import com.ruoyi.takeout.dto.RiderLocationDTO;
import com.ruoyi.takeout.service.ITakeoutRiderLocationService;

/**
 * 骑手位置 Controller
 *
 * <p>提供两类接口:
 *  <ul>
 *      <li>{@code POST /takeout/riderLocation/report} — 骑手 APP 周期性上报坐标(免登录,带签名防伪,开发期暂时不验签)</li>
 *      <li>{@code GET  /takeout/riderLocation/active}  — 后台地图组件批量拉取所有 active 派单的骑手位置</li>
 *  </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/riderLocation")
public class TakeoutRiderLocationController extends BaseController
{
    @Autowired
    private ITakeoutRiderLocationService locationService;

    /** 骑手上报位置 */
    @PostMapping("/report")
    public AjaxResult report(@RequestBody RiderLocationDTO dto)
    {
        int rows = locationService.reportLocation(dto);
        return rows > 0 ? success() : error("上报失败,参数或派单不匹配");
    }

    /** 地图组件批量拉取进行中派单的骑手位置 */
    @GetMapping("/active")
    public AjaxResult active()
    {
        List<TakeoutDispatch> list = locationService.listActiveDispatchLocations();
        return success(list);
    }
}
