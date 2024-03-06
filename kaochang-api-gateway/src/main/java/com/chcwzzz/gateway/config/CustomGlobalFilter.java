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
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
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
        return handleResponse(exchange, chain, userInterfaceInfo);
    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, UserInterfaceInfo userInterfaceInfo) {
        try {
            // 从交换机拿到原始response
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓冲区工厂 拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到状态码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        // 对象是响应式的
                        if (body instanceof Flux) {
                            // 我们拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里面写数据
                            // 拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // 7. 调用成功，接口调用次数+1
                                try {
                                    userInterfaceClient.invokeUserInterfaceCount(userInterfaceInfo);
                                } catch (Exception e) {
                                    log.error("invokeInterfaceCount error", e);
                                }
                                // data从这个content中读取
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);// 释放掉内存
                                // 6.构建日志
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);// data
                                rspArgs.add(data);
                                log.info("<--- status:{} data:{}"// data
                                        , rspArgs.toArray());// log.info("<-- {} {}", originalResponse.getStatusCode(), data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8.调用失败返回错误状态码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);// 降级处理返回数据
        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }

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
        return -2;
    }
}