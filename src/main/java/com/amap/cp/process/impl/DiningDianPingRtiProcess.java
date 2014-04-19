package com.amap.cp.process.impl;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.AbstractProcess;
import com.amap.cp.utils.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Objects.equal;

/**
 * 点评餐饮的动态单独处理
 * Created by yang.hua on 14-4-3.
 */
public class DiningDianPingRtiProcess extends AbstractProcess {
    @Override
    public Map<String, ?> process(String cpName, Map jsonMap) {
        throw new UnsupportedOperationException("Not impl yet");
    }

    @Override
    public Map<String, ?> process(String cpName, List<CpInfo> cpInfo) {
        Set<List> rtis = Sets.newLinkedHashSet();
        for (CpInfo info : cpInfo) {
            String rtiJson = info.getRti();
            if (isInvalidStr(rtiJson)) {
                continue;
            }
            List ls = Utils.key2Lower4List(Utils.loadsJson(rtiJson, List.class));
            List resultRti = new ArrayList();
            for (Object l : ls) {
                Map map = (Map) l;
                String market = (String) map.get("market");
                if (equal("groupbuy", market)) {
                    List groupByList = (List) map.get("groupbuy_list");
                    if (groupByList != null) {
                        for (Object o : groupByList) {
                            Map groupBuy = getFieldsAsMap(ruleConfig.getList(cpName + "_groupbuy_rti_field"), (Map) o);
                            groupBuy.put("market", "groupbuy");
                            resultRti.add(groupBuy);
                        }
                    }
                } else if (equal("review", market)) {
                    Map review = getFieldsAsMap(ruleConfig.getList(cpName + "_review_rti_field"), map);
                    resultRti.add(review);
                }
                //TODO if market discount
            }
            rtis.add(resultRti);
        }
        //TODO
        return ImmutableMap.of("rti", rtis);
    }

}
