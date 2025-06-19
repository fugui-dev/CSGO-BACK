package com.ruoyi.thirdparty.baiduPromotion.util;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtil {

    /**
     * 从给定的URL中提取域名
     *
     * @param urlString 要提取域名的URL字符串
     * @return 提取到的域名，如果URL无效则返回null
     */
    public static String extractDomain(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            // 处理无效的URL
            System.err.println("Invalid URL: " + urlString);
            return null;
        }
    }

//    public static void main(String[] args) {
//        // 测试示例
//        String[] testUrls = {
//                "https://www.example.com/path?query=123",
//                "http://subdomain.example.co.uk/path/to/resource",
//                "ftp://ftp.example.org/resource",
//                "invalid-url"
//        };
//
//        for (String url : testUrls) {
//            System.out.println("URL: " + url);
//            System.out.println("Domain: " + extractDomain(url));
//            System.out.println();
//        }
//    }
}
