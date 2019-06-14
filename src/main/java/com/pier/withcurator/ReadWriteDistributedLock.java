package com.pier.withcurator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @auther zhongweiwu
 * @date 2019/6/14 18:28
 */
public class ReadWriteDistributedLock {

    private static final String basePath = "/rwlock";

    private String lockName;

    private InterProcessReadWriteLock lock;

    private InterProcessLock readLock;

    private InterProcessLock writeLock;

    private static final String host = "aliyun:2181";

    private static CuratorFramework client;

    private static CountDownLatch countDownLatch = new CountDownLatch(10);

    static {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(host, retryPolicy);
        client.start();
    }

    public ReadWriteDistributedLock(String lockName) {
        this.lockName = lockName;
        try {
            lock = new InterProcessReadWriteLock(client, basePath + "/" + lockName);
            readLock = lock.readLock();
            writeLock = lock.writeLock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public InterProcessLock getReadLock() {
        return readLock;
    }

    public InterProcessLock getWriteLock() {
        return writeLock;
    }

    public static void main(String[] args) throws InterruptedException {
        ReadWriteDistributedLock readWriteLock = new ReadWriteDistributedLock("pier");
        final InterProcessLock rLock = readWriteLock.getReadLock();
        final InterProcessLock  wLock = readWriteLock.getWriteLock();
        for (int i = 0; i < 10; i++) {
            final String clientName = "client#" + i;
            new Thread(new Runnable() {
                public void run() {
                    /*ReadWriteDistributedLock readWriteLock = new ReadWriteDistributedLock("pier");
                    InterProcessLock rLock = readWriteLock.getReadLock();
                    InterProcessLock  wLock = readWriteLock.getWriteLock();*/
                    try {
                        // 注意只能先得到写锁再得到读锁，不能反过来！！！
                        if (!wLock.acquire(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(clientName + " 不能得到写锁");
                        }
                        System.out.println(clientName + " 已得到写锁");
                        if (!rLock.acquire(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(clientName + " 不能得到读锁");
                        }
                        System.out.println(clientName + " 已得到读锁");
                        try {
                            //Thread.sleep(500); // 模拟使用资源
                        } finally {
                            System.out.println(clientName + " 释放读写锁");
                            rLock.release();
                            wLock.release();
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    } finally {
                        CloseableUtils.closeQuietly(client);
                        countDownLatch.countDown();
                    }
                }
            }).start();
        }
        countDownLatch.await();
        System.out.println("结束！");
    }
}
