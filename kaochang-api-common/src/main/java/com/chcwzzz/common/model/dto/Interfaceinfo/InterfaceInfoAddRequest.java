package com.chcwzzz.common.model.dto.Interfaceinfo;

import com.chcwzzz.common.common.RequestParams;
import com.chcwzzz.common.common.ResponseParams;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求参数
     */
    private List<RequestParams> requestParams;

    /**
     * 响应参数
     */
    private List<ResponseParams> responseParams;

    /**
     * 请求示例
     */
    private String requestExample;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 请求类型
     */
    private String method;

    private static final long serialVersionUID = 1L;
}