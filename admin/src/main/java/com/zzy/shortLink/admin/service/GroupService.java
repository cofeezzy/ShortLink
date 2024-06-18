package com.zzy.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.shortLink.admin.dao.entity.GroupDO;
import com.zzy.shortLink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.zzy.shortLink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */

public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     *
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);

    /**
     * 获取短链接分组列表
     * @return
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 更新短链接分组
     * @param reqDTO
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO reqDTO);
}
