package com.zzy.shortLink.admin.controller;

import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.dto.req.ShortLinkGroupStatsReqDTO;
import com.zzy.shortLink.admin.remote.dto.ShortLinkRemoteService;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        return shortLinkRemoteService.oneShortLinkStats(shortLinkStatsReqDTO);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO) {
        return shortLinkRemoteService.groupShortLinkStats(shortLinkGroupStatsReqDTO);
    }


}
