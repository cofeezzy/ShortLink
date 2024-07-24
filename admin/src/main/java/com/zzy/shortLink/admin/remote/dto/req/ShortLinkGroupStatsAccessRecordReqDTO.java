package com.zzy.shortLink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分组短链接访问记录请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page {

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 分组标识
     */
    private String gid;
}
