package com.chcwzzz.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户接口信息表
 * @TableName user_interface_info
 */
@TableName(value ="user_interface_info")
@Data
public class UserInterfaceInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 调用者id
     */
    private Long userid;

    /**
     * 接口id
     */
    private Long interfaceinfoid;

    /**
     * 接口总调用次数
     */
    private Integer usertotalinvokes;

    /**
     * 接口剩余调用次数
     */
    private Integer userleftinvokes;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 更新时间
     */
    private Date updatetime;

    /**
     * 是否删除 0 - 未删  1 - 已删
     */
    @TableLogic
    private Integer isdelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}