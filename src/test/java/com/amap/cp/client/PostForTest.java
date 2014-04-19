package com.amap.cp.client;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.save.AbstractSave;
import com.amap.cp.save.DeepSave;
import com.amap.cp.save.impl.DiningDianPingSave;
import com.amap.cp.save.impl.JiaoDianSave;
import com.amap.cp.utils.Persistence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by yang.hua on 14-2-10.
 */
public class PostForTest {
    public static void main(String[] args) throws Exception {
        //全国医院大全, hospital_qgyy
//        doPost("hospital_39jk", Persistence.getInstance(), null, new DeepSave());
        doPost("hospital_qgyy", 1,  Persistence.getInstance(), ImmutableList.of("B02271AO0E","B023E1007V","B0FFF2RTH5","B0FFF2RTH6","B001B1CDOD","B0FFF2RTH7","B03180SU2K","B0FFF2I0WN","B02F70P8GQ","B01A014OP1"), new DeepSave());
//        doPost(" hospital_guahao_api", Persistence.getInstance(), null, new DeepSave());

//        doPost("residential_jiaodian_api", 0, Persistence.getInstance(), ImmutableList.of("B000A8VX71"), new JiaoDianSave());
//        doPost("gaode_qunar", Persistence.getInstance(), null, new DeepSave());
//        doPost("dining_dianping_api", 0, Persistence.getInstance(), ImmutableList.of("B00141VSYT"), new DiningDianPingSave());
    }

    private static void doPost(String cpName, int flagValue, Persistence p, List<String> poiids, AbstractSave handler) throws Exception {
        if (poiids == null) {
            poiids = p.getPagePoiid(cpName, flagValue, 0, 30);
        }
        List<Map<String, Object>> param = commPost(cpName, poiids, handler, p);
        p.executeUpdate(param);
    }

    public static List<Map<String, Object>> commPost(String cpName, List<String> poiids, AbstractSave instance, Persistence p) throws Exception {
        Collection<Collection<CpInfo>> cc = p.getCpFromPoiids(cpName, poiids).asMap().values();
        List<Map<String, Object>> params = Lists.newArrayList();
        for (Collection<CpInfo> cpInfos : cc) {
            Map<String, Object> param = instance.postInMultiThread(cpInfos);
            if (param != null) {
                params.add(param);
            }
        }
        return params;
    }

}
