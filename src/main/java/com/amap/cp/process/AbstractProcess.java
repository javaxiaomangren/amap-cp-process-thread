package com.amap.cp.process;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.base.Objects.equal;

/**
 * 处理流程
 * Created by yang.hua on 14-1-15.
 */
public abstract class AbstractProcess {
    protected static final PropertiesConfiguration conf = Utils.COMM_CONF;
    protected static final PropertiesConfiguration ruleConfig = Utils.RULE_CONF;
    protected final Logger logger = LoggerFactory.getLogger(AbstractProcess.class);

    /**
     * deep字段处理流程
     *
     * @param cpName cp名称
     * @return Map
     */
    abstract public Map<String, ?> process(String cpName,Map jsonMap);

    /**
     * 处理动态信息
     * @param cpName
     * @param cpInfo
     * @return
     */
    abstract public Map<String, ?> process(String cpName, List<CpInfo> cpInfo);

    /**
     * 根据配置的字段组装推送数据格式
     *
     * @param rules 可以上线字段
     * @param jsonMap  数据源
     * @return 根据规格获取的Map
     */

    protected Map getRuleFieldsForMap(final List rules, Map jsonMap) {
        jsonMap.remove("base");
        jsonMap.remove("BASE");
        jsonMap.remove("impl");
        jsonMap.remove("SPEC");
        Map map = Utils.key2Lower(jsonMap);
        return getFieldsAsMap(rules, map);
    }

    protected List getRuleFieldsForList(final List rules, String json) {
        List t = Utils.loadsJson(json, List.class);
        List list = Utils.key2Lower4List(t);
        List<Object> resultLs = Lists.newArrayList();
        for (Object o : list) {
            if (o != null && o instanceof Map) {
                resultLs.add(getFieldsAsMap(rules, (Map) o));
            } else {
                resultLs.add(o);
            }

        }
        return resultLs;
    }

    /**
     * 根据配置规格，从源数据里拿到配置的字段值
     *
     * @param rules 规格名称列表
     * @param obj   所有Key都转换为小写后的Map
     * @return Map
     */
    public LinkedHashMap getFieldsAsMap(List rules, Map<String, Object> obj) {
        LinkedHashMap<String, Object> linkedMap = Maps.newLinkedHashMap();
        for (Object field : rules) {
            String strField = (String) field;
            linkedMap.put(strField, obj.get(strField));
        }
        return linkedMap;
    }

    /**
     * 检验是否为有效的base信息
     *
     * @param baseMap base信息对应的Map
     * @return base是否有效
     */
    public boolean isRegularBaseInfo(Map baseMap) {
        return baseMap.keySet().size() > 25
                && baseMap.keySet().contains("checked");
    }

    /**
     * 多条评论只保留一个id小的评论
     * @param rtis
     * @return
     */
    public Set<Map<String, Object>> keepOneReview(Set<Map<String, Object>> rtis) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        boolean flag = true;
        for (Map<String, Object> rti : rtis) {
            if (rti.containsKey("review")) {
                if (flag) {
                    flag = false;
                    result.add(rti);
                }
            } else {
                result.add(rti);
            }
        }
        return result;
    }

    public boolean isInvalidStr(String src) {
        return equal(src, null) || equal(src, "[]") || equal(src, "") || equal(src, "null");
    }
}
