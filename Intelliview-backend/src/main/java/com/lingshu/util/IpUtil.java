package com.lingshu.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class IpUtil {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // 如果是多个IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    public static String getLocationByIp(String ip) {
        // 简单的IP位置获取（实际项目中可使用IP库）
        if (ip == null || ip.isEmpty()) {
            return "未知";
        }

        // 内网IP
        if (ip.startsWith("127.") || ip.startsWith("192.168.") ||
                ip.startsWith("10.") || ip.startsWith("172.")) {
            return "内网";
        }

        // 此处可集成IP地址库获取真实位置
        return "未知";
    }
}
