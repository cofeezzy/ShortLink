package com.zzy.shortLink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.dto.req.ShortLinkGroupStatsReqDTO;
import com.zzy.shortLink.admin.remote.ShortLinkActualRemoteService;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        return shortLinkActualRemoteService.oneShortLinkStats(shortLinkStatsReqDTO.getStartDate(), shortLinkStatsReqDTO.getEndDate(), shortLinkStatsReqDTO.getFullShortUrl(), shortLinkStatsReqDTO.getGid());
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO) {
        return shortLinkActualRemoteService.groupShortLinkStats(shortLinkGroupStatsReqDTO.getStartDate(), shortLinkGroupStatsReqDTO.getEndDate(), shortLinkGroupStatsReqDTO.getGid());
    }

    /**
     * 访问分组短链接指定时间内访问记录数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO recordReqDTO) {
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(recordReqDTO.getStartDate(), recordReqDTO.getEndDate(), recordReqDTO.getFullShortUrl(), recordReqDTO.getGid());
    }

    /**
     * 访问分组短链接指定时间内访问记录数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO groupStatsAccessRecordReqDTO) {
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(groupStatsAccessRecordReqDTO.getStartDate(), groupStatsAccessRecordReqDTO.getEndDate(), groupStatsAccessRecordReqDTO.getGid());
    }


}
