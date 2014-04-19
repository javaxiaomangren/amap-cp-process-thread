package com.amap.cp.process.impl;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.AbstractProcess;
import com.amap.cp.utils.Utils;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

/**
 * 特殊字段处理
 * Created by yang.hua on 14-1-13.
 */
public class DiningDianPingSpecProcess extends AbstractProcess {

    @Override
    public Map<String, ?> process(String cpName, Map jsonMap) {
        Map specMap = getSpecInfo(jsonMap);
        if (specMap != null) {
            specMap = Utils.key2Lower(specMap);
            specMap = getFieldsAsMap(ruleConfig.getList(cpName + "_spec_field"), specMap);
        }
        return ImmutableMap.of("spec", ImmutableMap.of("dianping_api", specMap));
    }

    @Override
    public Map<String, ?> process(String cpName, List<CpInfo> cpInfo) {
        throw new UnsupportedOperationException("Not impl yet");
    }

    private Map getSpecInfo(Map jsonMap) {
        Map specInDeep = (Map) jsonMap.get("spec");
        if (specInDeep == null) {
            specInDeep = (Map) jsonMap.get("SPEC");
        }
        return specInDeep;
    }

}
