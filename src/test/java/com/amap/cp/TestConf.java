package com.amap.cp;

import com.amap.cp.utils.Utils;

/**
 * Created by yang.hua on 14-1-24.
 */
public class TestConf {
    public static void main(String[] args) {
        System.out.println(Utils.COMM_CONF.getKeys("class").next());
    }
}
