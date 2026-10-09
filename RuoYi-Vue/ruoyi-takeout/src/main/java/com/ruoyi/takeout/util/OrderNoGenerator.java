package com.ruoyi.takeout.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单号生成器
 *
 * 格式：TKO + yyyyMMddHHmmss + 6 位自增序列（线程安全）
 *
 * 优势：
 * 1. 不依赖数据库，调用方无需自己生成
 * 2. 同一秒内并发也能保证唯一
 * 3. 带业务前缀 TKO（Takeout Order）便于人工识别
 * 4. 长度约 21 位，远小于 UNIQUE 索引列的合理大小
 *
 * 注意：进程重启后 seq 会归零，但因为带秒级时间戳，跨进程不会撞
 */
public class OrderNoGenerator
{
    private static final AtomicLong SEQ = new AtomicLong(0L);

    /** 上一秒的时间戳（用于判断是否跨秒） */
    private static volatile long lastSecond = 0L;

    /**
     * 生成订单号
     */
    public static synchronized String generate()
    {
        long now = System.currentTimeMillis() / 1000L;
        if (now != lastSecond)
        {
            lastSecond = now;
            SEQ.set(0L);
        }
        long seq = SEQ.incrementAndGet();
        // 兜底：极端情况下 seq 溢出也只会在同一秒内撞（数据库 UK 会兜住）
        if (seq > 999999L)
        {
            seq = 1L;
        }
        return String.format("TKO%s%06d", formatTimestamp(now), seq);
    }

    private static String formatTimestamp(long seconds)
    {
        // 简单把秒数转 yyyyMMddHHmmss（避免引入 DateUtils 依赖）
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(seconds * 1000L);
        return String.format("%04d%02d%02d%02d%02d%02d",
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH),
            c.get(java.util.Calendar.HOUR_OF_DAY),
            c.get(java.util.Calendar.MINUTE),
            c.get(java.util.Calendar.SECOND));
    }

    private OrderNoGenerator() {}
}
