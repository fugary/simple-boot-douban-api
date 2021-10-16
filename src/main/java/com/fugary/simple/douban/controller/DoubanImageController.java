package com.fugary.simple.douban.controller;

import com.fugary.simple.douban.config.DoubanApiConfigProperties;
import com.fugary.simple.douban.loader.BookLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

/**
 * Created on 2021/10/15 15:47 .<br>
 *
 * @author gary.fu
 */
@Controller
@RequestMapping("/")
@Slf4j
public class DoubanImageController {

    @Autowired
    private DoubanApiConfigProperties doubanApiConfigProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BookLoader bookLoader;

    /**
     * 获取URI
     *
     * @return
     */
    protected URI getImageURI(int serverId) {
        return restTemplate.getUriTemplateHandler().expand(doubanApiConfigProperties.getImageUrl()
                , StringUtils.join("img", serverId));
    }

    @ResponseBody
    @GetMapping(value = "/view/**", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] viewImage(HttpServletRequest request, HttpServletResponse response) {
        for (Integer serverId : doubanApiConfigProperties.getImageDomains()) {
            URI uri = getImageURI(serverId);
            String requestUrl = request.getRequestURI();
            uri = UriComponentsBuilder.fromUri(uri)
                    .path(requestUrl)
                    .query(request.getQueryString())
                    .build(true).toUri();
            byte[] resultBytes = bookLoader.loadImage(uri.toString());
            if (resultBytes != null) {
                return resultBytes;
            }
        }
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return new byte[0];
    }
}
