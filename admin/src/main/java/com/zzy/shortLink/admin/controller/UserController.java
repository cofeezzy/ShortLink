package com.zzy.shortLink.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
public class UserController {
    /**
     *根据用户名获得用户信息
     */
    @GetMapping("/api/shortLink/v1/user/{username}")
    public String getUserByUsername(@PathVariable("username") String username){
        return "Hi " + username;
    }
}
