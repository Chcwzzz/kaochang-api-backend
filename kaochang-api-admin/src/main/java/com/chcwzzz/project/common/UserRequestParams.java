package com.chcwzzz.project.common;

import lombok.Data;

/**
 * 用户请求参数类
 */
@Data
public class UserRequestParams {
    /**
     * 参数id，用于前端区分参数
     */
    private String  id;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数值
     */
    private Object value;
}
