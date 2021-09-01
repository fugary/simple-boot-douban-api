package com.fugary.simple.douban.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created on 2021/8/17 21:30 .<br>
 *
 * @author gary.fu
 */
@Data
public class BookVo implements Serializable {

    private static final long serialVersionUID = 8733227249093462908L;

    private String id;
    private String title;
    @JsonProperty("origin_title")
    private String originTitle;
    private List<String> author;
    private List<String> translator;
    private String summary;
    private String publisher;
    @JsonProperty("pubdate")
    private String publishDate = "1900-01";
    private List<Map<String, String>> tags;
    private Map<String, String> rating;
    private Map<String, String> series;
    private String image;
    private String url;
    private String isbn13;
    private String isbn10;
    private String pages;
    private String binding;
    private String price;
    @JsonProperty("author_intro")
    private String authorIntro;
    private String catalog;
    @JsonProperty("ebook_url")
    private String ebookUrl;
    @JsonProperty("ebook_price")
    private String ebookPrice;
}
