package com.pier.withoutcurator;

import org.I0Itec.zkclient.ZkClient;

/**
 * @auther zhongweiwu
 * @date 2019/6/14 14:37
 */
public class LockTest {

    public static void main(String[] args) throws Exception {
        ZkClient zkClient = new ZkClient("aliyun:2181", 3000);
        SimpleDistributedLock simple = new SimpleDistributedLock(zkClient, "/locker");

        for (int i = 0; i < 10; i++) {
            try {
                simple.acquire();
                System.out.println("正在进行运算操作：" + System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                simple.release();
                System.out.println("=================\r\n");
            }
        }
    }
}
