package com.amap.cp.utils;

import java.util.Date;

/**
 * Created by yang.hua on 14-3-14.
 */
public class TestUitls {
    public static void main(String[] args) {
        System.out.println(new Date().getTime());
        System.out.println(Utils.getDate("2014-03-13 18:51:44").getTime());
    }
}
