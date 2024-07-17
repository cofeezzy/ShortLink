package com.zzy.shortLink.admin.dto.req;

import lombok.Data;

/**
 * 回收站保存功能
 */
@Data
public class RecycleBinSaveDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}