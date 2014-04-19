package com.amap.cp.save;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.BaseProcess;
import com.amap.cp.process.DeepProcess;
import com.amap.cp.process.RtiProcess;

import java.util.Collection;
import java.util.Map;

/**
 * Created by yang.hua on 14-2-8.
 */
public class BaseDeepRtiSave extends AbstractSave {
    @Override
    public Map<String, Object> postInMultiThread(Collection<CpInfo> cps) throws Exception {
        setOrderProcess("base", new BaseProcess());
        setOrderProcess("deep", new DeepProcess());
        setOrderProcess("rti", new RtiProcess());
        return assembly(cps);
    }

}
