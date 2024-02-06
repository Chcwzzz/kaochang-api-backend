package com.chcwzzz.project.common;

import lombok.Data;

/**
 * 响应参数类
 */
@Data
public class ResponseParams {
    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数类型
     */
    private String type;

    /**
     * 参数描述
     */
    private String description;
}
