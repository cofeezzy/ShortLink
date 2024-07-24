package com.zzy.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzy.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
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

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param accessRecordReqDTO 获取短链接指定时间内访问记录
     * @return 短链接访问数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO accessRecordReqDTO);

    /**
     * 获取短链接分组监控数据
     * @param shortLinkGroupStatsReqDTO
     * @return
     */
    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);
}
