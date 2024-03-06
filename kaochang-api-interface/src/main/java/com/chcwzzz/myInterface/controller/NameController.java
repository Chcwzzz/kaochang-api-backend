package com.chcwzzz.myInterface.controller;

import com.chcwzzz.myInterface.domain.User;
import org.springframework.web.bind.annotation.*;

/**
 * 名称API
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user) {
        return "POST 你的名字是：" + user.getUserName();
    }

    @GetMapping
    public String getNameByGet(String name) {
        return "GET 你的name是 " + name;
    }
}
