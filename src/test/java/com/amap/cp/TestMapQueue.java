package com.amap.cp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.http.annotation.Immutable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by yang.hua on 14-3-28.
 */
public class TestMapQueue {
    public static void main(String[] args) {
        Map<String, Queue<String>> mq = new HashMap<String, Queue<String>>();
        Queue<String> poiids = new LinkedList<String>();
        poiids.addAll(ImmutableList.of("a", "b", "c", "d")) ;
//        poiids.addAll(ImmutableList.of("a", "b", "c", "d")) ;
//        poiids.addAll(ImmutableList.of("a", "b", "c", "d")) ;
        mq.putAll(ImmutableMap.of("aaa", poiids));
        for (String s : mq.keySet()) {
            Queue<String> q = mq.get(s);
            while (!q.isEmpty()) {
                q.poll();
            }
        }
        System.out.println(mq.size());
    }
}
