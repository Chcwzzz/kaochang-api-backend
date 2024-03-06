package com.chcwzzz.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户接口信息
 *
 */
@Data
public class UserInterfaceInfoVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

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
    private String requestParams;

    /**
     * 响应参数
     */
    private String responseParams;

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
     * 接口状态 0 - 关闭 1 - 开启
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 接口总调用次数
     */
    private Integer usertotalinvokes;

    /**
     * 接口剩余调用次数
     */
    private Integer userleftinvokes;

    private static final long serialVersionUID = 1L;
}