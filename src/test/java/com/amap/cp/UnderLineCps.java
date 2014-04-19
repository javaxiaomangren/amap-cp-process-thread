package com.amap.cp;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.utils.Persistence;
import com.amap.cp.utils.DBUtils;
import com.amap.cp.utils.HttpclientUtil;
import com.amap.cp.utils.Utils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yang.hua on 14-1-16.
 */
public class UnderLineCps {

    public static void main(String[] args) throws IOException {
        List<String> poiids = Files.readLines(new File("E:\\cinema_0116_下线数据_lines"),  Charsets.UTF_8);
        System.out.println(poiids.size());
       new UnderLineCps().underLineCps(poiids, "cms_cinema_merge");
    }

    public  void underLineCps(List<String> poiids, String cpName) throws IOException {
        Persistence persistence = Persistence.getInstance();
        List<CpInfo> cps = persistence.getDeepByPoiidsAndCp(cpName, poiids);
        Map[] params = new Map[cps.size()];
        List<Map<String, String>> httpParams = new ArrayList<Map<String, String>>();
        for (int i = 0; i < cps.size(); i++) {
            CpInfo cp = cps.get(i);
            String deep = cp.getDeep();
            Map mp = Utils.loadsJson(deep, Map.class);
            mp.put("status", "-1");
            Map<String, Object> batchUpdate = new HashMap<String, Object>();
            batchUpdate.put("id", cp.getId());
            batchUpdate.put("poiid", cp.getPoiid());
            batchUpdate.put("deep", Utils.dumpObjectAsString(mp));
            params[i] = batchUpdate;
            Map<String, String> urlMap = new HashMap<String, String>();
            urlMap.put("flag", "deep");
            urlMap.put("cp", cpName);
            urlMap.put("cpid", cp.getId());
            urlMap.put("poiid", cp.getPoiid());
            urlMap.put("deep", deep);
            httpParams.add(urlMap);
        }
        String sql = "UPDATE poi_deep SET deep=:deep  WHERE id=:id AND poiid=:poiid";
        int[] ints = DBUtils.getInstance().namedParameterJdbcTemplate().batchUpdate(sql, params);
        String url = "http://10.2.134.64:8085/saveDeepRti/SaveDeepRti";
        List<String> failed = new ArrayList<String>();
        for (Map<String, String> httpParam : httpParams) {
            String result = null;
            try {
                result = HttpclientUtil.post(url, httpParam, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!"success".equals(result)) {
                failed.add(httpParam.get("poiid"));
            }
        }

        System.out.println("数据库更新失败的有");
        System.out.println(cps.size() - ints.length);
        System.out.println("http请求失败的有");
        System.out.println(failed);
    }
}
