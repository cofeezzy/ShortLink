package com.zzy.shortLink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkCreateRespDTO;

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
}
