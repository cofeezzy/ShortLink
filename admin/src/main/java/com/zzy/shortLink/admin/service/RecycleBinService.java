package com.zzy.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * URL 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     * @param shortLinkRecycleBinPageReqDTO 分页查询短链接请求参数
     * @return 返回参数包装
     */
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO);
}
