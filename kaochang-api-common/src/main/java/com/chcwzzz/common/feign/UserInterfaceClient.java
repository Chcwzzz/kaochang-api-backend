package com.chcwzzz.common.feign;

import com.chcwzzz.common.model.entity.UserInterfaceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author 烤肠
 * @Date 2024/3/5 17:10
 */
@FeignClient(name = "kaochang-api-backend-userinterface", url = "http://127.0.0.1:8101/admin/userInterfaceInfo/")
public interface UserInterfaceClient {
    /**
     * 根据用户id和接口id查询用户对该接口的相关信息
     *
     * @param userId
     * @param interfaceId
     * @return
     */
    @GetMapping("/getUserLeftInvokes")
    public UserInterfaceInfo getUserLeftInvokes(@RequestParam Long userId, @RequestParam Long interfaceId);
}
