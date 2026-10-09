package com.ruoyi.takeout.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.takeout.domain.TakeoutDispatch;
import com.ruoyi.takeout.dto.RiderLocationDTO;
import com.ruoyi.takeout.mapper.TakeoutDispatchMapper;
import com.ruoyi.takeout.service.ITakeoutRiderLocationService;

/**
 * 骑手位置 Service 实现
 *
 * <p>位置上报是高频低优先级操作:
 *  <ul>
 *      <li>不做事务(单条 UPDATE,失败可重试)</li>
 *      <li>不写日志审计(量太大,留给埋点)</li>
 *      <li>dispatchId+riderId 双校验,防越权上报他人派单</li>
 *  </ul>
 *
 * @author ruoyi
 */
@Service
public class TakeoutRiderLocationServiceImpl implements ITakeoutRiderLocationService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutRiderLocationServiceImpl.class);

    @Autowired
    private TakeoutDispatchMapper dispatchMapper;

    @Override
    public int reportLocation(RiderLocationDTO dto)
    {
        if (dto == null || dto.getDispatchId() == null || dto.getRiderId() == null)
        {
            log.warn("reportLocation 参数缺失: {}", dto);
            return 0;
        }
        if (dto.getLng() == null || dto.getLat() == null)
        {
            log.warn("reportLocation 经纬度缺失 dispatchId={}", dto.getDispatchId());
            return 0;
        }
        // 校验派单存在 + 骑手匹配(防越权)
        TakeoutDispatch exist = dispatchMapper.selectDispatchById(dto.getDispatchId());
        if (exist == null)
        {
            log.warn("reportLocation 派单不存在 dispatchId={}", dto.getDispatchId());
            return 0;
        }
        if (exist.getRiderId() != null && !exist.getRiderId().equals(dto.getRiderId()))
        {
            log.warn("reportLocation 骑手不匹配 dispatchId={} expectRider={} actualRider={}",
                    dto.getDispatchId(), exist.getRiderId(), dto.getRiderId());
            return 0;
        }

        TakeoutDispatch upd = new TakeoutDispatch();
        upd.setDispatchId(dto.getDispatchId());
        upd.setRiderLng(dto.getLng());
        upd.setRiderLat(dto.getLat());
        upd.setLocationUpdateTime(dto.getReportTime() != null ? dto.getReportTime() : DateUtils.getNowDate());
        int rows = dispatchMapper.updateDispatch(upd);
        if (rows > 0)
        {
            log.debug("骑手位置已更新 dispatchId={} lng={} lat={}", dto.getDispatchId(), dto.getLng(), dto.getLat());
        }
        return rows;
    }

    @Override
    public List<TakeoutDispatch> listActiveDispatchLocations()
    {
        // 复用 dispatchMapper.selectDispatchList,只取 status 0/1/2
        // 由于 mapper 没有专门方法,这里在 service 层过滤
        // 出于性能考虑,直接复用 selectDispatchList 然后过滤
        TakeoutDispatch query = new TakeoutDispatch();
        List<TakeoutDispatch> all = dispatchMapper.selectDispatchList(query);
        return all.stream()
                .filter(d -> "0".equals(d.getStatus()) || "1".equals(d.getStatus()) || "2".equals(d.getStatus()))
                .filter(d -> d.getRiderLng() != null && d.getRiderLat() != null)
                .toList();
    }
}
