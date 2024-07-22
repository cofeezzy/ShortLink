package com.zzy.shortLink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.project.dao.entity.LinkAccessLogDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接访问记录请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLinkStatsAccessRecordReqDTO extends Page<LinkAccessLogDO> {

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;
}
