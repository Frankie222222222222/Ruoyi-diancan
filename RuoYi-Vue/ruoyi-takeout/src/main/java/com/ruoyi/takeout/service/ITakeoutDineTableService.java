package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutDineTable;

/**
 * 堂食桌台 业务层
 *
 * @author ruoyi
 */
public interface ITakeoutDineTableService
{
    /** 管理端查询 */
    List<TakeoutDineTable> selectDineTableList(TakeoutDineTable query);

    /** 按ID查询 */
    TakeoutDineTable selectDineTableById(Long tableId);

    /** 校验桌号唯一 */
    boolean checkTableNoUnique(TakeoutDineTable table);

    /** 新增(自动生成 qrUrl) */
    int insertDineTable(TakeoutDineTable table);

    /** 修改 */
    int updateDineTable(TakeoutDineTable table);

    /** 修改桌台状态(下单/结账时联动) */
    int updateDineTableStatus(Long tableId, String status);

    /** 软删除 */
    int deleteDineTableByIds(Long[] tableIds);

    /** 某商家空闲桌台数 */
    int countIdleByMerchantId(Long merchantId);

    /** 某桌台进行中订单数(用于释放桌台前校验) */
    int countActiveDineOrdersByTableId(Long tableId);

    /** 桌台当前进行中堂食订单(给顾客端查询) */
    List<com.ruoyi.takeout.domain.TakeoutOrder> selectActiveDineOrdersByTable(Long tableId);
}
