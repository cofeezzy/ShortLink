package com.zzy.shortLink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

import static com.zzy.shortLink.project.common.constant.ShortLinkConstant.DEFAULT_LINK_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短链接缓存有效时间
     * @param validateDate 有效期时间
     * @return 有效期时间戳
     */
    public static long getLinkCacheValidTime(Date validateDate){
        return Optional.ofNullable(validateDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_LINK_CACHE_VALID_TIME);
    }

    /**
     * 获取请求的真实IP地址
     * @param request 请求
     * @return 用户真实IP
     */
    public static String getRealAddress(HttpServletRequest request){
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }

    /**
     * 获取用户访问的OS
     * @param request 请求
     * @return 访问的OS
     */
    public static String getOS(HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");
        if(userAgent.toLowerCase().contains("windows")){
            return "Windows";
        }else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("unix")) {
            return "Unix";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }
}
