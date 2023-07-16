package com.fugary.simple.douban.controller;

import com.fugary.simple.douban.loader.BookLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private BookLoader bookLoader;

    @ResponseBody
    @GetMapping(value = "/view/cover", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] viewImage(@RequestParam(name = "cover") String coverUrl) {
        byte[] resultBytes = bookLoader.loadImage(coverUrl);
        if (resultBytes != null) {
            return resultBytes;
        }
        return new byte[0];
    }
}
