package com.fugary.simple.douban.controller;

import com.fugary.simple.douban.config.DoubanApiConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;

/**
 * Created on 2021/10/15 15:47 .<br>
 *
 * @author gary.fu
 */
@Controller
@RequestMapping("/")
public class DoubanImageController {

    @Autowired
    private DoubanApiConfigProperties doubanApiConfigProperties;

    @Autowired
    private RestTemplate restTemplate;

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
    public byte[] viewImage(HttpServletRequest request) {
        for (Integer serverId : doubanApiConfigProperties.getImageDomains()) {
            URI uri = getImageURI(serverId);
            String requestUrl = request.getRequestURI();
            uri = UriComponentsBuilder.fromUri(uri)
                    .path(requestUrl)
                    .query(request.getQueryString())
                    .build(true).toUri();
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.set(headerName, request.getHeader(headerName));
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                return responseEntity.getBody();
            }
        }
        return new byte[0];
    }
}
