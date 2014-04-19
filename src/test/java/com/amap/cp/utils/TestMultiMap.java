package com.amap.cp.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Created by yang.hua on 14-3-14.
 */
public class TestMultiMap {
    public static void main(String[] args) {
        Multimap mp = ArrayListMultimap.create();
        mp.put("a", 1);
        mp.put("b", 2);
        mp.put("a", 3);
        System.out.println(mp.values().iterator().next());
        System.out.println(mp.values().iterator().next());
        System.out.println(mp.values().iterator().next());
    }
}
