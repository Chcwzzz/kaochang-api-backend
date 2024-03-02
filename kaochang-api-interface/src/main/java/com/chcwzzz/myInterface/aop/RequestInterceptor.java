package com.chcwzzz.myInterface.aop;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.chcwzzz.myInterface.domain.User;
import com.chcwzzz.myInterface.service.UserService;
import com.chcwzzz.myInterface.utils.SignUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求校验
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RequestInterceptor {
    private final UserService userService;

    private Map<String, String> nonceMap = new HashMap<>();

    /**
     * 执行拦截
     */
    @Before("execution(* com.chcwzzz.myInterface.controller.*.*(..))")
    public void doInterceptor() throws Throwable {
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        String accessKeyHeader = httpServletRequest.getHeader("accessKey");
        String body = httpServletRequest.getHeader("body");
        String nonce = httpServletRequest.getHeader("nonce");
        String timestamp = httpServletRequest.getHeader("timestamp");
        String sign = httpServletRequest.getHeader("sign");

        // todo 实际情况应该去数据库中查询ak和sk
        User dbUser = userService.lambdaQuery()
                .eq(User::getAccessKey, accessKeyHeader)
                .one();
        String dBAccessKey = dbUser.getAccessKey();
        String dBSecretKey = dbUser.getSecretKey();


        //1. 校验ak
        if (!accessKeyHeader.equals(dBAccessKey)) {
            throw new RuntimeException("无权限");
        }
        //2. 校验nonce是否使用过
        String usedNonce = nonceMap.get("nonce");
        if (StrUtil.isNotBlank(usedNonce) && usedNonce.equals(nonce)) {
            throw new RuntimeException("无权限");
        }
        nonceMap.put("nonce", nonce);

        //3. 校验timestamp是否超时
        //获取5分钟前的时间戳
        Instant now = Instant.now().minus(5, ChronoUnit.MINUTES);
        //获取传递的时间戳
        Instant instant = Instant.ofEpochSecond(Long.parseLong(timestamp));
        //超时
        if (now.isAfter(instant)) {
            throw new RuntimeException("无权限");
        }

        //4. 校验签名
        String sign1 = SignUtils.getSign(URLUtil.decode(body, StandardCharsets.UTF_8), dBSecretKey);
        if (!sign1.equals(sign)) {
            throw new RuntimeException("无权限");
        }
    }
}
