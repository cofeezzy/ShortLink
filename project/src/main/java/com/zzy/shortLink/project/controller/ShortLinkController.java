package com.zzy.shortLink.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzy.shortLink.project.common.convention.result.Result;
import com.zzy.shortLink.project.common.convention.result.Results;
import com.zzy.shortLink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.zzy.shortLink.project.handler.CustomBlockHandler;
import com.zzy.shortLink.project.service.ShortLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 短链接跳转原始链接
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response){
        shortLinkService.restoreUri(shortUri, request, response);
    }

    /**
     * 创建
     */
    @PostMapping("/api/short-link/v1/create")
    @SentinelResource(
            value = "create_short_Link",
            blockHandlerClass = CustomBlockHandler.class,
            blockHandler = "createShortLinkBlockHandlerMethod"
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO){
        return Results.success(shortLinkService.createShortLink(shortLinkCreateReqDTO));
    }

    /**
     * 批量创建
     */
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> createShortLinkBatch(@RequestBody ShortLinkBatchCreateReqDTO batchCreateReqDTO){
        return Results.success(shortLinkService.createShortLinkBatch(batchCreateReqDTO));
    }

    /**
     * 修改短链接
     *
     * @param reqDTO
     * @return
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO reqDTO) {
        shortLinkService.updateShortLink(reqDTO);
        return Results.success();
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        return Results.success(shortLinkService.pageShortLink(shortLinkPageReqDTO));
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam List<String> requestParam){
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }
}
