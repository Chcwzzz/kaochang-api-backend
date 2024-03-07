package com.chcwzzz.myInterface.aop;

import com.chcwzzz.myInterface.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求校验
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RequestInterceptor {
    private final UserService userService;
    private static final String DYE_DATA_HEADER = "X-Dye-Data";
    private static final String DYE_DATA_VALUE = "chcwzzz";

    /**
     * 执行拦截
     */
    @Before("execution(* com.chcwzzz.myInterface.controller.*.*(..))")
    public void doInterceptor() throws Throwable {
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        //判断是否是网关发来的经过染色后的请求，只有网关发来的请求才可以调用服务
        String dye_data_header = httpServletRequest.getHeader(DYE_DATA_HEADER);
        if (StringUtils.isBlank(dye_data_header) || !StringUtils.equals(dye_data_header, DYE_DATA_VALUE)) {
            throw new RuntimeException("无权限");
        }
    }
}
