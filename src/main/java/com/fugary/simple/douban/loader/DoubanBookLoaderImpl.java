package com.fugary.simple.douban.loader;

import com.fugary.simple.douban.provider.BookHtmlParseProvider;
import com.fugary.simple.douban.util.HttpRequestUtils;
import com.fugary.simple.douban.vo.BookVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author Gary Fu
 * @date 2021/8/28 11:02
 */
@Component
@Slf4j
public class DoubanBookLoaderImpl implements BookLoader {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BookHtmlParseProvider bookHtmlParseProvider;

    @Cacheable(cacheNames = "dobanBook", sync = true)
    @Override
    public BookVo loadBook(String bookUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, HttpRequestUtils.getUserAgent());
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        String bookStr = restTemplate.exchange(bookUrl, HttpMethod.GET, entity, String.class).getBody();
        return bookHtmlParseProvider.parse(bookUrl, bookStr);
    }

    @Cacheable(cacheNames = "doubanImage", sync = true)
    @Override
    public byte[] loadImage(String imageUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, HttpRequestUtils.getUserAgent());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(imageUrl, HttpMethod.GET, entity, byte[].class);
            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                log.info("获取{}图片成功", imageUrl);
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("获取{}图片异常: {}", imageUrl, e.getMessage());
        }
        return null;
    }
}
