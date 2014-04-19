package com.amap.cp.save;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.DeepProcess;

import java.util.Collection;
import java.util.Map;

/**
 * 处理deep信息
 * Created by yang.hua on 14-3-25.
 */
public class DeepSave extends AbstractSave {

    @Override
    public Map<String, Object> postInMultiThread(Collection<CpInfo> cps) throws Exception {
        setOrderProcess("deep", new DeepProcess());
        return assembly(cps);
    }

}
