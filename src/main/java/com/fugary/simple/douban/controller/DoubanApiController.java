package com.fugary.simple.douban.controller;

import com.fugary.simple.douban.config.DoubanApiConfigProperties;
import com.fugary.simple.douban.provider.BookHtmlParseProvider;
import com.fugary.simple.douban.util.DoubanUrlUtils;
import com.fugary.simple.douban.vo.BookVo;
import com.fugary.simple.douban.vo.DoubanSearchResultVo;
import com.fugary.simple.douban.vo.ResultVo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created on 2021/8/17 18:43 .<br>
 *
 * @author gary.fu
 */
@RestController
@RequestMapping("/v2/book")
public class DoubanApiController {

    private static final Logger logger = LoggerFactory.getLogger(DoubanApiController.class);

    @Autowired
    private DoubanApiConfigProperties doubanApiConfigProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BookHtmlParseProvider bookHtmlParseProvider;

    /**
     * 新建一个线程池
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    @GetMapping("/search")
    @ResponseBody
    public ResultVo searchBook(@RequestParam("q") String searchText, HttpServletRequest request) throws ExecutionException, InterruptedException {
        if (searchText.matches("\\d{10,}")) {
            return searchIsbn(searchText, request);
        }
        long start = System.currentTimeMillis();
        ResultVo resultVo = new ResultVo();
        resultVo.setBooks(new ArrayList<>());
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT));
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        String catType = doubanApiConfigProperties.getMappings().get("book");
//        List<Element> bookElements = searchBookElements(searchText, entity, catType); // 按照网页查询，应该速度稍慢
        List<Element> bookElements = searchBookElementsNew(searchText, entity, catType);
        logger.info("查询列表{}条耗时{}ms", bookElements.size(), System.currentTimeMillis() - start);
        List<CompletableFuture<BookVo>> list = new ArrayList<>();
        // 多线程查询多本书籍
        bookElements.forEach(content -> {
            String href = content.attr("href");
            Map<String, String> map = DoubanUrlUtils.parseQuery(URI.create(href).getQuery());
            String url = map.get("url");
            list.add(CompletableFuture.supplyAsync(() -> {
                String bookStr = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
                BookVo bookVo = bookHtmlParseProvider.parse(url, bookStr);
                if (bookVo != null) {
                    resultVo.setSuccess(true);
                    resultVo.getBooks().add(bookVo);
                }
                return bookVo;
            }, executorService));
        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
        logger.info("查询书籍{}条完成耗时{}ms", bookElements.size(), System.currentTimeMillis() - start);
        return resultVo;
    }

    /**
     * 列表页从html中获取
     *
     * @param searchText
     * @param entity
     * @param catType
     * @return
     */
    protected List<Element> searchBookElements(String searchText, HttpEntity<String> entity, String catType) {
        ResponseEntity<String> responseEntity = restTemplate.exchange(doubanApiConfigProperties.getSearchUrl(), HttpMethod.GET, entity, String.class, catType, searchText);
        String resultStr = responseEntity.getBody();
        Document doc = Jsoup.parse(resultStr);
        return doc.select("a.nbg").stream().limit(doubanApiConfigProperties.getCount()).collect(Collectors.toList());
    }

    /**
     * 列表从json获取
     *
     * @param searchText
     * @param entity
     * @param catType
     * @return
     */
    protected List<Element> searchBookElementsNew(String searchText, HttpEntity<String> entity, String catType) {
        ResponseEntity<DoubanSearchResultVo> responseEntity = restTemplate.exchange(doubanApiConfigProperties.getSearchJsonUrl(), HttpMethod.GET, entity, DoubanSearchResultVo.class, catType, searchText);
        DoubanSearchResultVo doubanResultVo = responseEntity.getBody();
        if (doubanResultVo != null) {
            return doubanResultVo.getItems().stream().limit(doubanApiConfigProperties.getCount())
                    .flatMap(elementStr -> Jsoup.parseBodyFragment(elementStr).body().select("a.nbg").stream()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    @GetMapping("/isbn/{isbn}")
    @ResponseBody
    public ResultVo searchIsbn(@PathVariable("isbn") String isbn, HttpServletRequest request) {
        return detailResult(doubanApiConfigProperties.getIsbnUrl(), isbn, request.getHeader(HttpHeaders.USER_AGENT));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResultVo detail(@PathVariable("id") String id, HttpServletRequest request) {
        return detailResult(doubanApiConfigProperties.getDetailUrl(), id, request.getHeader(HttpHeaders.USER_AGENT));
    }

    /**
     * 获取详情
     *
     * @param url
     * @param id
     * @return
     */
    protected ResultVo detailResult(String url, String id, String userAgent) {
        long start = System.currentTimeMillis();
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
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
        logger.info("精确查询{}耗时{}ms", id, System.currentTimeMillis() - start);
        return resultVo;
    }

}
