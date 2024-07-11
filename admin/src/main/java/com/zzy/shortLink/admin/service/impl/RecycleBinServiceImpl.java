package com.zzy.shortLink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzy.shortLink.admin.common.biz.user.UserContext;
import com.zzy.shortLink.admin.common.convention.exception.ServiceException;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.dao.entity.GroupDO;
import com.zzy.shortLink.admin.dao.mapper.GroupMapper;
import com.zzy.shortLink.admin.remote.dto.ShortLinkRemoteService;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.zzy.shortLink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * URL 回收站接口实现类
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;

    /**
     * 后续需要重构成Spring cloud feign
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){
    };

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO shortLinkRecycleBinPageReqDTO) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if(!CollUtil.isNotEmpty(groupDOList)){
            throw new ServiceException("用户无分组信息");
        }
        shortLinkRecycleBinPageReqDTO.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkRemoteService.pageRecycleBinShortLink(shortLinkRecycleBinPageReqDTO);
    }
}
