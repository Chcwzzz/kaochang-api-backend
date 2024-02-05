package com.chcwzzz.sdk.client;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.chcwzzz.sdk.utils.SignUtils;
import com.chcwzzz.sdk.model.DevRequest;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class KaochangClient {

    private final String accessKey;
    private final String secretKey;

    /**
     * 发送POST请求，返回响应体
     *
     * @param devRequest
     * @return 返回响应体
     */
    public String doPost(DevRequest devRequest) {
        String url = devRequest.getUrl();
        String jsonStr = JSONUtil.toJsonStr(devRequest.getBody());
        Map<String, String> headerMap = getHeaderMap(jsonStr);
        return HttpRequest
                .post(URLUtil.normalize(url))
                .addHeaders(headerMap)
                .body(jsonStr)
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
        //paramMap.put("nonce", RandomUtil.randomNumbers(100));
        paramMap.put("nonce", RandomUtil.randomNumbers(4));
        paramMap.put("sign", SignUtils.getSign(body, secretKey));
        return paramMap;
    }
}
