package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutComplaint;

/**
 * 投诉工单 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutComplaintMapper
{
    /** 查询投诉列表 */
    List<TakeoutComplaint> selectComplaintList(TakeoutComplaint complaint);

    /** 查询单个投诉 */
    TakeoutComplaint selectComplaintById(Long complaintId);

    /** 新增投诉 */
    int insertComplaint(TakeoutComplaint complaint);

    /** 修改投诉 */
    int updateComplaint(TakeoutComplaint complaint);

    /** 删除投诉(逻辑) */
    int deleteComplaintByIds(Long[] complaintIds);

    /** 处理投诉 */
    int processComplaint(TakeoutComplaint complaint);

    /** 更新申诉状态 */
    int updateAppeal(TakeoutComplaint complaint);
}
