package com.chcwzzz.common.model.dto.Interfaceinfo;

import com.chcwzzz.common.common.UserRequestParams;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 调用接口请求
 */
@Data
public class InterfaceInvokeRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求方式
     */
    private String method;
    /**
     * 请求参数
     */
    private List<UserRequestParams> requestParams;


    private static final long serialVersionUID = 1L;
}