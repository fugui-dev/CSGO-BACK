package com.ruoyi.admin.util;

import java.util.Random;

public class PhoneNumberUtil {

    private static final String[] CHINA_MOBILE = {
            "134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159",
            "182", "183", "184", "187", "188", "178", "147", "172", "198"
    };

    private static final String[] CHINA_UNICOM = {"130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166"};

    private static final String[] CHINA_TELECOM = {"133", "149", "153", "173", "177", "180", "181", "189", "199"};

    public static String createPhoneNumber(int operator) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        String mobilePrefix = null;
        switch (operator) {
            case 0:
                mobilePrefix = CHINA_MOBILE[random.nextInt(CHINA_MOBILE.length)];
                break;
            case 1:
                mobilePrefix = CHINA_UNICOM[random.nextInt(CHINA_UNICOM.length)];
                break;
            case 2:
                mobilePrefix = CHINA_TELECOM[random.nextInt(CHINA_TELECOM.length)];
                break;
            default:
                mobilePrefix = "运营商错误";
                break;
        }

        builder.append(mobilePrefix);
        int temp;
        for (int i = 0; i < 8; i++) {
            temp = random.nextInt(10);
            builder.append(temp);
        }
        return builder.toString();
    }
}
