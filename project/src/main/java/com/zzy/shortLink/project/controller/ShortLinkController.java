package com.zzy.shortLink.project.controller;

import com.zzy.shortLink.project.common.convention.result.Result;
import com.zzy.shortLink.project.common.convention.result.Results;
import com.zzy.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建
     * @return
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO){
        return Results.success(shortLinkService.createShortLink(shortLinkCreateReqDTO));
    }

}
