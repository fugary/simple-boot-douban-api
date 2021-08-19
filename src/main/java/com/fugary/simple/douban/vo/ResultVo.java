package com.fugary.simple.douban.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 2021/8/17 19:28 .<br>
 *
 * @author gary.fu
 */
@Data
public class ResultVo implements Serializable {

    private static final long serialVersionUID = -8671929517795109491L;

    private boolean success;

    private String message;

    private List<BookVo> books;

}
