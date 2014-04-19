package com.amap.cp.utils;

import com.amap.cp.process.PicProcess;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.equal;

/**
 * 工具类
 * Created by yang.hua on 14-1-10.
 */
public class Utils {
    public static ObjectMapper OM = new ObjectMapper();

    public static PropertiesConfiguration COMM_CONF = getConf("conf/config.properties");
    public static PropertiesConfiguration RULE_CONF = getConf("conf/post_data_rules.properties");
    private static PicProcess picProcess = new PicProcess();

    public static PropertiesConfiguration getConf(String confName) {
        try {
            return new PropertiesConfiguration(confName);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonNode loadsJson(String json) {
        try {
            return OM.getJsonFactory().createJsonParser(json).readValueAsTree();
        } catch (IOException e) {
            SimpleMailSender.notify(e.getMessage(), json + getStackTrace(e.getStackTrace()));
        }
        return null;
    }

    public static <T> T loadsJson(String content, Class<T> clazz) {
        try {
            return OM.getJsonFactory().createJsonParser(content).readValueAs(clazz);
        } catch (IOException e) {
            SimpleMailSender.notify(e.getMessage(), content + getStackTrace(e.getStackTrace()));
        }
        return null;
    }

    public static String dumpObjectAsString(Object obj) {
        try {
            return OM.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 递归地迭代json，将所有key转换为小写
     * 遇到key=pic_info，且类型为list，转换图片
     *
     * @param m map
     * @return Map
     */
    public static Map key2Lower(Map m) {
        Map<String, Object> mp = Maps.newLinkedHashMap();
        for (Object o : m.keySet()) {
            String key = (String) o;
            String lower = key.toLowerCase().replace(".", "_");
            Object value = m.get(key);
            if (value == null) {
                mp.put(lower, null);
            } else if (value instanceof Map) {
                mp.put(lower, key2Lower((Map) value));
            } else if (value instanceof List) {
                List ls = key2Lower4List((List) value);
                if ("pic_info".equals(lower) && !"true".equals(COMM_CONF.getString("disableImage"))) {
                    StopWatch watch = new Slf4JStopWatch();
                    watch.start("getPicInfo", "Get pic url");
                    List newList = picProcess.getPicInfo(ls);
                    watch.stop();
                    mp.put(lower, newList);
                } else {
                    mp.put(lower, ls);
                }
            } else {
                mp.put(lower, value);
            }
        }
        return mp;
    }


    public static List key2Lower4List(List value) {
        List<Object> ls = Lists.newArrayList();
        for (Object o : value) {
            if (o instanceof Map) {
                ls.add(key2Lower((Map) o));
            } else if (o instanceof List) {
                ls.add(key2Lower4List((List) o));
            } else {
                ls.add(o);
            }
        }
        return ls;
    }

    public static String getDateStr(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fmt.format(date);
    }

    public static Date getDate(String a) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return fmt.parse(a);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStackTrace(StackTraceElement[] stackTraceElement) {
        StringBuilder sb = new StringBuilder("\n");
        for (StackTraceElement traceElement : stackTraceElement) {
            sb.append(traceElement.toString()).append("\n");
        }
        return sb.toString();
    }

    public static String printConf(PropertiesConfiguration conf) {
        Iterator<String> it = conf.getKeys();
        StringBuilder sb = new StringBuilder("[");
        while (it.hasNext()) {
            String key = it.next();
            String value = conf.getString(key);
            sb.append(key).append("=").append(value).append(", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]");

        return sb.toString();
    }

    public static <T> List<T> duplicateListElment(List<T> src) {
        List<T> list = Lists.newArrayList();
        PeekingIterator<T> iterator = Iterators.peekingIterator(src.iterator());
        while (iterator.hasNext()) {
            T current = iterator.next();
            while (iterator.hasNext() && equal(iterator.peek(), current)) {
                //skip the duplicate element
                iterator.next();
            }
            list.add(current);
        }
        return list;
    }

    public static void main(String[] args) throws URISyntaxException {
        PropertiesConfiguration conf = getConf("conf/config.properties");
//        System.out.println(conf.getString("url.poiexists"));
//        System.out.println(conf.getList("validCpNames"));
//        System.out.println("test for empty value");
//        JsonNode empty = loadsJson("");
//        System.out.println(empty);
//        JsonNode n = loadsJson("[{\"market\": \"groupbuy\", \"groupbuy_num\": 0, \"groupbuy_list\": []}, {\"market\": \"review\", \"review_num\": \"22\", \"review_list\": [{\"author\": \"君香食府\", \"review\": \"去过 很多次,让人流连忘返、小桥流水，鸟语花香，天然氧吧、       城市的净地      菜品丰富，口味适中，朋友鼎力推荐\", \"recommend\": null, \"time\": \"2013-09-05 13:31:00\", \"score\": \"5.0\", \"pic_info\": null, \"tag_review\": null, \"review_wapurl\": \"http://m.dianping.com/shop/review/46173465\", \"review_appurl\": {\"android\": null, \"ios\": null}, \"review_weburl\": \"http://www.dianping.com/review/46173465\"}, {\"author\": \"yuhanhelen\", \"review\": \"环境巴适，我和老公说，我要住在这种地方就巴适了，在古代，大户人家才住的起哦。茶水很便宜，10元一杯，按人头点，一茶一坐。那天热，大家都要吃素，结果9个人才吃了280，因为他们把座位安排错了，又免费让我...\", \"recommend\": null, \"time\": \"2013-07-30 15:00:00\", \"score\": \"5.0\", \"pic_info\": null, \"tag_review\": null, \"review_wapurl\": \"http://m.dianping.com/shop/review/44710788\", \"review_appurl\": {\"android\": null, \"ios\": null}, \"review_weburl\": \"http://www.dianping.com/review/44710788\"}, {\"author\": \"Xiao懒娃娃\", \"review\": \"以前单位团年在这吃过一次，那次就觉得这勒环境很舒服！ 这次朋友结婚又在这举行，带起儿子来洗洗肺。湖里很多锦鲤，而且都是硕大一条条勒。儿子喂鱼食给它们抢得超级厉害，逗得我儿子笑安逸落勒。\", \"recommend\": null, \"time\": \"2013-05-15 22:05:00\", \"score\": \"5.0\", \"pic_info\": null, \"tag_review\": null, \"review_wapurl\": \"http://m.dianping.com/shop/review/42050577\", \"review_appurl\": {\"android\": null, \"ios\": null}, \"review_weburl\": \"http://www.dianping.com/review/42050577\"}, {\"author\": \"花姑娘YY\", \"review\": \"这是一家农家乐，是一家看着挺大的农家乐，也是一家挺上档次的农家乐，这里的环境真不错的，小桥流水，绿树成荫，空气也觉得挺清新的，也挺安静的一个地方，这里的服务觉的挺周到的，要是服务员再热情一点就更好了，...\", \"recommend\": null, \"time\": \"2013-04-26 10:51:00\", \"score\": \"4.0\", \"pic_info\": null, \"tag_review\": null, \"review_wapurl\": \"http://m.dianping.com/shop/review/41348765\", \"review_appurl\": {\"android\": null, \"ios\": null}, \"review_weburl\": \"http://www.dianping.com/review/41348765\"}, {\"author\": \"WuXiaoFei18\", \"review\": \"很不错的地方，就是不好找，又在修路，麻烦得很。不过里面环境确实一流，服务员说是五星级，难道农家乐也评星级？别人宴请哈，不敢敞开吃\", \"recommend\": null, \"time\": \"2013-04-13 17:15:00\", \"score\": \"5.0\", \"pic_info\": null, \"tag_review\": null, \"review_wapurl\": \"http://m.dianping.com/shop/review/40774929\", \"review_appurl\": {\"android\": null, \"ios\": null}, \"review_weburl\": \"http://www.dianping.com/review/40774929\"}], \"review_all_wapurl\": \"http://m.dianping.com/shop/4507574/review_all\", \"review_all_appurl\": {\"android\": \"dianping://review?id=4507574\", \"ios\": \"dianping://review?id=4507574\"}, \"review_all_weburl\": \"http://www.dianping.com/shop/4507574/review_all\"}]");
//        System.out.println(n.size());
//        System.out.println("test get null value");
////        System.out.println(n.get("recommend").toString());
//        System.out.println(getDateStr(new Date()));
//        PropertiesConfiguration conf1 = getConf("conf/post_data_rules.properties");
//        System.out.println(conf1.getString("residential_jiaodian_api_deep_field"));
        System.out.println(printConf(conf));
    }
}
