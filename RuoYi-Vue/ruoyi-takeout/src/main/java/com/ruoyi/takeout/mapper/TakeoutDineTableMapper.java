package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutDineTable;

/**
 * 堂食桌台 数据层
 *
 * @author ruoyi
 */
public interface TakeoutDineTableMapper
{
    /** 查询桌台列表（管理端） */
    List<TakeoutDineTable> selectDineTableList(TakeoutDineTable query);

    /** 按ID查询 */
    TakeoutDineTable selectDineTableById(Long tableId);

    /** 按桌号+商家查询(校验唯一) */
    TakeoutDineTable selectDineTableByNo(TakeoutDineTable query);

    /** 新增 */
    int insertDineTable(TakeoutDineTable table);

    /** 修改 */
    int updateDineTable(TakeoutDineTable table);

    /** 修改桌台状态(乐观锁 by id) */
    int updateDineTableStatus(TakeoutDineTable table);

    /** 软删除 */
    int deleteDineTableByIds(Long[] tableIds);

    /** 统计某商家的空闲桌台数 */
    int countIdleByMerchantId(Long merchantId);

    /** 某桌台当前进行中订单数(order_type=1 AND status IN DRAFT/PAID/KITCHEN_ACCEPT/READY) */
    int countActiveDineOrdersByTableId(Long tableId);
}
