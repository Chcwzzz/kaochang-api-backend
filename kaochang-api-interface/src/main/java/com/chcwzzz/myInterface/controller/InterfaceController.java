package com.chcwzzz.myInterface.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chcwzzz.common.common.BaseResponse;
import com.chcwzzz.common.common.ErrorCode;
import com.chcwzzz.common.common.ResultUtils;
import com.chcwzzz.common.model.TranslateRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 返回随机毒鸡汤
     *
     * @param charset 返回编码类型 [gbk|utf-8] 默认 utf-8
     * @param encode  返回格式类型 [text|js|json] 默认 text
     * @return
     */
    @GetMapping("/yan")
    public BaseResponse yanInterface(String charset, String encode) {
        String url = "https://api.btstu.cn/yan/api.php";
        if (StringUtils.isAnyBlank(charset, encode)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        url = url + "?charset=" + charset + "&encode=" + encode;
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

    /**
     * 全国天气获取
     *
     * @param city
     * @return
     */
    @GetMapping("/weather")
    public BaseResponse weatherInterface(String city) {
        String url = "https://www.apii.cn/api/weather/";
        if (StringUtils.isAnyBlank(city)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        url = url + "?city=" + city;
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

    /**
     * 查看各大榜单的今日热榜排行（哔哩哔哩，百度，知乎，百度贴吧，少数派，IT 之家，澎湃新闻，今日头条，微博热搜，36 氪，稀土掘金，腾讯新闻）
     *
     * @param title （哔哩哔哩，百度，知乎，百度贴吧，少数派，IT之家，澎湃新闻，今日头条，微博热搜，36氪，稀土掘金，腾讯新闻）
     * @return
     */
    @GetMapping("/dailyhot")
    public Object dailyhotInterface(String title) {
        String url = "https://api.pearktrue.cn/api/dailyhot/";
        if (StringUtils.isBlank(title)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        url = url + "?title=" + title;
        HttpResponse execute = HttpRequest
                .get(url)
                .execute();
        if (HttpStatus.HTTP_OK == execute.getStatus()) {
            //响应成功
            String body = execute.body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            List<Object> data = (List<Object>) jsonObject.get("data");
            List<Object> collect = data.stream().limit(10).collect(Collectors.toList());
            jsonObject.putOpt("data", collect);
            return jsonObject;
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "接口请求失败，请稍后再试");
        }
    }

    @GetMapping("/ip")
    public Object ipInterface(String ip) {
        String url = "https://api.songzixian.com/api/ip";
        if (StringUtils.isBlank(ip)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        url = url + "?dataSource=GLOBAL_IP&ip=" + ip;
        HttpResponse execute = HttpRequest
                .get(url)
                .execute();
        if (HttpStatus.HTTP_OK == execute.getStatus()) {
            //响应成功
            String body = execute.body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            Object data = jsonObject.get("data");
            return data;
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "接口请求失败，请稍后再试");
        }
    }
}
