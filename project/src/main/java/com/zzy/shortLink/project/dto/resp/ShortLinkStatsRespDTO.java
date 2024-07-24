package com.zzy.shortLink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkStatsRespDTO {

    /**
     * 访问量 page View
     */
    private Integer pv;

    /**
     * 独立访问数
     */
    private Integer uv;

    /**
     * 独立ip数
     */
    private Integer uip;

    /**
     * 每日访问详情
     */
    private List<ShortLinkStatsAccessDailyRespDTO> daily;

    /**
     * 地区访问详情国内
     */
    private List<ShortLinkStatsLocaleCNRespDTO> localeCnStats;

    /**
     * 小时访问详情
     */
    private List<Integer> hourStats;

    /**
     * 高频访问IP详情
     */
    private List<ShortLinkStatsTopIpRespDTO> topIpStats;

    /**
     * 访问设备详情
     */
    private List<ShortLinkStatsDeviceRespDTO> deviceStats;

    /**
     * 浏览器访问详情
     */
    private List<ShortLinkStatsBrowserRespDTO> browserStats;

    /**
     * 一周访问详情
     */
    private List<Integer> weekdayStats;

    /**
     * 操作系统访问详情
     */
    private List<ShortLinkStatsOsRespDTO> osStats;

    /**
     * 网络访问详情
     */
    private List<ShortLinkStatsNetworkRespDTO> networkStats;

    /**
     * 访客详情
     */
    private List<ShortLinkStatsUvRespDTO> uvTypeStats;
}
