package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.TakeoutMerchant;
import com.ruoyi.takeout.enums.AuditStatusEnum;
import com.ruoyi.takeout.enums.BusinessStatusEnum;
import com.ruoyi.takeout.mapper.TakeoutMerchantMapper;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.service.ITakeoutMerchantService;

/**
 * Merchant service implementation.
 *
 * <p>All user-facing error messages are in Chinese (UTF-8 friendly).
 * Prefix [ERR_xxxx] is preserved so the frontend can look up
 * i18n entries / trigger specific actions.</p>
 */
@Service
public class TakeoutMerchantServiceImpl implements ITakeoutMerchantService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutMerchantServiceImpl.class);

    private static final int ERR_MERCHANT_NAME_DUP = 1001;
    private static final int ERR_USER_ALREADY_BIND  = 1002;
    private static final int ERR_BUSINESS_STATUS    = 1003;
    private static final int ERR_AUDIT_STATUS       = 1004;
    private static final int ERR_MERCHANT_NOT_FOUND = 1005;
    private static final int ERR_MERCHANT_HAS_USER  = 1006;
    private static final int ERR_MERCHANT_HAS_ORDER = 1007;

    @Autowired
    private TakeoutMerchantMapper merchantMapper;

    @Autowired
    private TakeoutOrderMapper orderMapper;

    @Override
    public List<TakeoutMerchant> selectMerchantList(TakeoutMerchant merchant)
    {
        return merchantMapper.selectMerchantList(merchant);
    }

    @Override
    public TakeoutMerchant selectMerchantById(Long merchantId)
    {
        return merchantMapper.selectMerchantById(merchantId);
    }

    @Override
    public boolean checkMerchantNameUnique(TakeoutMerchant merchant)
    {
        if (StringUtils.isEmpty(merchant.getMerchantName()))
        {
            return true;
        }
        Long currentId = StringUtils.isNull(merchant.getMerchantId()) ? -1L : merchant.getMerchantId();
        List<TakeoutMerchant> list = merchantMapper.selectMerchantList(buildProbe(merchant.getMerchantName()));
        return list.stream().noneMatch(m -> !m.getMerchantId().equals(currentId));
    }

    @Override
    public boolean checkBindUserUnique(TakeoutMerchant merchant)
    {
        if (merchant.getUserId() == null)
        {
            return true;
        }
        Long currentId = StringUtils.isNull(merchant.getMerchantId()) ? -1L : merchant.getMerchantId();
        TakeoutMerchant exist = merchantMapper.selectMerchantByUserId(merchant.getUserId());
        return exist == null || exist.getMerchantId().equals(currentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMerchant(TakeoutMerchant merchant)
    {
        validateEnum(merchant);
        if (!checkMerchantNameUnique(merchant))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 商家名称「%s」已存在", ERR_MERCHANT_NAME_DUP, merchant.getMerchantName()),
                ERR_MERCHANT_NAME_DUP);
        }
        if (!checkBindUserUnique(merchant))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 系统用户 %s 已绑定其他商家", ERR_USER_ALREADY_BIND, merchant.getUserId()),
                ERR_USER_ALREADY_BIND);
        }
        if (StringUtils.isEmpty(merchant.getStatus()))
        {
            merchant.setStatus(BusinessStatusEnum.OPEN.getCode());
        }
        if (StringUtils.isEmpty(merchant.getAuditStatus()))
        {
            merchant.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        }
        merchant.setCreateBy(SecurityUtils.getUsername());
        return merchantMapper.insertMerchant(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMerchant(TakeoutMerchant merchant)
    {
        validateEnum(merchant);
        if (!checkMerchantNameUnique(merchant))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 商家名称「%s」已存在", ERR_MERCHANT_NAME_DUP, merchant.getMerchantName()),
                ERR_MERCHANT_NAME_DUP);
        }
        if (!checkBindUserUnique(merchant))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 系统用户 %s 已绑定其他商家", ERR_USER_ALREADY_BIND, merchant.getUserId()),
                ERR_USER_ALREADY_BIND);
        }
        merchant.setUpdateBy(SecurityUtils.getUsername());
        return merchantMapper.updateMerchant(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMerchantStatus(TakeoutMerchant merchant)
    {
        if (!BusinessStatusEnum.isValid(merchant.getStatus()))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 非法的营业状态：%s", ERR_BUSINESS_STATUS, merchant.getStatus()),
                ERR_BUSINESS_STATUS);
        }
        merchant.setUpdateBy(SecurityUtils.getUsername());
        return merchantMapper.updateMerchantStatus(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMerchantByIds(Long[] merchantIds)
    {
        if (merchantIds == null || merchantIds.length == 0)
        {
            return 0;
        }
        for (Long merchantId : merchantIds)
        {
            TakeoutMerchant exist = merchantMapper.selectMerchantById(merchantId);
            if (exist == null)
            {
                throw new ServiceException(
                    String.format("[ERR_%d] 商家不存在，id=%s", ERR_MERCHANT_NOT_FOUND, merchantId),
                    ERR_MERCHANT_NOT_FOUND);
            }
            if (exist.getUserId() != null)
            {
                throw new ServiceException(
                    String.format("[ERR_%d] 商家「%s」已绑定系统用户，请先解绑后删除",
                        ERR_MERCHANT_HAS_USER, exist.getMerchantName()),
                    ERR_MERCHANT_HAS_USER);
            }
            // G5: 拦截有"进行中订单"的商家 —— 避免删除后历史订单找不到商家名
            int activeOrderCount = orderMapper.countUnfinishedByMerchantId(merchantId);
            if (activeOrderCount > 0)
            {
                throw new ServiceException(
                    String.format("[ERR_%d] 商家「%s」还有 %d 个进行中订单（待支付/已支付/商家接单/配送中），请先关闭订单后再删除",
                        ERR_MERCHANT_HAS_ORDER, exist.getMerchantName(), activeOrderCount),
                    ERR_MERCHANT_HAS_ORDER);
            }
        }
        return merchantMapper.deleteMerchantByIds(merchantIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditMerchant(TakeoutMerchant merchant)
    {
        if (!AuditStatusEnum.isValid(merchant.getAuditStatus()))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 非法的审核状态：%s", ERR_AUDIT_STATUS, merchant.getAuditStatus()),
                ERR_AUDIT_STATUS);
        }
        TakeoutMerchant exist = merchantMapper.selectMerchantById(merchant.getMerchantId());
        if (exist == null)
        {
            throw new ServiceException(
                String.format("[ERR_%d] 商家不存在，id=%s", ERR_MERCHANT_NOT_FOUND, merchant.getMerchantId()),
                ERR_MERCHANT_NOT_FOUND);
        }
        merchant.setUpdateBy(SecurityUtils.getUsername());
        int rows = merchantMapper.auditMerchant(merchant);
        log.info("merchant audited: id={} status={} remark={}",
                merchant.getMerchantId(), merchant.getAuditStatus(), merchant.getAuditRemark());
        return rows;
    }

    private void validateEnum(TakeoutMerchant merchant)
    {
        if (StringUtils.isNotEmpty(merchant.getStatus()) && !BusinessStatusEnum.isValid(merchant.getStatus()))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 非法的营业状态：%s", ERR_BUSINESS_STATUS, merchant.getStatus()),
                ERR_BUSINESS_STATUS);
        }
        if (StringUtils.isNotEmpty(merchant.getAuditStatus()) && !AuditStatusEnum.isValid(merchant.getAuditStatus()))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 非法的审核状态：%s", ERR_AUDIT_STATUS, merchant.getAuditStatus()),
                ERR_AUDIT_STATUS);
        }
    }

    private TakeoutMerchant buildProbe(String name)
    {
        TakeoutMerchant probe = new TakeoutMerchant();
        probe.setMerchantName(name);
        return probe;
    }
}