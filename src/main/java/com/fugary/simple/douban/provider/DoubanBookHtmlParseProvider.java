package com.fugary.simple.douban.provider;

import com.fugary.simple.douban.vo.BookVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fugary.simple.douban.util.DoubanUrlUtils.ID_PATTERN;

/**
 * Created on 2021/8/17 18:58 .<br>
 *
 * @author gary.fu
 */
@Slf4j
@Component
public class DoubanBookHtmlParseProvider implements BookHtmlParseProvider {

    private static final Pattern SERIES_PATTERN = Pattern.compile(".*/series/(\\d+)/?");

    private static final Pattern TAGS_PATTERN = Pattern.compile("criteria = '(.+)'");

    @Override
    public BookVo parse(String url, String html) {
        Document doc = Jsoup.parse(html);
        Elements contentElements = doc.select("body");
        Element content = contentElements.isEmpty() ? null : contentElements.get(0);
        String title = doc.select("[property='v:itemreviewed']").text(); // title获取方式修改
        BookVo bookVo = null;
        if (content != null) {
            bookVo = new BookVo();
            Element shareElement = content.selectFirst("a.bn-sharing");
            if (shareElement != null) {
                url = shareElement.attr("data-url");
            }
            bookVo.setUrl(url);
            Matcher matcher = ID_PATTERN.matcher(url);
            if (matcher.matches()) {
                bookVo.setId(matcher.group(1));
            }
            Element aNbg = content.selectFirst("a.nbg");
            if (aNbg != null) {
                bookVo.setImage(aNbg.attr("href"));
            }
            bookVo.setTitle(title);
            Element rateElement = content.selectFirst("[property='v:average']");
            if (rateElement != null) {
                Map<String, String> ratingMap = new HashMap<>();
                ratingMap.put("average", rateElement.text().trim());
                bookVo.setRating(ratingMap);
            }
            Elements elements = content.select("span.pl");
            for (Element element : elements) {
                String text = element.text();
                boolean translator = text.startsWith("译者");
                if (text.startsWith("作者") || translator) {
                    Elements authorElements = element.nextElementSiblings();
                    List<String> authors = new ArrayList<>();
                    for (Element authorElement : authorElements) {
                        if (authorElement.is("br")) {
                            break;
                        }
                        authors.addAll(Arrays.stream(authorElement.text().split("\\s*/\\s*")).collect(Collectors.toList()));
                    }
                    if (translator) {
                        bookVo.setTranslator(authors);
                    } else {
                        bookVo.setAuthor(authors);
                    }
                } else if (text.startsWith("原作名")) {
                    bookVo.setOriginTitle(getInfo(element));
                } else if (text.startsWith("出版社")) {
                    bookVo.setPublisher(getInfoOrNext(element));
                } else if (text.startsWith("出版年")) {
                    bookVo.setPublishDate(getInfo(element));
                } else if (text.startsWith("ISBN")) {
                    bookVo.setIsbn13(getInfo(element));
                } else if (text.startsWith("页数")) {
                    bookVo.setPages(getInfo(element));
                } else if (text.startsWith("定价")) {
                    bookVo.setPrice(getInfo(element));
                } else if (text.startsWith("装帧")) {
                    bookVo.setBinding(getInfo(element));
                } else if (text.startsWith("丛书")) {
                    Element seriesElement = element.nextElementSibling();
                    if (seriesElement != null) {
                        String seriesHref = seriesElement.attr("href");
                        Matcher seriesMatcher = SERIES_PATTERN.matcher(seriesHref);
                        Map<String, String> series = new HashMap<>();
                        if (seriesMatcher.matches()) {
                            series.put("id", seriesMatcher.group(1));
                        }
                        series.put("title", seriesElement.text());
                        bookVo.setSeries(series);
                    }
                }
            }
            Element summaryElement = content.selectFirst("#link-report :not(.short) .intro");
            String summary = StringUtils.EMPTY;
            if (summaryElement != null) {
                summary = StringUtils.trimToEmpty(summaryElement.html());
            }
            bookVo.setSummary(summary);
            Matcher tagMatcher = TAGS_PATTERN.matcher(html);
            if (tagMatcher.find()) {
                bookVo.setTags(Arrays.stream(tagMatcher.group(1).split("[|]")).filter(tag -> tag.startsWith("7:")).distinct()
                        .map(tag -> {
                            tag = tag.replace("7:", "");
                            Map<String, String> tagMap = new HashMap<>();
                            tagMap.put("name", tag);
                            tagMap.put("title", tag);
                            return tagMap;
                        }).collect(Collectors.toList()));
            }
            log.info("解析书籍成功:{}", bookVo);
        } else {
            log.error("获取书籍失败：{}", url);
        }
        return bookVo;
    }

    protected String getInfo(Element element) {
        return element.nextSibling().toString().trim();
    }

    protected String getInfoOrNext(Element element) {
        String info = element.nextSibling().toString().trim();
        if (StringUtils.isBlank(info)) {
            Element publisherElement = element.nextElementSibling();
            if (publisherElement != null) {
                info = StringUtils.trimToEmpty(publisherElement.text());
            }
        }
        return info;
    }

}
