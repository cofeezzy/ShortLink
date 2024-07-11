package com.zzy.shortLink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.common.convention.result.Results;
import com.zzy.shortLink.admin.dto.req.RecycleBinSaveDTO;
import com.zzy.shortLink.admin.remote.dto.ShortLinkRemoteService;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *  回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    /**
     * 后续需要重构成Spring cloud feign
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){
    };

    /**
     * 保存回收站
     * @param recycleBinSaveDTO
     * @return
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveDTO recycleBinSaveDTO){
        shortLinkRemoteService.saveRecycleBin(recycleBinSaveDTO);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        return shortLinkRemoteService.pageRecycleBinShortLink(shortLinkPageReqDTO);
    }
}
