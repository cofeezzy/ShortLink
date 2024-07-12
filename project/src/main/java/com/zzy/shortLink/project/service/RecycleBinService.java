package com.zzy.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dto.req.RecycleBinRecoverDTO;
import com.zzy.shortLink.project.dto.req.RecycleBinSaveDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkPageRespDTO;

/**
 *  回收站管理接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     *
     * @param recycleBinSaveDTO 请求参数
     */
    void saveRecycleBin(RecycleBinSaveDTO recycleBinSaveDTO);

    /**
     * 分页查询回收站短链接
     * @param shortLinkRecycleBinPageReqDTO 请求参数
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO);

    /**
     * 恢复短链接
     * @param recycleBinRecoverDTO 请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverDTO recycleBinRecoverDTO);
}
