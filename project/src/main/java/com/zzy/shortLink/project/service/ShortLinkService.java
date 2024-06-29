package com.zzy.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkPageRespDTO;

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
}
