package com.chcwzzz.sdk.client;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.chcwzzz.sdk.model.TranslateRequest;
import com.chcwzzz.sdk.utils.SignUtils;
import com.chcwzzz.sdk.model.DevRequest;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
public class KaochangClient {

    private final String accessKey;
    private final String secretKey;
    public static final String GATEWAY_HOST = "127.0.0.1:8888";

    /**
     * 发送请求，返回响应体
     *
     * @param devRequest
     * @return 返回响应体
     */
    public String request(DevRequest devRequest) {
        String url = devRequest.getUrl();
        String method = devRequest.getMethod();
        if ("POST".equals(method)) {
            String jsonStr = JSONUtil.toJsonStr(devRequest.getBody());
            Map<String, String> headerMap = getHeaderMap(jsonStr);
            return HttpRequest
                    .post(URLUtil.normalize(url))
                    .addHeaders(headerMap)
                    .body(jsonStr)
                    .execute()
                    .body();
        } else if ("GET".equals(method)) {
            String jsonStr = JSONUtil.toJsonStr(devRequest.getBody());
            Map<String, String> headerMap = getHeaderMap(jsonStr);
            if (!"null".equals(devRequest.getBody())) {
                Map<String, Object> params = (Map<String, Object>) devRequest.getBody();
                if (CollUtil.isNotEmpty(params)) {
                    boolean isFirst = true;
                    StringBuilder stringBuilder = new StringBuilder(url);
                    for (Map.Entry<String, Object> stringObjectEntry : params.entrySet()) {
                        String key = stringObjectEntry.getKey();
                        Object value = stringObjectEntry.getValue();
                        if (isFirst) {
                            stringBuilder.append("?" + key + "=" + value);
                            isFirst = false;
                        } else {
                            stringBuilder.append("&" + key + "=" + value);
                        }
                    }
                    url = stringBuilder.toString();
                }
            }
            return HttpRequest
                    .get(URLUtil.normalize(url))
                    .addHeaders(headerMap)
                    .execute()
                    .body();
        }
        return "请求方式暂不支持";
    }

    /**
     * 翻译接口
     *
     * @param message 翻译内容
     * @param type    1代表中-英，2代表英-中，3代表中<=>英【自动检测翻译】
     * @return
     */
    public String translateInterface(String message, Integer type) {
        String url = GATEWAY_HOST + "/api/interface/translate";
        TranslateRequest translateRequest = new TranslateRequest();
        translateRequest.setMessage(message);
        translateRequest.setType(type);
        String body = JSONUtil.toJsonStr(translateRequest);
        return HttpRequest
                .post(URLUtil.normalize(url))
                .body(body)
                .addHeaders(getHeaderMap(body))
                .execute()
                .body();
    }

    /**
     * 构建请求头保存的数据
     *
     * @param body 用户请求的方法体
     * @return
     */
    private Map<String, String> getHeaderMap(String body) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("accessKey", accessKey);
        paramMap.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
        paramMap.put("body", URLUtil.encode(body, CharsetUtil.CHARSET_UTF_8));
        paramMap.put("nonce", RandomUtil.randomNumbers(4));
        paramMap.put("sign", SignUtils.getSign(body, secretKey));
        return paramMap;
    }
}
