package com.chcwzzz.myInterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.chcwzzz.common.common.BaseResponse;
import com.chcwzzz.common.common.ErrorCode;
import com.chcwzzz.common.common.ResultUtils;
import com.chcwzzz.common.model.TranslateRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 烤肠
 * @Date 2024/3/4 17:31
 */
@RestController
@Slf4j
@RequestMapping("/interface")
public class InterfaceController {


    @PostMapping("/translate")
    public BaseResponse translateInterface(@RequestBody TranslateRequest translateRequest) {
        String url = "https://v.api.aa1.cn/api/api-fanyi-yd/index.php";
        String message = translateRequest.getMessage();
        Integer type = translateRequest.getType();
        if (StringUtils.isBlank(message) || type == null || type < 0 || type > 3) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        url = url + "?msg=" + message + "&type=" + type;
        HttpResponse execute = HttpRequest
                .get(url)
                .execute();
        if (HttpStatus.HTTP_OK == execute.getStatus()) {
            //响应成功
            String body = execute.body();
            return ResultUtils.success(body);
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "接口请求失败，请稍后再试");
        }
    }
}
