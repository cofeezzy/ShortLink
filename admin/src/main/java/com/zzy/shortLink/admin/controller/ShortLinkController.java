package com.zzy.shortLink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.common.convention.result.Results;
import com.zzy.shortLink.admin.remote.ShortLinkActualRemoteService;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkBaseInfoRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.zzy.shortLink.admin.toolkit.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 短链接后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO){
        return shortLinkActualRemoteService.createShortLink(shortLinkCreateReqDTO);
    }

    /**
     * 批量创建
     */
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void createShortLink(@RequestBody ShortLinkBatchCreateReqDTO batchCreateReqDTO, HttpServletResponse response){
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualRemoteService.batchCreateShortLink(batchCreateReqDTO);
        if(shortLinkBatchCreateRespDTOResult.isSuccess()){
            List<ShortLinkBaseInfoRespDTO> baseInfoRespDTOS = shortLinkBatchCreateRespDTOResult.getData().getBaseInfoList();
            EasyExcelWebUtil.write(response, "短链接批量创建", ShortLinkBaseInfoRespDTO.class, baseInfoRespDTOS);
        }
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO reqDTO){
        shortLinkActualRemoteService.updateShortLink(reqDTO);
        return Results.success();
    }

     /**
      * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        return shortLinkActualRemoteService.pageShortLink(shortLinkPageReqDTO.getGid(), shortLinkPageReqDTO.getOrderTag(),shortLinkPageReqDTO.getCurrent(),shortLinkPageReqDTO.getSize());
    }


}
