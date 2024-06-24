package com.zzy.shortLink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zzy.shortLink.admin.common.convention.exception.ClientException;
import com.zzy.shortLink.admin.common.enums.UserErrorCodeEnum;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 用户信息传输过滤器
 *
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/shortLink/admin/v1/user/login",
            "/api/shortLink/admin/v1/user/has-username"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if(!IGNORE_URI.contains(requestURI)){
            String method = httpServletRequest.getMethod();
            if(!(Objects.equals(requestURI, "/api/shortLink/admin/v1/user") && Objects.equals(method, "POST"))){
                String username = httpServletRequest.getHeader("username");
                String token = httpServletRequest.getHeader("token");
                if(!StrUtil.isAllNotBlank(username, token)){
                    throw new ClientException(UserErrorCodeEnum.USER_TOKEN_FAIL);
                }
                Object userinfoJsonStr = stringRedisTemplate.opsForHash().get("login_" + username, token);
                if(userinfoJsonStr !=null){
                    UserInfoDTO userInfoDTO = JSON.parseObject(userinfoJsonStr.toString(), UserInfoDTO.class);
                    UserContext.setUser(userInfoDTO);
                }
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}
