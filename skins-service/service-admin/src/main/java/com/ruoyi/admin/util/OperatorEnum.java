package com.ruoyi.admin.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperatorEnum {

    CHINA_MOBILE(0, "中国移动"),
    CHINA_UNICOM(1, "中国联通"),
    CHINA_TELECOM(2, "中国电信");

    private final Integer code;

    private final String name;

    public static String getOperateNameByCode(Integer code) {
        for (OperatorEnum value : OperatorEnum.values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return null;
    }
}

