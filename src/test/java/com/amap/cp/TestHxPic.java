package com.amap.cp;


import com.amap.cp.process.PicProcess;
import com.amap.cp.utils.HttpclientUtil;
import com.amap.cp.utils.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.hua on 14-1-10.
 */
public class TestHxPic {
    public static void main(String[] args) throws Exception {
        String hx = "{\"hxpic_info\":[{\"JUSHI\":null,\"HX_AREA\":\"\",\"detail\":[{\"REMARKS\":null,\"ISCOVER\":\"0\",\"url\":\"http://imgs.focus.cn/upload/bt/6491/a_64904883.jpg\",\"TITLE\":\"1\",\"SRC_TYPE\":\"residential_jiaodian_api\",\"PIC_LAYOUT\":null},{\"REMARKS\":null,\"ISCOVER\":\"0\",\"url\":\"http://i1.f.itc.cn/upload/bt/6491/b_64904954.jpg\",\"TITLE\":\"3\",\"SRC_TYPE\":\"residential_jiaodian_api\",\"PIC_LAYOUT\":null},{\"REMARKS\":null,\"ISCOVER\":\"0\",\"url\":\"http://i1.f.itc.cn/upload/bt/6491/a_64904913.jpg\",\"TITLE\":\"2\",\"SRC_TYPE\":\"residential_jiaodian_api\",\"PIC_LAYOUT\":null}]}]}";
        Map resultObj = Utils.loadsJson(hx, Map.class);
        Object pic = resultObj.get("hxpic_info");
        if (pic != null && pic instanceof List) {
            for (Object o : (List) pic) {
                if (o != null && o instanceof Map) {
                    Object detail = ((Map) o).get("detail");
                    if (detail != null && detail instanceof List) {
                        for (Object hx_pic : (List) detail) {
                            if (hx_pic != null && hx_pic instanceof Map) {
                                Map m = ((Map) hx_pic);
                                if (m.get("url") != null) {
                                    Map newM = new PicProcess().getGaoDePic(ImmutableMap.of("url", m.get("url")));
                                    m.putAll(newM);
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println(resultObj);
    }

}
