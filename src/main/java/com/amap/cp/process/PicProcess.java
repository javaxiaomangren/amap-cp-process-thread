package com.amap.cp.process;

import com.amap.cp.utils.HttpclientUtil;
import com.amap.cp.utils.Utils;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 图片处理，转换现在的图片url为高德的图片url
 * Created by yang.hua on 14-1-13.
 */
public class PicProcess {
    private final Logger logger = LoggerFactory.getLogger(PicProcess.class);
    private static final PropertiesConfiguration conf = Utils.COMM_CONF;
    private static final String imageService1 = conf.getString("url.image1");
    private static final String imageService2 = conf.getString("url.image2");

    public List<Object> getPicInfo(List<Object> picInfo) {
        List<Map> param = Lists.newArrayList();
        for (int i = 0; i < picInfo.size(); i++) {
            Object url = getUrlParam(picInfo.get(i));
            if (url != null) {
                param.add(ImmutableMap.of("url", url));
            }
        }
        Map<String, Map> resultMap = getAsMap(param);

        if (resultMap != null && !resultMap.isEmpty()) {
            for (int i = 0; i < picInfo.size(); i++) {
                Map old = (Map) picInfo.get(i);
                Map news = resultMap.get(old.get("url"));
                if (Objects.equal("1", news.get("state").toString())) {
                    String pic_id = (String) news.get("md5");
                    old.put("url", conf.getString("imageUrlStuff") + pic_id);
                    old.put("fetch_type", "1");
                    old.put("pic_id", pic_id);
                }
            }
        }
        return picInfo;
    }

    public Map<String, Map> getAsMap(List<Map> param) {
        Map<String, Map> resultMap = Maps.newHashMap();
        String paramStr = (Utils.dumpObjectAsString(param));
        String result = getPicString(imageService1, paramStr);
        if (result == null || !result.contains("urldata")) {
            result = getPicString(imageService2, paramStr);
            if (result == null || !result.contains("urldata")) {
                logger.info("Get image failed");
                return resultMap;
            }
        }
        if (result.contains("1}") || result.contains("1 }") || result.contains("1")) {
            List<Map> ls = (List<Map>) Utils.loadsJson(result, Map.class).get("urldata");
            for (Map m : ls) {
                resultMap.put((String) m.get("url"), m);
            }
        }
        return resultMap;
    }


    public Map getGaoDePic(Map url) {
        String param = Utils.dumpObjectAsString(ImmutableList.of(url));
        String result = getPicString(imageService1, param);
        if (result == null || !checkedResult(result)) {
            result = getPicString(imageService2, param);
            if (result == null || !checkedResult(result)) {
                return url;
            }
        }
        List l = (List) Utils.loadsJson(result, Map.class).get("urldata");
        for (Object o : l) {
            Map m = (Map) o;
            if (Objects.equal("1", m.get("state").toString())) {
                Map<String, Object> resultMap = Maps.newLinkedHashMap();
                String pic_id = (String) m.get("md5");
                resultMap.put("url", conf.getString("imageUrlStuff") + pic_id);
                resultMap.put("fetch_type", "1");
                resultMap.put("pic_id", pic_id);
                return resultMap;
            }
        }

        return url;
    }

    private boolean checkedResult(String result) {
        return result.contains("urldata")
                && (result.contains("1}") || result.contains("1 }") || result.contains("1"));
//        JsonNode resultNode = Utils.loadsJson(result);
//        return resultNode != null && resultNode.get("urldata") != null;
    }

    public String getPicString(String server, String picInfoJson) {
        try {
            StopWatch watch = new Slf4JStopWatch();
            watch.start("getPicString");
            String sr = HttpclientUtil.post(server, ImmutableMap.of("data", picInfoJson), "utf-8");
            watch.stop();
            return sr;
        } catch (Exception e) {
//            logger.info("Get pic at {}, Exception is[{}], 参数[{}]", imageService, e.getMessage(), picInfoJson);
            logger.info("Get pic at {}, Exception is[{}]", server, e.getMessage());
        }
        return null;
    }

    private Object getUrlParam(Object p) {
        if (p instanceof Map) {
            Object url = ((Map) p).get("url");
            return url;
        }
        return null;
    }
}
