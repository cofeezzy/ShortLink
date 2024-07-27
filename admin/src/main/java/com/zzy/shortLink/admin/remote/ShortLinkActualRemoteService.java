package com.zzy.shortLink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链接中台远程调用服务
 */
@FeignClient("short-link-project")
public interface ShortLinkActualRemoteService {

    /**
     * 创建短链接
     * @param shortLinkCreateReqDTO 创建短链接请求参数
     * @return
     */
    @PostMapping("/api/short-link/v1/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO);


    /**
     *  批量创建短链接
     * @param batchCreateReqDTO 创建短链接请求参数
     * @return
     */
    @PostMapping("/api/short-link/v1/create/batch")
    Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO batchCreateReqDTO);

    /**
     * 修改短链接
     * @param reqDTO 修改短链接请求参数
     * @return
     */
    @PostMapping("/api/short-link/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO reqDTO);


    /**
     * 分页查询短链接
     * @param gid 分页标识
     * @param orderTag 排序类型
     * @param current 当前页
     * @param size 当前数据多少
     * @return 查询短链接响应
     */
    @GetMapping("/api/short-link/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestParam("gid") String gid,
                                                     @RequestParam("orderTag")String orderTag,
                                                     @RequestParam("current")Long current,
                                                     @RequestParam("size")Long size);

    /**
     * 查询分组短链接总量
     * @param requestParam 分组短链接总量请求参数
     * @return 查询分组短链接总量响应
     */
    @GetMapping("/api/short-link/v1/count")
    Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam);




}
