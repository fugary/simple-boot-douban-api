package com.fugary.simple.douban.controller;

import com.fugary.simple.douban.config.DoubanApiConfigProperties;
import com.fugary.simple.douban.loader.BookLoader;
import com.fugary.simple.douban.util.DoubanUrlUtils;
import com.fugary.simple.douban.util.HttpRequestUtils;
import com.fugary.simple.douban.vo.BookVo;
import com.fugary.simple.douban.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

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

/**
 * Created on 2021/8/17 18:43 .<br>
 *
 * @author gary.fu
 */
@Slf4j
@RestController
@RequestMapping("/v2/book")
public class DoubanApiController {

    @Autowired
    private DoubanApiConfigProperties doubanApiConfigProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BookLoader bookLoader;

    /**
     * 新建一个线程池
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @GetMapping("/search")
    @ResponseBody
    public ResultVo searchBook(@RequestParam("q") String searchText) throws ExecutionException, InterruptedException {
        if (searchText.matches("\\d{10,}")) {
            return searchIsbn(searchText);
        }
        long start = System.currentTimeMillis();
        ResultVo resultVo = new ResultVo();
        resultVo.setBooks(new ArrayList<>());
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, HttpRequestUtils.getUserAgent());
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        String catType = doubanApiConfigProperties.getMappings().get("book");
        List<Element> bookElements = searchBookElements(searchText, entity, catType); // 按照网页查询，应该速度稍慢
        log.info("查询列表{}条耗时{}ms", bookElements.size(), System.currentTimeMillis() - start);
        List<CompletableFuture<BookVo>> list = new ArrayList<>();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 多线程查询多本书籍
        bookElements.forEach(content -> {
            String href = content.attr("href");
            Map<String, String> map = DoubanUrlUtils.parseQuery(URI.create(href).getQuery());
            String url = map.get("url");
            if (DoubanUrlUtils.isBookUrl(url) && list.size() < doubanApiConfigProperties.getCount()) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    BookVo bookVo = bookLoader.loadBook(url);
                    if (bookVo != null) {
                        resultVo.setSuccess(true);
                        processBookImage(bookVo);
                        resultVo.getBooks().add(bookVo);
                    }
                    return bookVo;
                }, executorService));
            }
        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
        log.info("查询书籍{}条完成耗时{}ms", list.size(), System.currentTimeMillis() - start);
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
        return doc.select("a.nbg");
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
        long start = System.currentTimeMillis();
        URI uri = restTemplate.getUriTemplateHandler().expand(url, id);
        ResultVo resultVo = new ResultVo();
        BookVo bookVo = bookLoader.loadBook(uri.toString());
        if (bookVo != null) {
            resultVo.setSuccess(true);
            processBookImage(bookVo);
            resultVo.setBooks(Arrays.asList(bookVo));
        }
        log.info("精确查询{}耗时{}ms", id, System.currentTimeMillis() - start);
        return resultVo;
    }

    /**
     * 代理图片
     *
     * @param bookVo
     */
    protected void processBookImage(BookVo bookVo) {
        if (bookVo != null && StringUtils.isNotBlank(bookVo.getImage()) && doubanApiConfigProperties.isProxyImageUrl()) {
            HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/view/cover")
                    .scheme(HttpRequestUtils.getSchema())
                    .host(request.getServerName()).port(request.getServerPort());
            if (!bookVo.getImage().contains(builder.toUriString())) {
                String coverUrl = builder.queryParam("cover", bookVo.getImage()).build().toUriString();
                bookVo.setImage(coverUrl);
            }
        }
    }
}
