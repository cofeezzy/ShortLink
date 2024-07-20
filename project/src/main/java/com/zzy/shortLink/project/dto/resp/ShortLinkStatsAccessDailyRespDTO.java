package com.zzy.shortLink.project.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 短链接基础访问数据响应参数
 */
@Data
public class ShortLinkStatsAccessDailyRespDTO {
    /**
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

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
}
