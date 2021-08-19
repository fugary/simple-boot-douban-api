package com.fugary.simple.douban.controller;

import com.fugary.simple.douban.config.DoubanApiConfigProperties;
import com.fugary.simple.douban.provider.BookHtmlParseProvider;
import com.fugary.simple.douban.util.DoubanUrlUtils;
import com.fugary.simple.douban.vo.BookVo;
import com.fugary.simple.douban.vo.ResultVo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

/**
 * Created on 2021/8/17 18:43 .<br>
 *
 * @author gary.fu
 */
@RestController
@RequestMapping("/v2/book")
public class DoubanApiController {

    @Autowired
    private DoubanApiConfigProperties doubanApiConfigProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BookHtmlParseProvider bookHtmlParseProvider;

    @GetMapping("/search")
    @ResponseBody
    public ResultVo searchBook(@RequestParam("q") String searchText) {
        if (searchText.matches("\\d{10,}")) {
            return searchIsbn(searchText);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, doubanApiConfigProperties.getUserAgent());
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        String catType = doubanApiConfigProperties.getMappings().get("book");
        ResponseEntity<String> responseEntity = restTemplate.exchange(doubanApiConfigProperties.getSearchUrl(), HttpMethod.GET, entity, String.class, catType, searchText);
        String resultStr = responseEntity.getBody();
        Document doc = Jsoup.parse(resultStr);
        Element content = doc.selectFirst("a.nbg");
        ResultVo resultVo = new ResultVo();
        if (content != null) {
            String href = content.attr("href");
            Map<String, String> map = DoubanUrlUtils.parseQuery(URI.create(href).getQuery());
            String url = map.get("url");
            resultStr = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            BookVo bookVo = bookHtmlParseProvider.parse(url, resultStr);
            if (bookVo != null) {
                resultVo.setSuccess(true);
                resultVo.setBooks(Arrays.asList(bookVo));
            }
        }
        return resultVo;
    }

    @GetMapping("/isbn/{isbn}")
    @ResponseBody
    public ResultVo searchIsbn(@PathVariable("isbn") String isbn) {
        return detailResult(doubanApiConfigProperties.getIsbnUrl(), isbn);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResultVo detail(@PathVariable("id") String id) {
        return detailResult(doubanApiConfigProperties.getDetailUrl(), id);
    }

    /**
     * 获取详情
     *
     * @param url
     * @param id
     * @return
     */
    protected ResultVo detailResult(String url, String id) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, doubanApiConfigProperties.getUserAgent());
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        URI uri = restTemplate.getUriTemplateHandler().expand(url, id);
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        String resultStr = responseEntity.getBody();
        ResultVo resultVo = new ResultVo();
        BookVo bookVo = bookHtmlParseProvider.parse(uri.toString(), resultStr);
        if (bookVo != null) {
            resultVo.setSuccess(true);
            resultVo.setBooks(Arrays.asList(bookVo));
        }
        return resultVo;
    }

}
