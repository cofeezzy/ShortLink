package com.zzy.shortLink.project.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ShortLinkBatchCreateReqDTO {

    /**
     * 原始链接
     */
    List<String> originUrls;

    /**
     * 描述
     */
    List<String> describes;

    /**
     * 分组id
     */
    String gid;

    /**
     * 创建类型
     */
    Integer createdType;

    /**
     * 有效期 0：永久 1：自定义
     */
    Integer validDateType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validDate;
}
