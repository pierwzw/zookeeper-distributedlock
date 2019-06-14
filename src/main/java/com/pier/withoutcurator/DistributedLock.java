package com.pier.withoutcurator;

import java.util.concurrent.TimeUnit;

/**
 * @auther zhongweiwu
 * @date 2019/6/14 14:28
 */
 interface DistributedLock {

    /**
     * 获取锁，如果没有得到锁就一直等待
     *
     * @throws Exception
     */
     void acquire() throws Exception;

    /**
     * 获取锁，如果没有得到锁就一直等待直到超时
     *
     * @param time 超时时间
     * @param unit time参数时间单位
     *
     * @return 是否获取到锁
     * @throws Exception
     */
     boolean acquire(long time, TimeUnit unit) throws Exception;

    /**
     * 释放锁
     *
     * @throws Exception
     */
     void release() throws Exception;
}
