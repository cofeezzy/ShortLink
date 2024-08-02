package com.zzy.shortLink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.common.convention.result.Results;
import com.zzy.shortLink.admin.dto.req.RecycleBinSaveDTO;
import com.zzy.shortLink.admin.remote.ShortLinkActualRemoteService;
import com.zzy.shortLink.admin.remote.dto.req.RecycleBinRecoverDTO;
import com.zzy.shortLink.admin.remote.dto.req.RecycleBinRemoveDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.zzy.shortLink.admin.service.RecycleBinService;
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

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 保存回收站
     * @param recycleBinSaveDTO
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveDTO recycleBinSaveDTO){
        shortLinkActualRemoteService.saveRecycleBin(recycleBinSaveDTO);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO){
        return recycleBinService.pageRecycleBinShortLink(shortLinkRecycleBinPageReqDTO);
    }

    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverDTO recycleBinRecoverDTO){
        shortLinkActualRemoteService.recoverRecycleBin(recycleBinRecoverDTO);
        return Results.success();
    }

    /**
     * 从回收站移除短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveDTO recycleBinRemoveDTO){
        shortLinkActualRemoteService.removeRecycleBin(recycleBinRemoveDTO);
        return Results.success();
    }
}
