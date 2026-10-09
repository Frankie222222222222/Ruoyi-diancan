package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutMerchant;

/**
 * �����̼� ҵ���
 *
 * @author ruoyi
 */
public interface ITakeoutMerchantService
{
    /**
     * ��ѯ�����̼��б�
     */
    List<TakeoutMerchant> selectMerchantList(TakeoutMerchant merchant);

    /**
     * ��ѯ�����̼�
     */
    TakeoutMerchant selectMerchantById(Long merchantId);

    /**
     * У���̼�����Ψһ
     */
    boolean checkMerchantNameUnique(TakeoutMerchant merchant);

    /**
     * У���ϵͳ�û�Ψһ
     */
    boolean checkBindUserUnique(TakeoutMerchant merchant);

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
     * ɾ���̼ң�֧�ְ�ɾ����
     */
    int deleteMerchantByIds(Long[] merchantIds);

    /**
     * �̼���ˣ�ͨ��/���أ�
     */
    int auditMerchant(TakeoutMerchant merchant);
}