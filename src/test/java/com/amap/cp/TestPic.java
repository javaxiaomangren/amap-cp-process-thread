package com.amap.cp;

import com.amap.cp.process.PicProcess;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;

/**
 * Created by yang.hua on 14-1-17.
 */
public class TestPic {
    public static void main(String[] args) throws Exception {

        List l = Lists.newArrayList();
        Map m1 = Maps.newHashMap();
        m1.put("url", "http://imgs.focus.cn/upload/bt/6436/a_64354193.jpg");
        Map m2 = Maps.newHashMap();
        m2.put("url", "http://gy.focus.cn/upload/photos/430450/p581.jpg");
        Map m3 = Maps.newHashMap();
        m3.put("url", "http://gy.focus.cn/upload/photos/430450/p581.jpg");
        Map m4 = Maps.newHashMap();
        m4.put("url", "http://imgs.focus.cn/upload/bt/6436/a_64354193.jpg");
        l.add(m1);
        l.add(m2);
        l.add(m3);
        l.add(m4);
        Map m = new PicProcess().getGaoDePic(m4);
        System.out.println(m.toString());
    }
}
