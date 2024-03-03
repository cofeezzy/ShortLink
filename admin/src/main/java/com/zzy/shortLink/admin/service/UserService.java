package com.zzy.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.shortLink.admin.dao.entity.UserDO;
import com.zzy.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.zzy.shortLink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return 用户名存在返回True，用户名不存在返回False
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     *
     * @param requestParam 用户请求参数 如果用userRegisterReq这种对象的语义杂合方法不好记忆，因为往后还有很多对象。
     */
    void  Register(UserRegisterReqDTO requestParam);

}
