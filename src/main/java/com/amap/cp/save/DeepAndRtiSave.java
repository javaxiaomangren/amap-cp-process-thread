package com.amap.cp.save;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.DeepProcess;
import com.amap.cp.process.RtiProcess;
import com.amap.cp.utils.Persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.hua on 14-1-21.
 */
public class DeepAndRtiSave extends AbstractSave {

    @Override
    public Map<String, Object> postInMultiThread(Collection<CpInfo> cps) throws Exception {
        setOrderProcess("deep", new DeepProcess());
        setOrderProcess("rti", new RtiProcess());
        return assembly(cps);
    }

}
