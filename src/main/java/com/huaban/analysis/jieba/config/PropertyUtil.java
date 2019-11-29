/*
 * @(#) PropertyUtil
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 * <br> Copyright:  Copyright (c) 2019
 * <br> Company:厦门畅享信息技术有限公司
 * <br> @author chenhao
 * <br> 2019-11-26 16:34:59
 *
 */

package com.huaban.analysis.jieba.config;

import java.io.IOException;
import java.util.Properties;

public class PropertyUtil {

    public static String getKey(String key) {
        Properties prop = new Properties();
        try {
            prop.load(PropertyUtil.class.getResourceAsStream("/path.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop.getProperty(key, "只是个默认值");
    }
}
