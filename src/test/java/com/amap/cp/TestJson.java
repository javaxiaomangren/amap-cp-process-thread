package com.amap.cp;

import com.amap.cp.process.DeepProcess;
import com.amap.cp.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.hua on 14-1-13.
 */
public class TestJson {
    public static void main(String[] args) throws IOException {
        String jsonStr = "{\"BUSINESS\":\"residential\",\"SRC_TYPE\":\"residential_jiaodian_api\",\"PROPERTY_TYPE\":\"普通住宅 \",\"PRICE\":\"12100\",\"SERVICE_PARKING\":\"2:1\",\"PROPERTY_COMPANY\":\"北京玉海辉物业管理公司\",\"PROPERTY_FEE\":\"2.36元/平方米/月\",\"AREA_TOTAL\":\"22.00万平方米\",\"GREEN_RATE\":\"31.60％\",\"VOLUME_RATE\":\"2.460\",\"DEVELOPER\":\"北京华泰方圆房地产开发有限公司\",\"GROUP.SRC_TYPE\":\"\",\"SALES_ADDR\":null,\"SALES_STATUS\":\"05\",\"HOUSE_AREA\":null,\"PRICE_INFO\":\" \",\"OPENING_DATA\":\"二期4栋11层板楼预计2007年开盘销售<br>一期4号楼2004年9月开盘\",\"CHECKIN_DATA\":\"2004年11月<br>现房\",\"SALES_LICE\":null,\"LAND_YEAR\":null,\"WATERSUPPLY_SYST\":null,\"HEATING\":\"小区集中采暖\",\"FLOOR_AREA\":\"22.00万平方米\",\"RENOVATION\":null,\"BUILDING_TYPES\":\"板楼 \",\"PERIPHERAL_SUPP\":\"中、小学： 远大中心小学 幼儿园： 四季青幼儿园 商场： 金四季超市 银行： 工行、农行 医院： 海、空军总医院 \\r\\n\",\"400TELE_READ\":\"4001234120转0\",\"400TELE\":\"4009001234\",\"PIC_INFO\":[{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1248.jpg\",\"TITLE\":\"底商\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/images/phpzAvkbS0.jpg\",\"TITLE\":\"金雅园外观图\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/images/phppaXKV711.jpg\",\"TITLE\":\"金雅园交通图\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/s3334.jpg\",\"TITLE\":\"4号楼实景图\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/s3335.jpg\",\"TITLE\":\"4号楼实景图\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1242.jpg\",\"TITLE\":\"周边配套\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1243.jpg\",\"TITLE\":\"小区周边配套之餐饮\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1245.jpg\",\"TITLE\":\"售楼处\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1246.jpg\",\"TITLE\":\"商业街\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1244.jpg\",\"TITLE\":\"苏宁电器\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1247.jpg\",\"TITLE\":\"购物中心\",\"SRC_TYPE\":\"residential_jiaodian_api\"},{\"REMARKS\":null,\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/f1249.jpg\",\"TITLE\":\"爱家家具\",\"SRC_TYPE\":\"residential_jiaodian_api\"}],\"HXPIC_INFO\":[{\"JUSHI\":\"2\",\"HX_AREA\":\"2\",\"DETAIL\":{\"REMARKS\":\"二室二厅\",\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/phpnvXYiD1.jpg\",\"TITLE\":\"二居\",\"SRC_TYPE\":\"residential_jiaodian_api\",\"PIC_LAYOUT\":\"2室2厅2卫\",\"LAYOUT_AREA\":\"100平米\",\"INDOOR_AREA\":\"100平米\"}},{\"JUSHI\":\"3\",\"HX_AREA\":\"3\",\"DETAIL\":{\"REMARKS\":\"三室二厅\",\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/phpWx5pr82.jpg\",\"TITLE\":\"三居\",\"SRC_TYPE\":\"residential_jiaodian_api\",\"PIC_LAYOUT\":\"3室2厅1卫\",\"LAYOUT_AREA\":\"150平米\",\"INDOOR_AREA\":\"150平米\"}},{\"JUSHI\":\"3\",\"HX_AREA\":\"3\",\"DETAIL\":{\"REMARKS\":\"三室二厅\",\"iscover\":\"0\",\"URL\":\"http://house.focus.cn/upload/photos/906/phpukTTzD3.jpg\",\"TITLE\":\"三居\",\"SRC_TYPE\":\"residential_jiaodian_api\",\"PIC_LAYOUT\":\"3室2厅1卫\",\"LAYOUT_AREA\":\"150平米\",\"INDOOR_AREA\":\"150平米\"}}]}";
        Map m = Utils.loadsJson(jsonStr, Map.class);
        System.out.println(Utils.dumpObjectAsString(getForMap(m)));
    }

    public static Map getForMap(Map m) {
        Map<String, Object> mp = Maps.newLinkedHashMap();
        for (Object o : m.keySet()) {
            String key = (String)o;
            String lower = key.toLowerCase();
            Object value = m.get(key);
            if (value == null) {
                mp.put(lower, null);
            } else if (value instanceof Map) {
                mp.put(lower, getForMap((Map) value));
            } else if (value instanceof List) {
                mp.put(lower, getForList((List)value)) ;
            } else {
                mp.put(lower, value);
            }
        }
        return mp;
    }

    private static List getForList(List value) {
        List<Object> ls = Lists.newArrayList();
        for (Object o : value) {
            if (o instanceof Map) {
                ls.add(getForMap((Map) o));
            } else if (o instanceof List) {
                ls.add(getForList((List) o));
            } else {
                ls.add(o);
            }
        }
        return ls;
    }
}

