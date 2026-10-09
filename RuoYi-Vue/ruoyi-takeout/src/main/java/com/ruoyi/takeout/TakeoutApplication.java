package com.ruoyi.takeout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * ����ҵ��ģ�飨ruoyi-takeout�����������ࡣ
 * <p>
 * ��ģ�鱻 ruoyi-admin ͨ�� ComponentScan ���𣬴˴������ڱ��ص������ԡ�
 * ����Դ�� ruoyi-admin ͳһ���ã������ų��Ա����ظ����á�
 * </p>
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class TakeoutApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(TakeoutApplication.class, args);
        System.out.println("(????)?  ����ҵ��ģ�������ɹ�  ?(?��??)?");
    }
}