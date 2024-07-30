package com.zzy.shortLink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zzy.shortLink.admin.common.convention.exception.ClientException;
import com.zzy.shortLink.admin.common.convention.result.Results;
import com.zzy.shortLink.admin.common.enums.UserErrorCodeEnum;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
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
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/v1/user/has-username"
    );

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if(!IGNORE_URI.contains(requestURI)){
            String method = httpServletRequest.getMethod();
            if(!(Objects.equals(requestURI, "/api/short-link/admin/v1/user") && Objects.equals(method, "POST"))){
                String username = httpServletRequest.getHeader("username");
                String token = httpServletRequest.getHeader("token");
                if(!StrUtil.isAllNotBlank(username, token)){
                    returnJson((HttpServletResponse)servletResponse, JSON.toJSONString(Results.failure(new ClientException(UserErrorCodeEnum.USER_TOKEN_FAIL))));
                    return;
                }
                Object userinfoJsonStr;
                try{
                    userinfoJsonStr = stringRedisTemplate.opsForHash().get("login_" + username, token);
                    if(userinfoJsonStr == null){
                        throw new ClientException(UserErrorCodeEnum.USER_TOKEN_FAIL);
                    }
                }catch (Exception ex){
                    returnJson((HttpServletResponse)servletResponse, JSON.toJSONString(Results.failure(new ClientException(UserErrorCodeEnum.USER_TOKEN_FAIL))));
                    return;
                }
                UserInfoDTO userInfoDTO = JSON.parseObject(userinfoJsonStr.toString(), UserInfoDTO.class);
                UserContext.setUser(userInfoDTO);
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    /**
     * 临时代码，返回客户端数据，因为全局异常拦截器无法拦截filter中的异常，此处先用这个，后续网关处理
     */
    private void returnJson(HttpServletResponse response, String json) throws Exception{
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try{
            writer = response.getWriter();
            writer.print(json);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(writer != null){
                writer.close();
            }
        }
    }
}
