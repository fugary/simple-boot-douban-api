package com.fugary.simple.douban.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2021/8/17 19:23 .<br>
 *
 * @author gary.fu
 */
@ConfigurationProperties(prefix = "douban.api")
@Data
public class DoubanApiConfigProperties {

    private Map<String, String> mappings = new HashMap<>();

    private String userAgent;

    private String searchUrl;

    private String detailUrl;

    private String isbnUrl;

}
