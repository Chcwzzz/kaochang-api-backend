package com.chcwzzz.common.feign;


import com.chcwzzz.common.model.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author 烤肠
 * @Date 2024/3/4 20:46
 */
@FeignClient(name = "kaochang-api-backend-user",url = "http://127.0.0.1:8101/admin")
public interface UserClient {
    /**
     * 根据accessKey查询用户信息
     * @param accessKey
     * @return
     */
    @GetMapping("/user/getUserByAK")
    public User getUserByAK(@RequestParam("accessKey") String accessKey);
}
