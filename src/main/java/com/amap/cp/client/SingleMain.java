package com.amap.cp.client;

import com.amap.cp.beans.CpInfo;
import com.amap.cp.save.AbstractSave;
import com.amap.cp.save.DeepSave;
import com.amap.cp.utils.Persistence;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** *
 * Created by yang.hua on 14-1-15.
 */
public class SingleMain {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(SingleMain.class);
    Persistence p = Persistence.getInstance();
    String cpName = "hospital_qgyy";
    AbstractSave processor = new DeepSave();
    List<Map<String, Object>> params = Lists.newArrayList();
    int retry = 0;
    int page = 100;

    public List<String> initPoiids(int counts) {
        List<String> poiids = getPoiidFromDeep(counts);
        if (poiids.size() == 0) {
            poiids = getPoiidFromRti();
        }
        return poiids;
    }

    public List<String> getPoiidFromDeep(int poiCounts) {
        List<String> _poiids = Lists.newArrayList();
        int _page = 1000;
        if (poiCounts < 1000) {
            _page = poiCounts;
        }
        int page = (poiCounts - 1) / _page + 1;
        for (int i = 0; i < page; i++) {
            List<String> poiid = p.getPagePoiid(cpName, 1, i, _page);
            _poiids.addAll(poiid);
        }
        logger.info("Initialize cp {} poiid counts={}", cpName, poiCounts);
        return _poiids;
    }

    public List<String> getPoiidFromRti() {
        List<String> _poiids = Lists.newArrayList();
        int rtiCnts = p.countCpIds(cpName, 1);
        int page = (rtiCnts - 1) / 1000 + 1;
        for (int i = 0; i < page; i++) {
            List<String> cpId = p.getPageCpIds(cpName, 1, i, 1000);
            List<String> temp = p.getPagePoiidById(cpName, cpId);
            _poiids.addAll(temp);
        }
        return _poiids;
    }

    public void run() throws Exception {
        int counts = p.countPois(cpName, 1);
        List<String> poiids = initPoiids(counts);
        while (retry < 10) {
            if (poiids.size() > 0) {
                if (poiids.size() > 100){
                    int pp = (poiids.size() - 1) / 100 + 1;
                    for (int x = 0; x <pp; x++) {
                        int y =  (x + 1) * page;
                        if (y > poiids.size()) {
                            y = poiids.size();
                        }
                        List<String> paramIds = poiids.subList(x * page, y);
                        _processByIds(processor, paramIds);
                    }
                } else {
                    _processByIds(processor, poiids);
                }
                poiids.clear();
            } else {
                poiids = initPoiids(p.countPois(cpName, 1));
                retry += 1;
                page = 100;
            }
        }
        p.executeUpdate(params);

    }

    private void _processByIds(AbstractSave processor, List<String> paramIds) throws Exception {
        Multimap<String, CpInfo> mp = p.getCpFromPoiids(cpName, paramIds);
        for (Collection<CpInfo> cpInfo : mp.asMap().values()) {
            Map<String, Object> r = processor.postInMultiThread(cpInfo);
            if (r != null) {
                params.add(r);
            }
        }

        if (params.size() > 500) {
            p.executeUpdate(params);
            params.clear();
        }
    }

    public static void main(String[] args) throws Exception {
        new SingleMain().run();
        System.out.println(new Date().toString());
    }
}
