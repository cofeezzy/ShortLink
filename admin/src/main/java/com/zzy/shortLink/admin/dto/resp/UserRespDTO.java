package com.zzy.shortLink.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zzy.shortLink.admin.common.serialize.PhoneDesensitizationSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户返回参数响应
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRespDTO {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;


    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)
    private String phone;

    /**
     * 邮箱
     */
    private String mail;



}
