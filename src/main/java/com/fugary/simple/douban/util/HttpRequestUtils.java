package com.fugary.simple.douban.util;

import org.apache.commons.lang3.StringUtils;
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

    /**
     * 计算schema
     *
     * @return
     */
    public static String getSchema() {
        HttpServletRequest currentRequest = getCurrentRequest();
        String schema = StringUtils.EMPTY;
        if (currentRequest != null) {
            schema = currentRequest.getHeader("x-forwarded-proto");
            if (StringUtils.isBlank(schema)) {
                schema = currentRequest.getScheme();
            }
        }
        if (StringUtils.isBlank(schema)) {
            schema = "http";
        }
        return schema;
    }
}
