package com.fugary.simple.douban;

import com.fugary.simple.douban.config.DoubanApiConfigProperties;
import com.fugary.simple.douban.jsonp.JsonpResponseBodyAdvice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableConfigurationProperties({DoubanApiConfigProperties.class})
@SpringBootApplication
@EnableCaching
public class SimpleBootDoubanApiApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public JsonpResponseBodyAdvice jsonpResponseBodyAdvice(){
        return new JsonpResponseBodyAdvice("callback");
    }

    public static void main(String[] args) {
        SpringApplication.run(SimpleBootDoubanApiApplication.class, args);
    }

}
