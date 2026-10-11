package com.ruoyi.takeout.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 平台公告 Controller(2026-10-11)
 * 表 takeout_announcement:announcement_id, title, content, status, create_time
 */
@RestController
@RequestMapping("/takeout/announcement")
public class TakeoutAnnouncementController extends BaseController
{
    @Autowired
    private JdbcTemplate jdbc;

    @PreAuthorize("@ss.hasPermi('takeout:announcement:list')")
    @GetMapping("/list")
    public TableDataInfo list()
    {
        startPage();
        return getDataTable(jdbc.queryForList(
            "SELECT * FROM takeout_announcement WHERE del_flag = '0' ORDER BY announcement_id DESC"));
    }

    @Anonymous
    @GetMapping("/active")
    public AjaxResult active()
    {
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT announcement_id, title, content, create_time FROM takeout_announcement " +
            "WHERE del_flag='0' AND status='0' ORDER BY announcement_id DESC LIMIT 5");
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('takeout:announcement:edit')")
    @Log(title = "公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Map<String, Object> body)
    {
        Object id = body.get("announcementId");
        Object title = body.get("title");
        Object content = body.get("content");
        Object status = body.getOrDefault("status", "0");
        int rows;
        if (id == null) {
            rows = jdbc.update(
                "INSERT INTO takeout_announcement (title, content, status) VALUES (?, ?, ?)",
                title, content, status);
        } else {
            rows = jdbc.update(
                "UPDATE takeout_announcement SET title=?, content=?, status=? WHERE announcement_id=?",
                title, content, status, id);
        }
        return rows > 0 ? success() : error();
    }

    @PreAuthorize("@ss.hasPermi('takeout:announcement:remove')")
    @Log(title = "公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        StringBuilder sb = new StringBuilder("UPDATE takeout_announcement SET del_flag='2' WHERE announcement_id IN (");
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
