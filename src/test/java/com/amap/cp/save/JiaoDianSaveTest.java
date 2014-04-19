package com.amap.cp.save;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.save.impl.JiaoDianSave;
import com.amap.cp.utils.Persistence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.hua on 14-2-25.
 */
public class JiaoDianSaveTest extends TestCase {
    public void testPostInMultiThread() throws Exception {
        Persistence p = Persistence.getInstance();
        Collection<Collection<CpInfo>> cc = p.getCpFromPoiids("residential_jiaodian_api",
                ImmutableList.of("B000A8VX71")).asMap().values();;
        List<Map<String, Object>> params = Lists.newArrayList();
        for (Collection<CpInfo> cpInfos : cc) {
            Map<String, Object> param = new JiaoDianSave().postInMultiThread(cpInfos);
            if (param != null) {
                params.add(param);
            }
        }
    }
}
