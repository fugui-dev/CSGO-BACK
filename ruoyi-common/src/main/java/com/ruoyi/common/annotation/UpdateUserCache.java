package com.ruoyi.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义更新用户缓存注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UpdateUserCache {
}
