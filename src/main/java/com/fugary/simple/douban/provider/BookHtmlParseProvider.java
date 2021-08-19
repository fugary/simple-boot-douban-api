package com.fugary.simple.douban.provider;

import com.fugary.simple.douban.vo.BookVo;

/**
 * Created on 2021/8/17 18:56 .<br>
 *
 * @author gary.fu
 */
public interface BookHtmlParseProvider {
    /**
     * 从HTML解析书籍信息
     *
     * @param url
     * @param html
     * @return
     */
    BookVo parse(String url, String html);
}
