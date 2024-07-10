package com.zzy.shortLink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dto.req.RecycleBinSaveDTO;

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
}
