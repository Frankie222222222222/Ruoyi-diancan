package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.TakeoutDineTable;
import com.ruoyi.takeout.mapper.TakeoutDineTableMapper;
import com.ruoyi.takeout.service.ITakeoutDineTableService;
import com.ruoyi.takeout.service.ITakeoutOrderService;

/**
 * 堂食桌台 Service
 *
 * <p>桌台二维码(QR)策略: 后端不生成图片,只把
 * {@code ${dine-in.qr-base-url}?tableId={tableId}}
 * 存到 {@code qr_url} 字段,前端在管理端页面用 JS 库(如 qrcode.js)实时渲染。
 * 这样避免引入 zxing 等额外依赖。</p>
 *
 * @author ruoyi
 */
@Service
public class TakeoutDineTableServiceImpl implements ITakeoutDineTableService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutDineTableServiceImpl.class);

    @Autowired
    private TakeoutDineTableMapper dineTableMapper;

    @Autowired
    private ITakeoutOrderService takeoutOrderService;

    /** H5 扫码点餐的 URL 前缀,可在 application.yml 覆盖 */
    @Value("${dine-in.qr-base-url:https://localhost:8081/dine-in}")
    private String qrBaseUrl;

    @Override
    public List<TakeoutDineTable> selectDineTableList(TakeoutDineTable query)
    {
        return dineTableMapper.selectDineTableList(query);
    }

    @Override
    public TakeoutDineTable selectDineTableById(Long tableId)
    {
        return dineTableMapper.selectDineTableById(tableId);
    }

    @Override
    public boolean checkTableNoUnique(TakeoutDineTable table)
    {
        if (StringUtils.isEmpty(table.getTableNo()) || table.getMerchantId() == null)
        {
            return true;
        }
        TakeoutDineTable exist = dineTableMapper.selectDineTableByNo(table);
        return exist == null || exist.getTableId().equals(table.getTableId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDineTable(TakeoutDineTable table)
    {
        if (!checkTableNoUnique(table))
        {
            throw new ServiceException(
                String.format("桌号「%s」在该商家下已存在", table.getTableNo()),
                3001);
        }
        if (StringUtils.isEmpty(table.getStatus()))
        {
            table.setStatus("0");
        }
        if (table.getCapacity() == null)
        {
            table.setCapacity(4);
        }
        table.setCreateBy(SecurityUtils.getUsername());
        table.setCreateTime(DateUtils.getNowDate());
        int rows = dineTableMapper.insertDineTable(table);
        if (rows > 0)
        {
            // 生成 QR 链接(前端渲染)
            String qrUrl = buildQrUrl(table.getTableId());
            TakeoutDineTable upd = new TakeoutDineTable();
            upd.setTableId(table.getTableId());
            upd.setQrUrl(qrUrl);
            upd.setUpdateBy(SecurityUtils.getUsername());
            dineTableMapper.updateDineTable(upd);
            table.setQrUrl(qrUrl);
            log.info("dine table created: id={} no={} qr={}", table.getTableId(), table.getTableNo(), qrUrl);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDineTable(TakeoutDineTable table)
    {
        if (!checkTableNoUnique(table))
        {
            throw new ServiceException(
                String.format("桌号「%s」在该商家下已存在", table.getTableNo()),
                3001);
        }
        // 桌台若就餐中,不允许改桌号(会破坏订单关联)
        TakeoutDineTable exist = dineTableMapper.selectDineTableById(table.getTableId());
        if (exist == null)
        {
            throw new ServiceException("桌台不存在", 3002);
        }
        if ("1".equals(exist.getStatus()) && !StringUtils.isEmpty(table.getTableNo())
                && !table.getTableNo().equals(exist.getTableNo()))
        {
            throw new ServiceException("就餐中桌台不可改桌号", 3003);
        }
        table.setUpdateBy(SecurityUtils.getUsername());
        table.setUpdateTime(DateUtils.getNowDate());
        return dineTableMapper.updateDineTable(table);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDineTableStatus(Long tableId, String status)
    {
        TakeoutDineTable upd = new TakeoutDineTable();
        upd.setTableId(tableId);
        upd.setStatus(status);
        upd.setUpdateBy(SecurityUtils.getUsername());
        return dineTableMapper.updateDineTableStatus(upd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDineTableByIds(Long[] tableIds)
    {
        if (tableIds == null || tableIds.length == 0)
        {
            return 0;
        }
        // 拦截: 任何进行中订单关联的桌台不能删
        for (Long id : tableIds)
        {
            int active = dineTableMapper.countActiveDineOrdersByTableId(id);
            if (active > 0)
            {
                throw new ServiceException(
                    String.format("桌台(id=%s)存在 %d 个进行中堂食订单,无法删除", id, active),
                    3004);
            }
        }
        return dineTableMapper.deleteDineTableByIds(tableIds);
    }

    @Override
    public int countIdleByMerchantId(Long merchantId)
    {
        return dineTableMapper.countIdleByMerchantId(merchantId);
    }

    @Override
    public int countActiveDineOrdersByTableId(Long tableId)
    {
        return dineTableMapper.countActiveDineOrdersByTableId(tableId);
    }

    @Override
    public List<com.ruoyi.takeout.domain.TakeoutOrder> selectActiveDineOrdersByTable(Long tableId)
    {
        return takeoutOrderService.selectActiveDineOrdersByTable(tableId);
    }

    /** 拼接 QR 链接: ${qr-base-url}?tableId={tableId} */
    private String buildQrUrl(Long tableId)
    {
        String base = qrBaseUrl == null ? "" : qrBaseUrl;
        if (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }
        return base + "?tableId=" + tableId;
    }
}
