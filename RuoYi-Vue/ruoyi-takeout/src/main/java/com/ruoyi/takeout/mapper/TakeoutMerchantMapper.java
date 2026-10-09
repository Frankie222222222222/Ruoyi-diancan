package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutMerchant;

/**
 * �����̼� ���ݲ�
 *
 * @author ruoyi
 */
public interface TakeoutMerchantMapper
{
    /**
     * ��ѯ�����̼��б���֧��������ѯ+�� �߼���
     */
    List<TakeoutMerchant> selectMerchantList(TakeoutMerchant merchant);

    /**
     * ͨ���̼�ID��ѯ�̼�
     */
    TakeoutMerchant selectMerchantById(Long merchantId);

    /**
     * ͨ�����û�ID��ѯ�̼ң�����У�� user_id Ψһ�ԣ�
     */
    TakeoutMerchant selectMerchantByUserId(Long userId);

    /**
     * ͨ���̼�ID��������ɾ��������ɾ��������ʹ�ã�
     */
    int deleteMerchantByIds(Long[] merchantIds);

    /**
     * �����̼�
     */
    int insertMerchant(TakeoutMerchant merchant);

    /**
     * �޸��̼�
     */
    int updateMerchant(TakeoutMerchant merchant);

    /**
     * �޸��̼�Ӫҵ״̬
     */
    int updateMerchantStatus(TakeoutMerchant merchant);

    /**
     * �̼���ˣ�ͨ��/���أ�
     */
    int auditMerchant(TakeoutMerchant merchant);
}