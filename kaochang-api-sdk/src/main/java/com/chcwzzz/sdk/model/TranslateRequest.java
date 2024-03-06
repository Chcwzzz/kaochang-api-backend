package com.chcwzzz.sdk.model;

/**
 * @Author 烤肠
 * @Date 2024/3/4 17:40
 */

import lombok.Data;

/**
 * 翻译请求类
 */
@Data
public class TranslateRequest {
    /**
     * 翻译内容
     */
    private String message ;
    /**
     * 请输入翻译类型（1代表中-英，2代表英-中，3代表中<=>英【自动检测翻译】）
     */
    private Integer type;
}
