package com.amap.cp.save;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.*;
import com.amap.cp.utils.CommonsData;
import com.amap.cp.utils.HttpclientUtil;
import com.amap.cp.utils.MatchImpl;
import com.amap.cp.utils.Utils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.codehaus.jackson.JsonNode;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Object;
import java.util.*;

import static com.google.common.base.Objects.equal;

/**
 * TODO 这个类逻辑比较复杂，需要精简,分解功能，解耦
 * Created by yang.hua on 14-1-9.
 */

public abstract class AbstractSave {
    public Logger logger = LoggerFactory.getLogger(AbstractSave.class);
    public static PropertiesConfiguration conf = Utils.COMM_CONF;
    private Map<String, AbstractProcess> processMap = Maps.newLinkedHashMap();

    /**
     * 添加处理逻辑的实现，比如：深度数据，基础数据，动态数据
     */
    public void setOrderProcess(String key, AbstractProcess process) {
        processMap.put(key, process);
    }

    /**
     * 组装处理流程
     *
     * @param cps
     * @return
     * @throws Exception
     */
    public abstract Map<String, Object> postInMultiThread(Collection<CpInfo> cps) throws Exception;

    /**
     * 核心处理流程
     *
     * @throws Exception
     */


    public Map<String, Object> assembly(Collection<CpInfo> cps) throws Exception {
        /*保存处理结果*/
        Map<String, Object> resultObj = Maps.newLinkedHashMap();
        /*待处理cp，默认该poi下只有一条数据*/
        CpInfo selectedCp = cps.iterator().next();
        boolean isValidate = true; // 标记是否为有效cp
        boolean isNewPoiid; //标记是否为新poiid
        /*该poiid下的所有id, 用户sql update*/
        Collection<String> cpIds = Collections2.transform(cps, new Function<CpInfo, String>() {
            @Override
            public String apply(CpInfo input) {
                return input.getId();
            }
        });

        try {
            StopWatch watch = new Slf4JStopWatch();
            watch.start("isNewPoiid", "check poiid is new or not");
            int b = isNewPoiid(selectedCp.getPoiid());
            isNewPoiid = b != 0;
            watch.stop();
        } catch (Exception e) {
            logger.info("Can't check poiid {} exists or not,exception:[{}]", selectedCp.getPoiid(), e.getMessage());
            return null;
        }
        /*新增入库，不入库 flag=-1*/
        if (isNewPoiid && !CommonsData.requireAdd(selectedCp.getCpName())) {
            return getBatchUpdateParams(selectedCp.getCpName(), selectedCp.getPoiid(), cpIds, -1);
        }
        /*动态信息需要处理全部有效的cp*/
        List<CpInfo> forRti = ImmutableList.copyOf(cps);
        /*如果该poiid下有多条数据，只取最新一条有效的*/
        if (cps.size() > 1) {
            /*有效的cp*/
            Collection<CpInfo> validCpInfos = Collections2.filter(cps, new Predicate<CpInfo>() {
                @Override
                public boolean apply(CpInfo input) {
                    return isValidCp(input.getDeep());
                }
            });

            forRti = ImmutableList.copyOf(validCpInfos);
            /*取最新的一条cp, TODO 最新且有动态的优先？？*/
            if (validCpInfos != null && !validCpInfos.isEmpty()) {
                selectedCp = getLastUpdateCp(validCpInfos);
            } else {
                /*这里要判断是否为新cp，如果是新的flag=-2，不再处理*/
                if (isNewPoiid) {
                    return getBatchUpdateParams(selectedCp.getCpName(), selectedCp.getPoiid(), cpIds, -2);
                }
                isValidate = false;
            }
        } else {
            isValidate = isValidCp(selectedCp.getDeep());
            if (!isValidate) {
                /*没有有效cp,且为新增*/
                if (isNewPoiid) {
                    return getBatchUpdateParams(selectedCp.getCpName(), selectedCp.getPoiid(), cpIds, -2);
                }
            }
        }
        String cpName = selectedCp.getCpName();
        String poiid = selectedCp.getPoiid();
        resultObj.put("poiid", poiid);
        resultObj.put("from", getFromInfo(selectedCp.getId(), null, 1, selectedCp.getCpName()));
        if (isValidate) {   // 有效cp才处理，如果为无效的cp只组装from
            /* 当前信息不是下线信息，开始调用处理方法*/
            for (String key : processMap.keySet()) {
                Map<String, ?> result;
                if (equal("rti", key)) {
                    result = processMap.get(key).process(cpName, forRti);
                } else {
                    result = processMap.get(key).process(cpName, Utils.loadsJson(selectedCp.getDeep(), Map.class));
                }
                if (result != null) {
                    resultObj.putAll(result);
                }
            }
            // 根据deep信息组装from信息
            Object deep = resultObj.get("deep");
            if (deep != null) {
                Map deepMap = (Map) deep;
                if (deepMap.get("opt_type") == null) {
                    resultObj.put("from", getFromByDeep(deepMap, cpName, selectedCp.getId()));
                } else {
                    // 来源信息中已经有from字段，直接拿来用即可
                    resultObj.put("from", getExistFrom(deepMap, cpName));
                }
            }
            /*特殊字段处理*/
            StopWatch hx_watch = new Slf4JStopWatch();
            hx_watch.start("reProcessAssembled");
            resultObj = reProcessAssembled(resultObj);
            hx_watch.stop();
            setIdDictionary(resultObj, selectedCp.getId(), cpName);
        }

        boolean success = replacePoiid(cpName, selectedCp.getId(), poiid, resultObj);
        if (!success) {
            return null;
        }
        //此功能
        if (isNewPoiid && CommonsData.requireAdd(selectedCp.getCpName())) {
            //TODO: 如果是新增的，需要调用匹配接口，接口返回有两种情况：null或者和poiid不同
            Map base = (Map) resultObj.get("base");
            if (base != null) {
                Map from = (Map) resultObj.get("from");
                String matchedPoiid = MatchImpl.getMatchedPoiid(base, (String) from.get("src_type"), selectedCp.getId());
                //TODO 如uo没有匹配上咋办
                if (Strings.isNullOrEmpty(matchedPoiid)) {
                    return getBatchUpdateParams(cpName, poiid, cpIds, -1);
                }
                from.put("opt_type", "a");
                resultObj.put("from", from);
                Map saveParam = ImmutableMap.of("json", Utils.dumpObjectAsString(resultObj),
                        "mergedid", poiid, "usingid", matchedPoiid);
                boolean posted = doPost(conf.getString("url.saveall"), saveParam);
                if (!posted) {
                    return null;
                }
                return getBatchUpdateParams(cpName, poiid, cpIds, 0);
            }
            return getBatchUpdateParams(cpName, poiid, cpIds, -1);
        }
        /*推送数据*/
        String resultJson = Utils.dumpObjectAsString(resultObj);
        StopWatch watch = new Slf4JStopWatch();
        watch.start("url.savePoi", "post poi to cms");
        boolean posted = postData(resultJson);
        watch.stop();
        if (!posted) {
            return null;
        }
        return getBatchUpdateParams(cpName, poiid, cpIds, 0);
    }

    private void setIdDictionary(Map<String, Object> resultObj, String id, String cpName) {
        Map<String, Object> ids = Maps.newLinkedHashMap();
        ids.put(cpName + "_id", id);

        if (cpName.contains("dianping")) {
            ids.put("dianping_api_id", id);
        } else if (equal(cpName.toLowerCase(), "hotel_ctrip_wireless_api")) {
            ids.put(cpName + "_city_id", ((Map) resultObj.get("from")).get("city"));
        }
        resultObj.put("idDictionaries", ids);
    }

    /**
     * 如果存在旧有匹配关系，需要把旧有的数据下线
     * 旧匹配关系中，为了防止新poiid和旧poiid是融合与被融合关系，
     * 需要把旧json中到的from时间调成小于新json中的时间
     *
     * @param resultObj 如果存在新poiid这个对象将被修改
     */
    private boolean replacePoiid(String cpName, String id, String oldPoiid, Map<String, Object> resultObj) {
        String newPoiid = CommonsData.mergedPoiid.get(cpName + id);
        if (newPoiid != null && !equal(newPoiid, oldPoiid)) {
            resultObj.put("poiid", newPoiid);
            Map base = (Map) resultObj.get("base");
            if (base != null) {
                base.put("poiid", newPoiid);
            }
            //下线旧poiid
            Map<String, Object> underLine = Maps.newLinkedHashMap();
            underLine.put("poiid", oldPoiid);
            Map from = (Map) resultObj.get("from");
            underLine.put("from", from);
            boolean b = postData(Utils.dumpObjectAsString(underLine));
            if (b) {
                from.put("update_time", Utils.getDateStr(new Date()));
                logger.info("replace old poiid[{}] as new poiid [{}]", newPoiid, oldPoiid);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 特殊字段处理，这个方法由子类去实现
     * 比如焦点数据的hxpic_info @Link JiaoDianSave
     *
     * @param resultObj Map
     * @return Map
     */
    protected Map<String, Object> reProcessAssembled(Map<String, Object> resultObj) {
        return resultObj;
    }

    /*多条cp的情况下，取最近修改的一条*/
    private CpInfo getLastUpdateCp(Collection<CpInfo> validCpInfos) {
        List<CpInfo> validList = Lists.newArrayList();
        validList.addAll(validCpInfos);
        Collections.sort(validList, new Comparator<CpInfo>() {
            @Override
            public int compare(CpInfo o1, CpInfo o2) {
                return o2.getUpdateTime().compareTo(o1.getUpdateTime());
            }
        });
        //TODO 最新且有动态的？
        return validList.get(0);
    }

    /**
     * 数据库update 语句的参数
     */
    public Map<String, Object> getBatchUpdateParams(String cpName, String poiid, Collection<String> ids, int flagValue) {
        Map<String, Object> mp = Maps.newHashMap();
        mp.put("flagValue", flagValue);
        mp.put("ids", ids);
        mp.put("poiid", poiid);
        mp.put("cpName", cpName);
        return mp;
    }

    /**
     * 调用save接口，推送数据到服务器
     *
     * @param resultJson 处理好的json
     * @return boolean
     */
    public boolean postData(String resultJson) {
        String url = conf.getString("url.savePoi");
        Map param = ImmutableMap.of("json", resultJson);
        return doPost(url, param);
    }

    public boolean doPost(String url, Map param) {
        if ("true".equals(conf.getString("isDebug"))) {
            System.out.println(param.get("json"));
            return true;
        }
        String saveResult;
        boolean b = false;
        for (int i = 0; i < 3; i++) {
            try {
                saveResult = HttpclientUtil.post(url, param, "UTF-8");
                b = equal("0", Utils.loadsJson(saveResult).get("statuscode").asText());
                if (b) {
                    break;
                }
            } catch (Exception e) {
                logger.error("Failed to save poiid, message{}, exception:{}", e.getMessage(), Utils.getStackTrace(e.getStackTrace()));
            }
        }
        return b;
    }

    public Object getFromByDeep(Map deep, String cpName, String id) {
        Object url = deep.get("url");
        Object src_version = deep.get("src_version");
        Object update_flag = deep.get("update_flag");
        Object srcType = deep.get("src_type");
        Map from = new LinkedHashMap();

        if (equal("2", update_flag)) {
            from.put("opt_type", "d");
        } else {
            from.put("opt_type", "u");
        }
        if ("hotel_ctrip_wireless_api".equalsIgnoreCase(cpName)) {
            from.put("city", deep.get("city") == null || deep.get("city").equals("") || deep.get("city").equals("null") ? "1" : deep.get("city"));
        }

        //dianping来源的话，from中需要增加group_src_type
        if (deep.keySet().contains("group_src_type")) {
            from.put("group_src_type", deep.get("group_src_type"));
        }
        // 获取update_time
        from.put("update_time", Utils.getDateStr(new Date()));
        if (srcType == null) {
            srcType = cpName;
        }
        from.put("src_type", srcType);
        from.put("src_version", src_version == null || src_version.equals("") ? "1" : src_version);
        from.put("src_id", id);
        from.put("src_url", url);
        return from;
    }

    /**
     * 生成from字段
     *
     * @param updateFlag if is 2 set opt_type = d
     * @return from Map
     */
    public Map<String, Object> getFromInfo(String srcId, String srcUrl, int updateFlag, String cpName) {
        Map<String, Object> from = new LinkedHashMap<String, Object>();
        from.put("opt_type", "u");
        if (updateFlag == 2) {
            from.put("opt_type", "d");
        }
        from.put("update_time", Utils.getDateStr(new Date()));
        from.put("src_type", cpName);
        from.put("src_version", "1");
        from.put("src_id", srcId);
        from.put("src_url", srcUrl);
        return from;
    }

    public Object getExistFrom(Map result, String cp) {
        Map from = new LinkedHashMap();
        from.put("opt_type", result.get("opt_type"));
        // 获取update_time
        from.put("update_time", Utils.getDateStr(new Date()));
        from.put("src_type", cp);
        from.put("src_version", result.get("src_version"));
        from.put("src_id", result.get("src_id").toString());
        from.put("src_url", result.get("src_url"));
        return from;
    }

    /**
     * http请求poiexists接口；
     * int，statuscode=0表示poiid存在
     */
    public int isNewPoiid(String poiid) {
        String response = null;
        for (int i = 0; i < 3; i++) {
            try {
                response = HttpclientUtil.post(conf.getString("url.poiexists"),
                        ImmutableMap.of("poiid", poiid), "UTF-8");
                if (response != null) {
                    break;
                }
            } catch (Exception e) {
                logger.info("check poiid exists failed: poiid[{}], Message{}", poiid, e.getMessage());
            }
        }
        JsonNode node = Utils.loadsJson(response);
        return node.get("statuscode").asInt();
    }

    /*默认的验证cp是否有效，子类可能会重写该方法*/
    public boolean isValidCp(String deep) {
        if (deep == null || !deep.contains("status")) {
            return true;
        }
        JsonNode node = Utils.loadsJson(deep);
        if (node != null && node.get("status") != null) {
            JsonNode status = node.get("status");
            return !equal("-1", status.asText());
        }
        return true;
    }
}
