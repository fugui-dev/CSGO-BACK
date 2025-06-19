package com.ruoyi.common.annotation;

import java.lang.annotation.*;

/**
 * 方法之前更新用户缓存
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NewUserInfo {
}
