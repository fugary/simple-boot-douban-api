package com.fugary.simple.douban.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2021/8/19 11:29 .<br>
 *
 * @author gary.fu
 */
@Data
public class DoubanSearchResultVo implements Serializable {

    private boolean more;

    private int limit;

    private int total;

    private List<String> items = new ArrayList<>();

}
