package com.ruoyi.common.utils.file;

import com.ruoyi.common.config.RuoYiConfig;

import java.io.File;

public class ApiStringUtils {

    public static int findNthMatchIndex(String str, String matchStr, int n) {
        return findNthMatchIndexHelper(str, matchStr, n, 0);
    }

    private static int findNthMatchIndexHelper(String str, String matchStr, int n, int startIndex) {
        int index = str.indexOf(matchStr, startIndex);
        if (index == -1) {
            return -1;
        }
        if (n == 1) {
            return index;
        }
        return findNthMatchIndexHelper(str, matchStr, n - 1, index + matchStr.length());
    }

    public static String delAvatar(String filePath) {
        String domainName = RuoYiConfig.getDomainName();
        int numberOfSlashes = domainName.length() - domainName.replaceAll("/", "").length();
        int nthMatchIndex = ApiStringUtils.findNthMatchIndex(filePath, "/", numberOfSlashes + 2);
        File delFile = new File(RuoYiConfig.getProfile() + filePath.substring(nthMatchIndex));
        if (delFile.delete()) return "删除成功！";
        return "";
    }
}
