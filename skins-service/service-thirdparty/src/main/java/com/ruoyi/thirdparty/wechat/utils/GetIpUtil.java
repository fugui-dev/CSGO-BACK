package com.ruoyi.thirdparty.wechat.utils;

import com.ruoyi.common.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class GetIpUtil {
    public static String getIpAddress(HttpServletRequest request) {
        // 首先, 获取 X-Forwarded-For 中的 IP 地址，它在 HTTP 扩展协议中能表示真实的客户端 IP
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress)) {
            // 多次反向代理后会有多个 ip 值，第一个 ip 才是真实 ip, 例: X-Forwarded-For: client, proxy1, proxy2，proxy…
            int index = ipAddress.indexOf(",");
            if (index != -1) {
                return ipAddress.substring(0, index);
            }

            return ipAddress;
        }

        // 如果 X-Forwarded-For 获取不到, 就去获取 X-Real-IP
        ipAddress = request.getHeader("X-Real-IP");
        if (StringUtils.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            // 如果 X-Real-IP 获取不到, 就去获取 Proxy-Client-IP
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            // 如果 Proxy-Client-IP 获取不到, 就去获取 WL-Proxy-Client-IP
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            // 如果 WL-Proxy-Client-IP 获取不到, 就去获取 HTTP_CLIENT_IP
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            // 如果 HTTP_CLIENT_IP 获取不到, 就去获取 HTTP_X_FORWARDED_FOR
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            // 都获取不到, 最后才通过 request.getRemoteAddr() 获取IP
            ipAddress = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ipAddress) ? "127.0.0.1" : ipAddress;
    }

}
