package com.amap.cp;

import com.amap.cp.client.MultiThreadExecutor;
import com.amap.cp.utils.SimpleMailSender;
import com.amap.cp.utils.Utils;

/**
 * Created by yang.hua on 14-4-1.
 */
public class Main {
    public static void main(String[] args) {
//        cp名以参数的方式传入
        int threadCount = 1;
        try {
            threadCount = Integer.valueOf(args[1]);
            new MultiThreadExecutor(args[0], threadCount).activate();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("usage: java -jar *.jar residential_jiaodian_api 4 (*.jar cpName threadCount )");
            SimpleMailSender.notify(e.getMessage(), Utils.getStackTrace(e.getStackTrace()));
        }
    }
}
