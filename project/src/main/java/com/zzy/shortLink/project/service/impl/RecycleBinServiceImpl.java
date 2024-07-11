package com.zzy.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dao.mapper.ShortLinkMapper;
import com.zzy.shortLink.project.dto.req.RecycleBinSaveDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.zzy.shortLink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.zzy.shortLink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 *  回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveRecycleBin(RecycleBinSaveDTO recycleBinSaveDTO) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, recycleBinSaveDTO.getFullShortUrl())
                .eq(ShortLinkDO::getGid, recycleBinSaveDTO.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, recycleBinSaveDTO.getFullShortUrl()));
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkPageReqDTO.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(shortLinkPageReqDTO, queryWrapper);
        return resultPage.convert(each ->{
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

}
