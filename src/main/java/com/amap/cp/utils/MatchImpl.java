package com.amap.cp.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 调用match接口
 * Created by yang.hua on 14-4-8.
 */
public class MatchImpl {
    private static List<String> paramFields = ImmutableList.of("poiid", "name", "new_type", "new_keytype", "x", "y", "code", "checked");
    private static String url = Utils.COMM_CONF.getString("url.merge");
    private static String CODING = "UTF-8";

    public static String getMatchedPoiid(Map base, String srcType, String srcId) {
        String param = getHttpParameter(base, srcType, srcId);
        if (!Strings.isNullOrEmpty(param)) {
            String matchRs = null;
            try {
                matchRs = HttpclientUtil.get(url + URLEncoder.encode(param, CODING));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (!Strings.isNullOrEmpty(matchRs)) {
                String[] rs = matchRs.split(",");
                if (rs.length > 1 && rs[1].length() > 3) {
                    return rs[1];
                }
            }
        }
        return "";
    }

    public static String getHttpParameter(Map baseInfo, String srcType, String srcId) {
        if (hasValidParam(baseInfo, paramFields)) {
            StringBuffer sb = new StringBuffer("?method=merge");
            String name = (String) baseInfo.get("name");
            Map admin = (Map) baseInfo.get("admin");
            String province = (String) admin.get("adm1_chn");
            String city = (String) admin.get("adm8_chn");
            String district = (String) admin.get("adm9_chn");
            sb.append("&source=").append(srcType).append("&poiid=").append(srcId)
                    .append("&x=").append(baseInfo.get("x")).append("&y=").append(baseInfo.get("y"))
                    .append("&name=").append(name).append("&addr=").append(baseInfo.get("address"))
                    .append("&tel=").append(baseInfo.get("telephone")).append("&type=")
                    .append(baseInfo.get("new_type")).append("&code=").append(baseInfo.get("code"))
                    .append("&province=").append(province).append("&city=").append(city).append("&district=")
                    .append(district);
            try {
                return URLEncoder.encode(sb.toString(), CODING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean hasValidParam(Map base, List<String> notNullFields) {
        for (String notNullField : notNullFields) {
            String baseField = (String) base.get(notNullField);
            boolean invalidParam = Strings.isNullOrEmpty(baseField) || " ".equals(baseField.trim());
            if (invalidParam) {
                return false;
            }
            return base.get("new_type").toString().length() == 6;
        }
        return true;
    }


}
