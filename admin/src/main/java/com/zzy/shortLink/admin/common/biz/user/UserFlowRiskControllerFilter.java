package com.zzy.shortLink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.zzy.shortLink.admin.common.config.UserFlowRiskControlConfiguration;
import com.zzy.shortLink.admin.common.convention.exception.ClientException;
import com.zzy.shortLink.admin.common.convention.result.Results;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static com.zzy.shortLink.admin.common.convention.errorcode.BaseErrorCode.FLOW_LIMIT_ERROR;

@Slf4j
@RequiredArgsConstructor
public class UserFlowRiskControllerFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserFlowRiskControlConfiguration userFlowRiskControlConfiguration;

    private static final String USER_FLOW_RISK_LUA_SCRIPT_PATH = "lua/user_flow_risk_control.lua";


    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(USER_FLOW_RISK_LUA_SCRIPT_PATH)));
        redisScript.setResultType(Long.class);
        String username = Optional.ofNullable(UserContext.getUsername()).orElse("other");
        Long result;
        try{
            result = stringRedisTemplate.execute(redisScript, Lists.newArrayList(username), userFlowRiskControlConfiguration.getTimeWindow());
        }catch (Throwable ex){
            log.error("执行用户请求流量限制LUA脚本出错", ex);
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return;
        }
        if(result == null || result > userFlowRiskControlConfiguration.getMaxAccessCount()){
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception{
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try (PrintWriter writer = response.getWriter()){
            writer.print(json);
        }
    }
}
