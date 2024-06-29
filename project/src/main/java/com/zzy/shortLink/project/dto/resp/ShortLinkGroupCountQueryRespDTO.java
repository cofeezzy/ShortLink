package com.zzy.shortLink.project.dto.resp;

import lombok.Data;

/**
 * 短链接分组查询返回实体
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {

    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 短链接数量
     */
    private Integer shortLinkCount;
}
