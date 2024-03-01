package com.zzy.shortLink.admin.controller;

import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.common.convention.result.Results;
import com.zzy.shortLink.admin.dto.resp.UserRespDTO;
import com.zzy.shortLink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     *根据用户名获得用户信息
     */
    @GetMapping("/api/shortLink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username){
        UserRespDTO result = userService.getUserByUsername(username);
//        if(result == null){
//            return new Result<UserRespDTO>().setCode(UserErrorCodeEnum.USER_NULL.code()).setMessage(UserErrorCodeEnum.USER_NULL.message());
//        }else {
            return Results.success(result);
            //Result<UserRespDTO>().setCode("0").setData(result);
        //}
    }
}
