package com.ruoyi.takeout.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 运营 Banner Controller(2026-10-11)
 *
 * <p>走 JdbcTemplate,无 mapper。表 takeout_banner 必须在 sql/takeout_banner.sql 建好。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/banner")
public class TakeoutBannerController extends BaseController
{
    @Autowired
    private JdbcTemplate jdbc;

    /** 列表(后台) */
    @PreAuthorize("@ss.hasPermi('takeout:banner:list')")
    @GetMapping("/list")
    public TableDataInfo list()
    {
        startPage();
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT * FROM takeout_banner WHERE del_flag = '0' ORDER BY sort ASC, banner_id DESC");
        return getDataTable(list);
    }

    /** C 端拉取启用的 banner(匿名) */
    @com.ruoyi.common.annotation.Anonymous
    @GetMapping("/active")
    public AjaxResult active()
    {
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT banner_id, title, image, link, sort FROM takeout_banner " +
            "WHERE del_flag = '0' AND status = '0' ORDER BY sort ASC");
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('takeout:banner:edit')")
    @Log(title = "Banner", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Map<String, Object> body)
    {
        Object id = body.get("bannerId");
        Object title = body.get("title");
        Object image = body.get("image");
        Object link = body.get("link");
        Object sort = body.getOrDefault("sort", 0);
        Object status = body.getOrDefault("status", "0");
        int rows;
        if (id == null) {
            rows = jdbc.update(
                "INSERT INTO takeout_banner (title, image, link, sort, status) VALUES (?, ?, ?, ?, ?)",
                title, image, link, sort, status);
        } else {
            rows = jdbc.update(
                "UPDATE takeout_banner SET title=?, image=?, link=?, sort=?, status=? WHERE banner_id=?",
                title, image, link, sort, status, id);
        }
        return rows > 0 ? success() : error();
    }

    @PreAuthorize("@ss.hasPermi('takeout:banner:remove')")
    @Log(title = "Banner", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        StringBuilder sb = new StringBuilder("UPDATE takeout_banner SET del_flag='2' WHERE banner_id IN (");
        Object[] params = new Object[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
            params[i] = ids[i];
        }
        sb.append(')');
        return toAjax(jdbc.update(sb.toString(), params));
    }
}
