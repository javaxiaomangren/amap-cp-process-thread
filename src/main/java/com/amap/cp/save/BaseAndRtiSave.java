package com.amap.cp.save;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.process.BaseProcess;
import com.amap.cp.process.RtiProcess;
import com.amap.cp.utils.Utils;
import org.codehaus.jackson.JsonNode;

import javax.rmi.CORBA.Util;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.hua on 14-2-8.
 * 通用的推送基础信息和动态信息
 */

public class BaseAndRtiSave extends AbstractSave {
    @Override
    public Map<String, Object> postInMultiThread(Collection<CpInfo> cps) throws Exception {
        setOrderProcess("base", new BaseProcess());
        setOrderProcess("rti", new RtiProcess());
        return assembly(cps);
    }

}
