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
 * 帮助中心 Controller(2026-10-11)
 * 表:
 *   takeout_help_category: category_id, name, sort
 *   takeout_help_article : article_id, category_id, title, content, sort
 */
@RestController
@RequestMapping("/takeout/help")
public class TakeoutHelpController extends BaseController
{
    @Autowired
    private JdbcTemplate jdbc;

    // 分类列表(后台)
    @PreAuthorize("@ss.hasPermi('takeout:help:list')")
    @GetMapping("/category/list")
    public TableDataInfo categoryList()
    {
        startPage();
        return getDataTable(jdbc.queryForList(
            "SELECT * FROM takeout_help_category WHERE del_flag = '0' ORDER BY sort ASC"));
    }

    @Anonymous
    @GetMapping("/category/all")
    public AjaxResult categoryAll()
    {
        List<Map<String, Object>> list = jdbc.queryForList(
            "SELECT * FROM takeout_help_category WHERE del_flag='0' ORDER BY sort ASC");
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('takeout:help:edit')")
    @Log(title = "帮助分类", businessType = BusinessType.INSERT)
    @PostMapping("/category")
    public AjaxResult saveCategory(@RequestBody Map<String, Object> body)
    {
        Object id = body.get("categoryId");
        Object name = body.get("name");
        Object sort = body.getOrDefault("sort", 0);
        int rows = id == null
            ? jdbc.update("INSERT INTO takeout_help_category (name, sort) VALUES (?, ?)", name, sort)
            : jdbc.update("UPDATE takeout_help_category SET name=?, sort=? WHERE category_id=?", name, sort, id);
        return rows > 0 ? success() : error();
    }

    // 文章列表(后台)
    @PreAuthorize("@ss.hasPermi('takeout:help:list')")
    @GetMapping("/article/list")
    public TableDataInfo articleList(@RequestParam(required = false) Long categoryId)
    {
        startPage();
        String sql = "SELECT * FROM takeout_help_article WHERE del_flag='0'";
        Object[] params;
        if (categoryId != null) {
            sql += " AND category_id=? ORDER BY sort ASC";
            params = new Object[] { categoryId };
        } else {
            sql += " ORDER BY sort ASC";
            params = new Object[0];
        }
        return getDataTable(jdbc.queryForList(sql, params));
    }

    @Anonymous
    @GetMapping("/article/all")
    public AjaxResult articleAll(@RequestParam(required = false) Long categoryId)
    {
        String sql = "SELECT article_id, category_id, title, content, sort FROM takeout_help_article WHERE del_flag='0'";
        Object[] params;
        if (categoryId != null) {
            sql += " AND category_id=? ORDER BY sort ASC";
            params = new Object[] { categoryId };
        } else {
            sql += " ORDER BY sort ASC";
            params = new Object[0];
        }
        return success(jdbc.queryForList(sql, params));
    }

    @PreAuthorize("@ss.hasPermi('takeout:help:edit')")
    @Log(title = "帮助文章", businessType = BusinessType.INSERT)
    @PostMapping("/article")
    public AjaxResult saveArticle(@RequestBody Map<String, Object> body)
    {
        Object id = body.get("articleId");
        Object categoryId = body.get("categoryId");
        Object title = body.get("title");
        Object content = body.get("content");
        Object sort = body.getOrDefault("sort", 0);
        int rows = id == null
            ? jdbc.update("INSERT INTO takeout_help_article (category_id, title, content, sort) VALUES (?, ?, ?, ?)",
                categoryId, title, content, sort)
            : jdbc.update("UPDATE takeout_help_article SET category_id=?, title=?, content=?, sort=? WHERE article_id=?",
                categoryId, title, content, sort, id);
        return rows > 0 ? success() : error();
    }

    @PreAuthorize("@ss.hasPermi('takeout:help:remove')")
    @Log(title = "帮助文章", businessType = BusinessType.DELETE)
    @DeleteMapping("/article/{ids}")
    public AjaxResult removeArticle(@PathVariable Long[] ids)
    {
        StringBuilder sb = new StringBuilder("UPDATE takeout_help_article SET del_flag='2' WHERE article_id IN (");
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
