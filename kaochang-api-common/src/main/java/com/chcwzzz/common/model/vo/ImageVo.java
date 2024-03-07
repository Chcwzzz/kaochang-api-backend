package com.chcwzzz.common.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传图片状态vo
 */
@Data
public class ImageVo implements Serializable {
    private static final long serialVersionUID = -4296258656223039373L;
    private String url;
}