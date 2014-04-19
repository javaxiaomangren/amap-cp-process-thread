package com.amap.cp.save.impl;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.BaseProcess;
import com.amap.cp.process.DeepProcess;
import com.amap.cp.process.PicProcess;
import com.amap.cp.process.RtiProcess;
import com.amap.cp.save.AbstractSave;
import com.amap.cp.utils.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 焦点房产数据处理
 * Created by yang.hua on 14-2-20.
 */
public class JiaoDianSave extends AbstractSave {

    /**
     * 焦点房产数据，户型图片特殊处理
     * 转换为高德地图的图片url
     */
    @Override
    protected Map<String, Object> reProcessAssembled(Map<String, Object> resultObj) {
        if (!"true".equals(conf.getString("disableImage"))) {
            try {
                Object pic = ((Map) resultObj.get("deep")).get("hxpic_info");
                if (pic != null) {
                    List<Map> params = getPicParam(pic);
                    if (params != null && params.size() > 0) {
                        Map<String, Map> result = new PicProcess().getAsMap(params);
                        if (result != null && result.size() > 0) {
                            setNewPic(pic, result);
                        }
                    }
                }
            } catch (Exception e) {
                logger.info("failed to transform hx pic: {}", Utils.getStackTrace(e.getStackTrace()));
            }
        }
        return resultObj;
    }

    private List<Map> getPicParam(Object picObj) {
        List<Map> param = Lists.newArrayList();
        for (Object o : (List) picObj) {
            Object detail = ((Map) o).get("detail");
            for (Object hx_pic : (List) detail) {
                Map m = (Map) ((Map) hx_pic).get("pic_info");
                param.add(ImmutableMap.of("url", m.get("url")));
            }
        }
        return param;
    }


    private void setNewPic(Object picObj, Map<String, Map> result) {
        List pic = (List) picObj;
        for (int i = 0; i < pic.size(); i++) {
            Object detail = ((Map) pic.get(i)).get("detail");
            for (Object hx_pic : (List) detail) {
                Map oldM = (Map) ((Map) hx_pic).get("pic_info");
                Map m = result.get(oldM.get("url"));
                if ("1".equals(m.get("state").toString())) {
                    String pic_id = (String) m.get("md5");
                    oldM.put("url", conf.getString("imageUrlStuff") + pic_id);
                    oldM.put("fetch_type", "1");
                    oldM.put("pic_id", pic_id);

                }
            }
        }
    }

    @Override
    public Map<String, Object> postInMultiThread(Collection<CpInfo> cps) throws Exception {
        setOrderProcess("base", new BaseProcess());
        setOrderProcess("deep", new DeepProcess());
        setOrderProcess("rti", new RtiProcess());
        return assembly(cps);
    }

}
