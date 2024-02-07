package com.chcwzzz.project.common;

import lombok.Data;

/**
 * 请求参数类
 */
@Data
public class RequestParams {
    /**
     * 参数id，用于前端区分参数
     */
    private String  id;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数类型
     */
    private String type;

    /**
     * 是否必填
     */
    private String required;

    /**
     * 参数描述
     */
    private String description;
}
