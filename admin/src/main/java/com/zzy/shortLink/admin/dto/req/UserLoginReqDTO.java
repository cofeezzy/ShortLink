package com.zzy.shortLink.admin.dto.req;

import lombok.Data;

/**
 * 用户登录Req
 */
@Data
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
