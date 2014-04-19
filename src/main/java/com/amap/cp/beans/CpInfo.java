package com.amap.cp.beans;

import java.util.Date;

/**
 * 待处理cp的信息
 * Created by yang.hua on 14-1-15.
 */
public class CpInfo {
    private String cpName;
    private String poiid;
    private String id;
    private String deep;
    private Date updateTime;
    private String rti;
    private int deepFlag;
    private int rtiFlag;

    public String getCpName() {
        return cpName;
    }

    public void setCpName(String cpName) {
        this.cpName = cpName;
    }

    public String getPoiid() {
        return poiid;
    }

    public void setPoiid(String poiid) {
        this.poiid = poiid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeep() {
        return deep;
    }

    public void setDeep(String deep) {
        this.deep = deep;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRti() {
        return rti;
    }

    public void setRti(String rti) {
        this.rti = rti;
    }

    public int getDeepFlag() {
        return deepFlag;
    }

    public void setDeepFlag(int deepFlag) {
        this.deepFlag = deepFlag;
    }

    public int getRtiFlag() {
        return rtiFlag;
    }

    public void setRtiFlag(int rtiFlag) {
        this.rtiFlag = rtiFlag;
    }

    @Override
    public String toString() {
        return "CpInfo{" +
                "cpName='" + cpName + '\'' +
                ", poiid='" + poiid + '\'' +
                ", id='" + id + '\'' +
                ", deep='" + deep + '\'' +
                ", updateTime=" + updateTime +
                ", rti='" + rti + '\'' +
                ", deepFlag=" + deepFlag +
                ", rtiFlag=" + rtiFlag +
                '}';
    }
}
