package com.zzy.shortLink.admin.remote.dto.req;

import lombok.Data;

/**
 * 回收站回收功能参数
 */
@Data
public class RecycleBinRemoveDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
