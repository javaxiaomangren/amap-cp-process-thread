package com.amap.cp.process;

import com.amap.cp.beans.CpInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 动态信息处理
 * Created by yang.hua on 14-1-13.
 */
public class RtiProcess extends AbstractProcess {

    @Override
    public Map<String, ?> process(String cpName, Map jsonMap) {
        throw new UnsupportedOperationException("not impl yet");
    }

    @Override
    public Map<String, ?> process(String cpName, List<CpInfo> cpInfo) {
        Set<Map<String, Object>> rtis = Sets.newLinkedHashSet();
        boolean hasReviews = false;
        for (CpInfo info : cpInfo) {
            String json = info.getRti();
            if (isInvalidStr(json)) {
                continue;
            }
            if (json.contains("review") || json.contains("REVIEW")){
                hasReviews = true;
            }
            List rti = getRuleFieldsForList(ruleConfig.getList(cpName + "_rti_field"), json);
            //todo filter, 特殊过滤,commonFieldFilter, specFieldMap, commonFieldMap, commonFieldCheck, specFieldCheck

            //过滤掉重复的rti
            if (rti != null && rti.size() > 0) {
                for (Object o : rti) {
                    rtis.add((Map<String, Object>) o);
                }
            }
        }
        if (hasReviews) {
            rtis = keepOneReview(rtis);
        }
        return ImmutableMap.of("rti", rtis);
    }

}
