package com.zzy.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     *
     * @param reqDTO
     * @return
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO);

    /**
     * 修改短链接
     * @param reqDTO
     */
    void updateShortLink(ShortLinkUpdateReqDTO reqDTO);

    /**
     * 分页查询短链接
     * @param shortLinkPageReqDTO 请求参数
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO);

    /**
     * 统计短链接分组内数量
     * @param requestParam 查询短链接分组内数量请求参数
     * @return
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);


    /**
     * 短链接跳转
     * @param shortUri 短链接后缀
     * @param request 请求
     * @param response 响应
     */
    void restoreUri(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 批量创建短链接
     * @param batchCreateReqDTO 批量创建短链接请求参数
     * @return 响应
     */
    ShortLinkBatchCreateRespDTO createShortLinkBatch(ShortLinkBatchCreateReqDTO batchCreateReqDTO);
}
