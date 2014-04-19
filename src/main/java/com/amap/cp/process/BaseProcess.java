package com.amap.cp.process;

import com.amap.cp.beans.CpInfo;
import com.google.common.collect.ImmutableMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;

/**
 * 基础信息处理
 * Created by yang.hua on 14-1-13.
 */
public class BaseProcess extends AbstractProcess {

    @Override
    public Map<String, ?> process(String cpName, Map jsonMap) {
        Map base = getBaseInfo(jsonMap);
        if (base != null) {
            return ImmutableMap.of("base", base);
        }
        return null;
    }

    @Override
    public Map<String, ?> process(String cpName, List<CpInfo> cpInfo) {
        throw new NotImplementedException();
    }

    /**
     * 读取base信息，base信息不做特殊处理
     *
     * @param jsonMap 数据库deep字段数据,jsonLoads as Map
     * @return Map
     */
    public Map getBaseInfo(Map jsonMap) {
        if (jsonMap != null) {
            Map base = (Map) jsonMap.get("base");
            if (base == null) {
                base = (Map) jsonMap.get("BASE");
            }
            if (base != null && isRegularBaseInfo(base)) {
                return base;
            }
        }
        return null;
    }
}