package com.ruoyi.thirdparty.zbt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ZBTUtils {

    private static final Logger log = LoggerFactory.getLogger(ZBTUtils.class);

    public static <T> Map<String, String> objectToMap(T obj) {
        Map<String, String> map = new HashMap<>();
        if (obj == null) {
            return map;
        }

        try {
            Class<?> clazz = obj.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String value = (String) field.get(obj);
                if (value != null) {
                    map.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            log.error("对象转Map时出现异常", e);
        }
        return map;
    }
}
