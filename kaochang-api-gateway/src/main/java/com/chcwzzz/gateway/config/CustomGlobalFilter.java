package com.chcwzzz.gateway.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.chcwzzz.common.feign.InterfaceClient;
import com.chcwzzz.common.feign.UserClient;
import com.chcwzzz.common.feign.UserInterfaceClient;
import com.chcwzzz.common.model.entity.InterfaceInfo;
import com.chcwzzz.common.model.entity.User;
import com.chcwzzz.common.model.entity.UserInterfaceInfo;
import com.chcwzzz.sdk.utils.SignUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    private Map<String, String> nonceMap = new HashMap<>();
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");
    private final UserClient userClient;
    private final InterfaceClient interfaceClient;
    private final UserInterfaceClient userInterfaceClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        log.info("请求id: {}", request.getId());
        log.info("请求路径: {}", request.getPath());
        log.info("请求URI: {}", request.getURI());
        log.info("请求方法: {}", request.getMethod());
        log.info("请求参数: {}", request.getQueryParams());
        log.info("请求头: {}", request.getHeaders());
        log.info("请求Remote地址: {}", request.getRemoteAddress().getHostString());
        log.info("请求Local地址: {}", request.getLocalAddress());
        log.info("格式化请求Local地址: {}", URLUtil.normalize(request.getLocalAddress().toString()) + request.getPath());

        //2.黑白名单过滤
        if (!IP_WHITE_LIST.contains(request.getRemoteAddress().getHostString())) {
            //拦截请求不在IP白名单中的地址
            return handlerReject(exchange);
        }
        //3.用户鉴权（AK、SK）
        HttpHeaders headers = request.getHeaders();
        String accessKeyHeader = headers.getFirst("accessKey");
        String body = headers.getFirst("body");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        if (StringUtils.isAnyBlank(accessKeyHeader, body, nonce, timestamp, sign)) {
            return handlerReject(exchange);
        }

        //3.1 去数据库中查询ak和sk
        User dbUser = userClient.getUserByAK(accessKeyHeader);
        String dBAccessKey = dbUser.getAccessKey();
        String dBSecretKey = dbUser.getSecretKey();

        //3.2 校验ak
        if (!accessKeyHeader.equals(dBAccessKey)) {
            return handlerReject(exchange);
        }
        //3.3 校验nonce是否使用过
        String usedNonce = nonceMap.get("nonce");
        if (StrUtil.isNotBlank(usedNonce) && usedNonce.equals(nonce)) {
            return handlerReject(exchange);
        }
        nonceMap.put("nonce", nonce);

        //3.4 校验timestamp是否超时
        //获取5分钟前的时间戳
        Instant now = Instant.now().minus(5, ChronoUnit.MINUTES);
        //获取传递的时间戳
        Instant instant = null;
        if (timestamp != null) {
            instant = Instant.ofEpochSecond(Long.parseLong(timestamp));
        }
        //超时
        if (instant != null && now.isAfter(instant)) {
            return handlerReject(exchange);
        }

        //3.5 校验签名
        String sign1 = SignUtils.getSign(URLUtil.decode(body, StandardCharsets.UTF_8), dBSecretKey);
        if (!sign1.equals(sign)) {
            return handlerReject(exchange);
        }
        //4.判断接口是否存在（feign远程调用）
        String requestUrl = URLUtil.normalize(request.getLocalAddress().toString()) + request.getPath();
        String methodValue = request.getMethodValue();
        InterfaceInfo interfaceInfo = interfaceClient.getInterfaceInfoByUrlAndMethod(requestUrl, methodValue);
        if (Objects.isNull(interfaceInfo)) {
            return handleResponse("请求接口不存在", response);
        }
        //4.1 用户是否还有调用次数
        UserInterfaceInfo userInterfaceInfo = userInterfaceClient.getUserLeftInvokes(dbUser.getId(), interfaceInfo.getId());
        if (userInterfaceInfo == null) {
            return handleResponse("用户未开通该接口", response);
        }
        if (userInterfaceInfo.getUserleftinvokes() <= 0) {
            return handleResponse("接口可调用次数耗尽，请重新开通接口", response);
        }
        //5.请求转发，调用接口
        Mono<Void> filter = chain.filter(exchange);
        //6.响应日志
        log.info("响应状态码：{}", response.getStatusCode());
        if (HttpStatus.OK.equals(response.getStatusCode())) {
            //7.调用成功，接口调用次数 + 1

        } else {
            //8.调用失败，返回规范错误码
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
        return filter;
    }

    private static Mono<Void> handleResponse(String message, ServerHttpResponse response) {
        byte[] bytes = JSONUtil.toJsonStr(message).getBytes(StandardCharsets.UTF_8);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 拦截请求
     *
     * @param exchange
     * @return
     */
    private static Mono<Void> handlerReject(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setRawStatusCode(HttpStatus.UNAUTHORIZED.value());
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        // 过滤器执行顺序，值越小，优先级越高
        return 0;
    }
}