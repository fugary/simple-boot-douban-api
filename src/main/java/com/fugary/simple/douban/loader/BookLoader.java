package com.fugary.simple.douban.loader;

import com.fugary.simple.douban.vo.BookVo;

/**
 * @author Gary Fu
 * @date 2021/8/28 11:01
 */
public interface BookLoader {

    /**
     * 加载书籍信息
     *
     * @param bookUrl
     * @return
     */
    BookVo loadBook(String bookUrl);

    /**
     * 加载图片
     *
     * @param imageUrl
     * @return
     */
    byte[] loadImage(String imageUrl);
}
