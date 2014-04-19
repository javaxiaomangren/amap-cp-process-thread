package com.amap.cp.save.impl;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.*;
import com.amap.cp.process.impl.DiningDianPingRtiProcess;
import com.amap.cp.process.impl.DiningDianPingSpecProcess;
import com.amap.cp.save.AbstractSave;

import java.util.Collection;
import java.util.Map;

/**
 *
 * Created by yang.hua on 14-4-3.
 */
public class DiningDianPingSave extends AbstractSave {
    @Override
    public Map<String, Object> postInMultiThread(Collection<CpInfo> cps) throws Exception {
        setOrderProcess("base", new BaseProcess());
        setOrderProcess("deep", new DeepProcess());
        setOrderProcess("spec", new DiningDianPingSpecProcess());
        setOrderProcess("rti", new DiningDianPingRtiProcess());
        return assembly(cps);
    }
}
