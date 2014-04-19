package com.amap.cp.process;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.utils.Utils;
import com.google.common.collect.ImmutableMap;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;

/**
 * Created by yang.hua on 14-1-13.
 */
public class DeepProcess extends AbstractProcess {

    @Override
    public Map<String, ?> process(String cpName, List<CpInfo> cpInfo) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, ?> process(String cpName, Map jsonMap) {
        Map deep = getRuleFieldsForMap(ruleConfig.getList(cpName + "_deep_field"), getDeepMap(jsonMap));
        return ImmutableMap.of("deep", deep);
    }

    /*有的数据有深度这个字段，有的整个json就是深度 */
    private Map getDeepMap(Map jsonMap) {
        Map deepInDeep = (Map) jsonMap.get("deep");
        if (deepInDeep == null) {
            deepInDeep = (Map) jsonMap.get("DEEP");
        }
        if (deepInDeep != null) {
            return deepInDeep;
        }
        return jsonMap;
    }

}
