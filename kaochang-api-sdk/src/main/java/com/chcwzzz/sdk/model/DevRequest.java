package com.chcwzzz.sdk.model;

import lombok.Data;

/**
 * API请求对象
 * @author Chcwzzz
 */
@Data
public class DevRequest {
    /**
     * 请求url地址
     */
    private String url;
    /**
     * 请求体body
     */
    private Object body;
    /**
     * 请求方式
     */
    private String method;
}
