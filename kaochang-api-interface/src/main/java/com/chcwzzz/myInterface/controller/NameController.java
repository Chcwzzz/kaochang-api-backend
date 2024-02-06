package com.chcwzzz.myInterface.controller;

import com.chcwzzz.myInterface.domain.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 名称API
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user) {
        return "POST 你的名字是：" + user.getUsername();
    }
}
