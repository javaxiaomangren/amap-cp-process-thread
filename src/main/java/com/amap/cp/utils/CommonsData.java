package com.amap.cp.utils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.hua on 14-3-25.
 */
public class CommonsData {
    public static Collection addAbleCpNames = initAddAbleCpNames();
    public static Map<String, String> mergedPoiid = Maps.newHashMap();
    public static boolean requireAdd(String cpName) {
        return addAbleCpNames.contains(cpName);
    }

    public static Collection initAddAbleCpNames() {
        List ls = Utils.COMM_CONF.getList("addAbleCpNames");
        if (ls != null) {
           return Collections2.transform(ls, new Function<Object, String>() {
                @Override
                public String apply(Object input) {
                    return (String) input;
                }
            });
        }
        return Lists.newArrayList();
    }

    public static void initNewPoiRelation(String cpName) {
        //TODO 以后这部分考虑做成一个http服务
        mergedPoiid = Persistence.getInstance().getNewPoiids(cpName);
    }


}
