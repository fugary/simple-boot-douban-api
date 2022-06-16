package com.fugary.simple.douban.util;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created on 2021/8/17 20:59 .<br>
 *
 * @author gary.fu
 */
public class DoubanUrlUtils {

    private static final Pattern QUERY_PATTERN = Pattern.compile("\\s*&\\s*");

    public static final Pattern ID_PATTERN = Pattern.compile(".*/subject/(\\d+)/?");

    /**
     * 解析url地址中的参数
     *
     * @param query
     * @return
     */
    public static Map<String, String> parseQuery(String query) {
        return QUERY_PATTERN
                .splitAsStream(query.trim())
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
    }

    public static boolean isBookUrl(String url){
        return ID_PATTERN.matcher(url).matches();
    }
}
