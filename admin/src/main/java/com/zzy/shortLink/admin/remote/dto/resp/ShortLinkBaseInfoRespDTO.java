package com.zzy.shortLink.admin.remote.dto.resp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接基础信息响应参数
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ShortLinkBaseInfoRespDTO {

    /**
     * 全部短链接
     */
    @ExcelProperty("短链接")
    @ColumnWidth(40)
    private String fullShortUrl;

    /**
     * 原始链接
     */
    @ExcelProperty("原始链接")
    @ColumnWidth(40)
    private String originUrl;

    /**
     * 描述
     */
    @ExcelProperty("标题")
    @ColumnWidth(40)
    private String describe;
}
