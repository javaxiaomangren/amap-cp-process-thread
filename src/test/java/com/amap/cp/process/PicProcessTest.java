package com.amap.cp.process;

import com.amap.cp.utils.Utils;
import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.Map;

/**
 * Test Pic Process
 * Created by yang.hua on 14-2-13.
 */
public class PicProcessTest extends TestCase {
    private static final PropertiesConfiguration conf = Utils.COMM_CONF;
    private static final String imageService1 = conf.getString("url.image1");
    private static final String imageService2 = conf.getString("url.image2");
    PicProcess picProcess = new PicProcess();

    public void testGetPicInfo() throws Exception {

    }

    public void testGetGaoDePic() throws Exception {
        Map m = ImmutableMap.of("url", "http://house.focus.cn/upload/photos/862/phpIXRXqwHouseImageshow0022.jpg");
        Map newPic = picProcess.getGaoDePic(m);
        assertNotNull(newPic);
    }

    public void testGetPicString() throws Exception {
        String json = "[{\"url\":\"http://imgs.focus.cn/upload/bt/6491/a_64904913.jpg\"}]";
        String json2 = "[{\"url\":\"http://imgs.focus.cn/upload/cd/8467/a_84664697.jpg\"}]";
        String result1 = picProcess.getPicString(imageService1, json) ;
        String result2 = picProcess.getPicString(imageService2, json2);
        System.out.println(result1);
        System.out.println(result2);
        assertNotNull(result1);
        assertNotNull(result2);
    }
}
