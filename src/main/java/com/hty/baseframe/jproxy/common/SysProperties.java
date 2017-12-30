package com.hty.baseframe.jproxy.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.hty.baseframe.common.util.StringUtil;

public class SysProperties {

    private BeanProvider beanProvider;

    /**
     * 保存配置的Map
     */
    private static final Map<String, String> properties =
            Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * 添加配置
     *
     * @param key
     * @param value
     */
    public static void addProperty(String key, String value) {
        properties.put(StringUtil.trim(key), StringUtil.trim(value));
    }

    /**
     * 获取配置
     *
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        return properties.get(key);
    }

    public BeanProvider getBeanProvider() {
        return beanProvider;
    }

    public void setBeanProvider(BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }
}
