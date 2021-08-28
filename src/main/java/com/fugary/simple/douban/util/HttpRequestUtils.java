package com.fugary.simple.douban.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Gary Fu
 * @date 2021/8/28 11:09
 */
public class HttpRequestUtils {

    private HttpRequestUtils() {
    }

    /**
     * 获取Request
     *
     * @return
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes != null ? requestAttributes.getRequest() : null;
    }

    /**
     * 获取HEADER
     *
     * @param key
     * @return
     */
    public static String getHeader(String key) {
        HttpServletRequest currentRequest = getCurrentRequest();
        if (currentRequest != null) {
            return currentRequest.getHeader(key);
        }
        return null;
    }
}
