package com.pier.withcurator;

import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;

/**
 * @auther zhongweiwu
 * @date 2019/6/14 16:07
 */
public class DistributedLock {

    private static final String basePath = "/curator";

    private String lockName;
    // 可重入
    private InterProcessMutex lock;
    private static final String host = "aliyun:2181";

    private static CuratorFramework client;

    static {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(host, retryPolicy);
        client.start();
    }

    public DistributedLock(String lockName) {
        this.lockName = lockName;
        try {
            lock = new InterProcessMutex(client, basePath + "/" + lockName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tryLock(long time, TimeUnit timeUnit) {
        try {
            lock.acquire(time, timeUnit);
            System.out.println(Thread.currentThread().getId() + "获取锁成功");
        }
        catch (Exception e) {
            System.out.println(Thread.currentThread().getId() + "获取锁失败");
            e.printStackTrace();
        }
    }

    public void tryLock(){
        try {
            lock.acquire();
            System.out.println(Thread.currentThread().getId() + "获取锁成功");
        }
        catch (Exception e) {
            System.out.println(Thread.currentThread().getId() + "获取锁失败");
            e.printStackTrace();
        }
    }

    /****
     * 释放锁
     */
    public void release() {
        try {
            if (lock != null && lock.isAcquiredInThisProcess()) {
                lock.release();
                System.out.println(Thread.currentThread().getId() + "释放锁成功");
            }
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getId() + "释放锁失败");
            e.printStackTrace();
        }
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public static void main(String[] args) {
        DistributedLock lock = new DistributedLock("pier");
        for (int i = 0; i < 10; i++) {
            try {
                lock.tryLock();
                System.out.println("正在进行运算操作：" + System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.release();
                System.out.println("=================\r\n");
            }
        }
    }
}
