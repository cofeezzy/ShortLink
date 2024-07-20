package com.zzy.shortLink.project.service;

import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接统计数据接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     * @param shortLinkStatsReqDTO 获取短链接数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO);
}
