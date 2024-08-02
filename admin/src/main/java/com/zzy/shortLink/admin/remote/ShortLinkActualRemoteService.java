package com.zzy.shortLink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.dto.req.RecycleBinSaveDTO;
import com.zzy.shortLink.admin.remote.dto.req.*;
import com.zzy.shortLink.admin.remote.dto.resp.*;
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
    Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam);

    /**
     *
     * @param url 目标网站地址
     * @return 网站标题
     */
    @GetMapping("/api/short-link/v1/title")
    Result<String> getTitleByUrl(@RequestParam("url") String url);

    /**
     *  回收站保存
     * @param recycleBinSaveDTO
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    void saveRecycleBin(RecycleBinSaveDTO recycleBinSaveDTO);

    /**
     * 恢复短链接
     * @param recycleBinRecoverDTO 短链接恢复请求参数
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    void recoverRecycleBin(RecycleBinRecoverDTO recycleBinRecoverDTO);

    /**
     * 从回收站移除短链接
     * @param recycleBinRemoveDTO 短链接移除请求参数
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    void removeRecycleBin(RecycleBinRemoveDTO recycleBinRemoveDTO);

    /**
     * 分页查询回收站短链接
     * @param gidList 分页标识
     * @param current 当前页
     * @param size 当前数据多少
     * @return
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@RequestParam("gidList") List<String> gidList,
                                                                @RequestParam("current")Long current,
                                                                @RequestParam("size")Long size);

    /**
     *  访问单个短链接指定时间内监控数据
     *  @param startDate 开始时间
     *  @param endDate 结束时间
     *  @param fullShortUrl 短链接
     *  @param gid 分组标识
     *  @return
     */
    @GetMapping("/api/short-link/v1/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(@RequestParam("startDate") String startDate,
                                                    @RequestParam("endDate") String endDate,
                                                    @RequestParam("fullShortUrl")String fullShortUrl,
                                                    @RequestParam("gid") String gid);

    /**
     *  访问分组短链接指定时间内监控数据
     *  @param startDate 开始时间
     *  @param endDate 结束时间
     *  @param gid 分组标识
     * @return
     */
    @GetMapping("/api/short-link/v1/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(@RequestParam("startDate") String startDate,
                                                      @RequestParam("endDate") String endDate,
                                                      @RequestParam("gid") String gid);

    /**
     *  访问短链接指定时间内访问数据
     *  @param startDate 开始时间
     *  @param endDate 结束时间
     *  @param fullShortUrl 短链接
     *  @param gid 分组标识
     * @return
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestParam("startDate") String startDate,
                                                                                @RequestParam("endDate") String endDate,
                                                                                @RequestParam("fullShortUrl")String fullShortUrl,
                                                                                @RequestParam("gid") String gid);

    /**
     *  访问分组短链接指定时间内访问数据
     *  @param startDate 开始时间
     *  @param endDate 结束时间
     *  @param gid 分组标识
     * @return
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@RequestParam("startDate") String startDate,
                                                                                             @RequestParam("endDate") String endDate,
                                                                                             @RequestParam("gid") String gid);

}
