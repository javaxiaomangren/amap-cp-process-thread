package com.amap.cp.utils;

import com.amap.cp.beans.CpInfo;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.*;
import java.util.*;

/**
 * 数据查询和更新
 * Created by yang.hua on 14-1-13.
 */
public class Persistence {
    private Logger logger = LoggerFactory.getLogger(Persistence.class);
    private static Persistence instance = new Persistence();
    private JdbcTemplate template = DBUtils.getInstance().jdbcTemplate();
    private NamedParameterJdbcTemplate namedTemplate = DBUtils.getInstance().namedParameterJdbcTemplate();
    private static String flagName = Utils.COMM_CONF.getString("flagName");

    private Persistence() {
    }

    public static Persistence getInstance() {
        return instance;
    }

    /**
     * 查询poiid列表
     *
     * @param cpName    cp名称
     * @param flagValue flag字段的值
     * @param pageSize  美也显示数量
     * @return poiid列表
     */
    public List<String> getPoiidByFlagAndTime(String cpName, int flagValue, int pageSize, String lastTime) {
        String sql = "SELECT poiid FROM poi_deep " + "USE INDEX(_index_cp_" + flagName + "_) " +
                " WHERE cp=? AND " + flagName + "=? AND updatetime > '" + lastTime +
                "'  LIMIT " + pageSize;
        List<String> rs = template.query(sql, new Object[]{cpName, flagValue}, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("poiid");
            }
        });
        if (logger.isDebugEnabled()) {
            logger.debug("query poiid sql[{}] Parameter:[{},{}]", sql, cpName, flagValue);
        }
        return rs;
    }

    public int countPois(String cpName, int flagValue) {
        String sql = "SELECT count(*) as cnt FROM poi_deep " + "USE INDEX(_index_cp_" + flagName + "_) " +
                " WHERE cp=? AND " + flagName + "=? ";
        return template.queryForObject(sql, new Object[]{cpName, flagValue}, Integer.class);
    }

    public int countCpIds(String cpName, int flagValue) {
        String sql = "SELECT count(*) as cnt FROM poi_rti " +
                " WHERE cp=? AND " + flagName + "=? ";
        return template.queryForObject(sql, new Object[]{cpName, flagValue}, Integer.class);
    }

    public List<String> getPageCpIds(String cpName, int flagValue, int pageNo, int pageSize) {
        String sql = "SELECT id FROM poi_rti " + "USE INDEX(_index_cp_" + flagName + "_) " +
                " WHERE cp=? AND " + flagName + "=? " +
                "  LIMIT ?, ? ";
        List<String> rs = template.query(sql, new Object[]{cpName, flagValue, pageNo, pageSize}, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("id");
            }
        });
        if (logger.isDebugEnabled()) {
            logger.debug("query id sql[{}] Parameter:[{},{}]", sql, cpName, flagValue);
        }
        return rs;
    }

    public List<String> getPagePoiid(String cpName, int flagValue, int pageNo, int pageSize) {
        String sql = "SELECT poiid FROM poi_deep " + "USE INDEX(_index_cp_" + flagName + "_) " +
                " WHERE cp=? AND " + flagName + "=? " +
                "  LIMIT ?, ? ";
        List<String> rs = template.query(sql, new Object[]{cpName, flagValue, pageNo, pageSize}, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("poiid");
            }
        });
        if (logger.isDebugEnabled()) {
            logger.debug("query poiid sql[{}] Parameter:[{},{}]", sql, cpName, flagValue);
        }
        return rs;
    }

    public List<String> getPagePoiidById(String cpName, List<String> ids) {
        String sql = "SELECT poiid FROM poi_deep WHERE cp=:cpName AND id IN(:ids) AND " + flagName + " >= 0";
        List<String> rs = namedTemplate.query(sql, ImmutableMap.of("cpName", cpName, "ids", ids), new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("poiid");
            }
        });
        return rs;
    }


    /**
     * 获取深度表的更新数据
     *
     * @param cpName cp名称
     * @param limit  test_update_flag或者update_flag的值
     * @return 一个多key的Map（一个map支持重复的key）
     */
    public Multimap<String, CpInfo> getUpdatePoiFromDeep(String cpName, int limit, String lastTime) {
        List<String> poiids = getPoiidByFlagAndTime(cpName, 1, limit, lastTime);
        return getCpFromPoiids(cpName, poiids);

    }

    /**
     * 根据oiid查询深度和动态信息
     *
     * @param cpName cp名称
     * @param poiids poiid 列表
     * @return Multimap
     */
    public Multimap<String, CpInfo> getCpFromPoiids(String cpName, List<String> poiids) {
        if (poiids == null || poiids.isEmpty()) {
            return ArrayListMultimap.create();
        }
        String sql = "SELECT deep.cp, deep.poiid, deep.id, deep.deep,deep.updatetime," +
                " deep." + flagName + " as deep_flag, rti." + flagName + " as rti_flag, rti.rti " +
                " FROM poi_deep deep LEFT JOIN poi_rti rti ON (deep.id=rti.id AND deep.cp = rti.cp) " +
                " WHERE deep.cp=:cpName AND deep.poiid IN(:Poiids)";
        List<CpInfo> list = getCpsList(sql, ImmutableMap.<String, Object>of(
                "cpName", cpName,
                "Poiids", poiids));
        return groupByPoiId(list);
    }

    /**
     * 将查询结果通过Multimap分组
     *
     * @param list CpInfo列表
     * @return Multimap
     */
    private Multimap<String, CpInfo> groupByPoiId(List<CpInfo> list) {
        Multimap<String, CpInfo> multiMap = ArrayListMultimap.create();
        for (CpInfo cpInfo : list) {
            multiMap.put(cpInfo.getPoiid(), cpInfo);
        }
        return multiMap;
    }

    /**
     * 查询cp
     *
     * @param sqlTemplate sql模板
     * @param param       sql参数
     * @return List
     */
    public List<CpInfo> getCpsList(String sqlTemplate, Map<String, Object> param) {
        if (logger.isDebugEnabled()) {
            logger.debug("Query cp data by poiids sql:[{}], parameters[{}]", sqlTemplate, param.toString());
        }
        return namedTemplate.query(sqlTemplate, param, new RowMapper<CpInfo>() {
            @Override
            public CpInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                CpInfo cpInfo = new CpInfo();
                String poiid = rs.getString("poiid");
                cpInfo.setCpName(rs.getString("cp"));
                cpInfo.setPoiid(poiid);
                cpInfo.setId(rs.getString("id"));
                cpInfo.setDeep(rs.getString("deep"));
                Timestamp date = rs.getTimestamp("updatetime");
                cpInfo.setUpdateTime(date);
                cpInfo.setDeepFlag(rs.getInt("deep_flag"));
                cpInfo.setRtiFlag(rs.getInt("rti_flag"));
                cpInfo.setRti(rs.getString("rti"));
                return cpInfo;
            }
        });
    }


    public List<CpInfo> getDeepByPoiidsAndCp(String cpName, List<String> poiids) {
        String sql = "SELECT * FROM poi_deep WHERE cp=:cpName AND poiid IN(:Poiids)";
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("Poiids", poiids);
        param.put("cpName", cpName);
        return namedTemplate.query(sql, param, new RowMapper<CpInfo>() {
            @Override
            public CpInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                CpInfo cp = new CpInfo();
                cp.setPoiid(rs.getString("poiid"));
                cp.setId(rs.getString("id"));
                cp.setDeep(rs.getString("deep"));
                return cp;
            }
        });
    }

    /**
     * 执行批量更新
     *
     * @param sql    sql
     * @param params sql参数
     * @return 更新数量
     */
    public int[] executeButchSql(String sql, Map[] params) {
        if (params != null && params.length > 0) {
            return namedTemplate.batchUpdate(sql, params);
        }
        return null;
    }

    public void executeUpdate(List<Map<String, Object>> params) {
        if (params.size() == 0) {
            return;
        }

        String deepSql = "UPDATE poi_deep SET " + flagName + "=:flagValue  WHERE cp=:cpName AND poiid=:poiid";
        String rtiSql = "UPDATE poi_rti SET " + flagName + "=:flagValue  WHERE cp=:cpName AND id=:id";
        if (logger.isDebugEnabled()) {
            logger.debug(deepSql);
            logger.debug(rtiSql);
        }
        StopWatch watch = new Slf4JStopWatch();
        watch.start("executeUpdate", "Execute Batch Update Flag value");
        Map[] deepParam = toSqlArray(params);
        Map[] rtiParam = toRtiParam(params);
        if (deepParam != null && deepParam.length > 0) {
            int[] deepCounts = executeButchSql(deepSql, deepParam);
            int[] rtiCounts = executeButchSql(rtiSql, rtiParam);
            logger.info("Execute Update deep poi counts:{},  rti counts:{}",
                    deepCounts.length, rtiCounts.length);
        }
        watch.stop();
    }

    //TODO 需要优化
    private Map[] toSqlArray(List<Map<String, Object>> params) {
        Collection<Map<String, Object>> c = Collections2.filter(params, new Predicate<Map<String, Object>>() {
            @Override
            public boolean apply(Map<String, Object> input) {
                return input != null && !input.isEmpty();
            }
        });
        return c.toArray(new Map[c.size()]);
    }

    private Map[] toRtiParam(List<Map<String, Object>> params) {
        List<Map<String, Object>> param = Lists.newArrayList();
        for (Map _m : params) {
            if (_m.isEmpty()) {
                continue;
            }
            Collection<String> ids = (Collection<String>) _m.get("ids");
            for (String id : ids) {
                Map<String, Object> p = Maps.newHashMap();
                p.put("flagValue", _m.get("flagValue"));
                p.put("cpName", _m.get("cpName"));
                p.put("id", id);
                param.add(p);
            }
        }
        return param.toArray(new Map[param.size()]);
    }

    public Map<String, String> getNewPoiids(String cpName) {
        String sql = "SELECT id,newpoiid FROM poi_newpoi WHERE cp=:cpName ";
        final Map<String, String> newPois = Maps.newHashMap();
        final String cp = cpName;
        namedTemplate.query(sql, ImmutableMap.of("cpName", cpName), new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                newPois.put(cp + rs.getString("id"), rs.getString("newpoiid"));
                return null;
            }
        });
        return newPois;
    }

    public static void main(String[] args) {
//        Persistence persistence = new Persistence();
//        List<String> rs = persistence.getAllPoiIdByFlag("residential_jiaodian_api", 1);
//        System.out.println(rs.size());
//        Multimap<String, CpInfo> datas = persistence.getCpByPoiid(Collections.singletonList("B000A01CD7"), 1, "chinatelecom_114_info");
//        System.out.println(datas.toString());
//        Multimap<String, CpInfo> datas2 = persistence.getCpByPoiid(Collections.singletonList("B000A7ZLKR"), 1, "residential_jiaodian_api");
//        System.out.println(datas2.toString());
//        DBUtils.getInstance().cleanup();
//        Multimap<String, CpInfo> result = Persistence.getInstance().getUpdatePoiFromDeep("residential_jiaodian_api", 100);
//        System.out.println(result.size());
        //test get update from rti
//        Multimap<String, CpInfo> result = Persistence.getInstance().getUpdatePoiFromDeep("residential_jiaodian_api", 10);
//        System.out.println( result);
        //test to trit
//        Map<String, Object> param = Maps.newHashMap();
//        Map[] x = Persistence.getInstance().toRtiParam(ImmutableList.of(param));
//        Persistence.getInstance().executeUpdate(ImmutableList.of(param));
//        int x = Persistence.getInstance().countPois("residential_jiaodian_api", 1);
//        System.out.println(x);
        List ids = Persistence.getInstance().getPageCpIds("residential_jiaodian_api", 1, 0, 10);
        System.out.println(ids);
        List poids = Persistence.getInstance().getPagePoiidById("residential_jiaodian_api", ids);
        System.out.println(poids);

    }
}
