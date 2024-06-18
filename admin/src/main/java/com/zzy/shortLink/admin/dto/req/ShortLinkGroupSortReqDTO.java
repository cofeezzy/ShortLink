package com.zzy.shortLink.admin.dto.req;

import lombok.Data;

/**
 * 分组排序参数
 */
@Data
public class ShortLinkGroupSortReqDTO {

    /**
     * 分组ID
     */
    private String gid;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
