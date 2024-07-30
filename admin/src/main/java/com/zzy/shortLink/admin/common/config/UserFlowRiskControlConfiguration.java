package com.zzy.shortLink.admin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用户流量风控配置文件
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.flow-limit")
public class UserFlowRiskControlConfiguration {

    /**
     * 是否开启
     */
    private Boolean enable;

    /**
     * 时间窗口
     */
    private String timeWindow;

    /**
     * 时间窗口内最大访问次数
     */
    private Integer maxAccessCount;
}
