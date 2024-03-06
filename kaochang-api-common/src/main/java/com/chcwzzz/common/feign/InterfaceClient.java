package com.chcwzzz.common.feign;

import com.chcwzzz.common.model.entity.InterfaceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author 烤肠
 * @Date 2024/3/5 16:03
 */
@FeignClient(name = "kaochang-api-backend-interface", url = "http://127.0.0.1:8101/admin/interfaceInfo/")
public interface InterfaceClient {

    /**
     * 根据url和method查询接口是否存在
     * @param url
     * @param method
     * @return
     */
    @GetMapping("/getInterfaceInfoByUrlAndMethod")
    public InterfaceInfo getInterfaceInfoByUrlAndMethod(@RequestParam String url, @RequestParam String method);
}
