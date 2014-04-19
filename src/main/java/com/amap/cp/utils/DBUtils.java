package com.amap.cp.utils;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.PooledDataSource;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据连接封装
 * Created by yang.hua on 14-1-12.
 */
public class DBUtils{
    private static DBUtils instance = new DBUtils();
    private PropertiesConfiguration conf = Utils.getConf("conf/db.properties");
    private DataSource dataSource ;

    public static DBUtils getInstance() {
        return instance;
    }

    private DBUtils() {
        ComboPooledDataSource cpds = null;
        try {
            cpds = new ComboPooledDataSource();
            cpds.setDriverClass(conf.getString("jdbc.driver"));
            cpds.setJdbcUrl(conf.getString("jdbc.url"));
            cpds.setUser(conf.getString("jdbc.username"));
            cpds.setPassword(conf.getString("jdbc.password"));
            cpds.setMinPoolSize(conf.getInt("c3p0.MinPoolSize"));
            cpds.setAcquireIncrement(conf.getInt("c3p0.AcquireIncrement"));
            cpds.setMaxPoolSize(conf.getInt("c3p0.MaxPoolSize"));
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
       this.dataSource = cpds;
    }

    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource) ;
    }

    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 释放连接资源
     */
    public void cleanup(){
        // make sure it's a c3p0 PooledDataSource
        if ( dataSource instanceof PooledDataSource){
            PooledDataSource pds = (PooledDataSource) dataSource;
            try {
                pds.close();
                System.out.println("clean up dataSource");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else
            System.err.println("Not a c3p0 PooledDataSource!");
    }

    public static void main(String[] args) {
        List<Map<String,Object>> maps = DBUtils.getInstance().jdbcTemplate().queryForList("select * from poi_deep where 1>2 limit 10");
        System.out.println(maps.size());
        DBUtils.getInstance().cleanup();
    }
}
