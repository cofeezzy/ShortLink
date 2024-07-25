package com.zzy.shortLink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接基础信息响应参数
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ShortLinkBaseInfoRespDTO {

    /**
     * 全部短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 描述
     */
    private String describe;
}
