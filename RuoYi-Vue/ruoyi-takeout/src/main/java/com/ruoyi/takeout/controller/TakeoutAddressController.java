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
 * 收货地址 Controller
 *
 * <p>走 JdbcTemplate 写,避免再生成 Mybatis Mapper/XML。
 * 字段与 takeout_address 表一一对应,前端传 JSON,后端直接 map 写库。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/address")
public class TakeoutAddressController extends BaseController
{
    @Autowired
    private JdbcTemplate jdbc;

    /**
     * 列表(分页)
     * GET /takeout/address/list?userId=&pageNum=&pageSize=
     */
    @PreAuthorize("@ss.hasPermi('takeout:address:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) Long userId)
    {
        startPage();
        StringBuilder sql = new StringBuilder(
            "SELECT address_id, user_id, name, phone, address, house_number, lat, lng, is_default, del_flag, create_time, update_time " +
            "FROM takeout_address WHERE del_flag = '0' "
        );
        Object[] params;
        if (userId != null) {
            sql.append("AND user_id = ? ORDER BY is_default DESC, create_time DESC");
            params = new Object[] { userId };
        } else {
            sql.append("ORDER BY create_time DESC");
            params = new Object[0];
        }
        List<Map<String, Object>> list = jdbc.queryForList(sql.toString(), params);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:address:query')")
    @GetMapping("/{addressId}")
    public AjaxResult getInfo(@PathVariable Long addressId)
    {
        Map<String, Object> row = jdbc.queryForMap(
            "SELECT * FROM takeout_address WHERE address_id = ? AND del_flag = '0'", addressId);
        return success(row);
    }

    /**
     * 新增 / 编辑 (upsert)
     * POST /takeout/address
     * body: { addressId?, userId, name, phone, address, houseNumber, lat, lng, isDefault }
     */
    @PreAuthorize("@ss.hasPermi('takeout:address:edit')")
    @Log(title = "收货地址", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Map<String, Object> body)
    {
        Long addressId = body.get("addressId") == null ? null : Long.valueOf(body.get("addressId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        String name = (String) body.get("name");
        String phone = (String) body.get("phone");
        String address = (String) body.get("address");
        String houseNumber = (String) body.get("houseNumber");
        Object lat = body.get("lat");
        Object lng = body.get("lng");
        String isDefault = body.get("isDefault") == null ? "0" : body.get("isDefault").toString();

        if ("1".equals(isDefault)) {
            // 同一用户其它地址取消默认
            jdbc.update("UPDATE takeout_address SET is_default = '0' WHERE user_id = ?", userId);
        }

        int rows;
        if (addressId == null) {
            rows = jdbc.update(
                "INSERT INTO takeout_address (user_id, name, phone, address, house_number, lat, lng, is_default) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                userId, name, phone, address, houseNumber, lat, lng, isDefault);
        } else {
            rows = jdbc.update(
                "UPDATE takeout_address SET name=?, phone=?, address=?, house_number=?, lat=?, lng=?, is_default=? " +
                "WHERE address_id = ?",
                name, phone, address, houseNumber, lat, lng, isDefault, addressId);
        }
        return rows > 0 ? success() : error("保存失败");
    }

    /**
     * 设为默认
     * PUT /takeout/address/default/{addressId}
     */
    @PreAuthorize("@ss.hasPermi('takeout:address:edit')")
    @PutMapping("/default/{addressId}")
    public AjaxResult setDefault(@PathVariable Long addressId)
    {
        Map<String, Object> row = jdbc.queryForMap(
            "SELECT user_id FROM takeout_address WHERE address_id = ?", addressId);
        Long userId = ((Number) row.get("user_id")).longValue();
        jdbc.update("UPDATE takeout_address SET is_default = '0' WHERE user_id = ?", userId);
        int rows = jdbc.update("UPDATE takeout_address SET is_default = '1' WHERE address_id = ?", addressId);
        return rows > 0 ? success() : error();
    }

    /** 软删除 */
    @PreAuthorize("@ss.hasPermi('takeout:address:remove')")
    @Log(title = "收货地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        if (ids == null || ids.length == 0) return error("ids 不能为空");
        StringBuilder sb = new StringBuilder("UPDATE takeout_address SET del_flag = '2' WHERE address_id IN (");
        Object[] params = new Object[ids.length];
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
            params[i] = ids[i];
        }
        sb.append(')');
        int rows = jdbc.update(sb.toString(), params);
        return rows > 0 ? success() : error();
    }
}
