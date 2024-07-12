package com.zzy.shortLink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzy.shortLink.project.common.convention.result.Result;
import com.zzy.shortLink.project.common.convention.result.Results;
import com.zzy.shortLink.project.dto.req.RecycleBinRecoverDTO;
import com.zzy.shortLink.project.dto.req.RecycleBinSaveDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.zzy.shortLink.project.service.RecycleBinService;
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

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveDTO recycleBinSaveDTO){
        recycleBinService.saveRecycleBin(recycleBinSaveDTO);
        return Results.success();
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO){
        return Results.success(recycleBinService.pageShortLink(shortLinkRecycleBinPageReqDTO));
    }

    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverDTO recycleBinRecoverDTO){
        recycleBinService.recoverRecycleBin(recycleBinRecoverDTO);
        return Results.success();
    }
}
