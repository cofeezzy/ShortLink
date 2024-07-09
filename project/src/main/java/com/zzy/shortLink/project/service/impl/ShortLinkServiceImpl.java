package com.zzy.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.shortLink.project.common.convention.exception.ServiceException;
import com.zzy.shortLink.project.common.enums.ValiDateTypeEnum;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dao.entity.ShortLinkGoTODO;
import com.zzy.shortLink.project.dao.mapper.ShortLinkGoToMapper;
import com.zzy.shortLink.project.dao.mapper.ShortLinkMapper;
import com.zzy.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zzy.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.zzy.shortLink.project.service.ShortLinkService;
import com.zzy.shortLink.project.toolkit.HashUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.zzy.shortLink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;
import static com.zzy.shortLink.project.common.constant.RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCacheBloomFilter;

    private final ShortLinkGoToMapper shortLinkGoToMapper;
    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissionClient;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO) {
        String shortLinkSuffix = generateSuffix(reqDTO);
        String fullShortUrl = reqDTO.getDomain() + "/" + shortLinkSuffix;
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .fullShortUrl(fullShortUrl)
                .domain(reqDTO.getDomain())
                .originUrl(reqDTO.getOriginUrl())
                .shortUri(shortLinkSuffix)
                .gid(reqDTO.getGid())
                .createdType(reqDTO.getCreatedType())
                .validDateType(reqDTO.getValidDateType())
                .validDate(reqDTO.getValidDate())
                .enableStatus(0)
                .describe(reqDTO.getDescribe())
                .delTime(0L)
                .build();

        ShortLinkGoTODO shortLinkGoTO = ShortLinkGoTODO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(reqDTO.getGid())
                .build();
        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGoToMapper.insert(shortLinkGoTO);
        }catch (DuplicateKeyException ex){
            throw new ServiceException(String.format("短链接: %s 生成重复", fullShortUrl));
        }
        shortUriCreateCacheBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(reqDTO.getOriginUrl())
                .gid(reqDTO.getGid())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO reqDTO) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, reqDTO.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, reqDTO.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if(hasShortLinkDO == null){
            throw new ServiceException("短链接记录不存在");
        }
        if(Objects.equals(hasShortLinkDO.getGid(), reqDTO.getGid())){
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, reqDTO.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, reqDTO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(reqDTO.getValidDateType(), ValiDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(hasShortLinkDO.getFavicon())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(reqDTO.getGid())
                    .originUrl(reqDTO.getOriginUrl())
                    .describe(reqDTO.getDescribe())
                    .validDateType(reqDTO.getValidDateType())
                    .validDate(reqDTO.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        }else{
            LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, reqDTO.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelTime, 0L);
            ShortLinkDO delShortLinkDO = ShortLinkDO.builder().delTime(System.currentTimeMillis())
                    .build();
            delShortLinkDO.setDelFlag(1);
            baseMapper.update(delShortLinkDO, linkUpdateWrapper);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(hasShortLinkDO.getFavicon())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(reqDTO.getGid())
                    .originUrl(reqDTO.getOriginUrl())
                    .describe(reqDTO.getDescribe())
                    .validDateType(reqDTO.getValidDateType())
                    .validDate(reqDTO.getValidDate())
                    .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                    .delTime(0L)
                    .build();
            baseMapper.insert(shortLinkDO);
        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkPageReqDTO.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(shortLinkPageReqDTO, queryWrapper);
        return resultPage.convert(each ->{
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @SneakyThrows
    @Override
    public void restoreUri(String shortUri, ServletRequest request, ServletResponse response) {
        //判断是不是在布隆过滤器
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if(StrUtil.isNotBlank(originalLink)){
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        RLock lock = redissionClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try{
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if(StrUtil.isNotBlank(originalLink)){
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            LambdaQueryWrapper<ShortLinkGoTODO> shortLinkGoTOQueryWrapper = Wrappers.lambdaQuery(ShortLinkGoTODO.class)
                    .eq(ShortLinkGoTODO::getFullShortUrl, fullShortUrl);
            ShortLinkGoTODO shortLinkGoTODO = shortLinkGoToMapper.selectOne(shortLinkGoTOQueryWrapper);
            if(shortLinkGoTODO == null){
                // 此处应该封控
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGoTODO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if(shortLinkDO != null){
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), shortLinkDO.getOriginUrl());
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
            }
        }finally {
            lock.unlock();
        }
    }

    private String generateSuffix(ShortLinkCreateReqDTO reqDTO){
        int customGenerateCount = 0;
        String shortUri;
        while(true){
            if(customGenerateCount > 10){
                throw new ServiceException("短链接生成频繁，请稍后再试");
            }
            String originalUrl = reqDTO.getOriginUrl();
            originalUrl += System.currentTimeMillis(); //最大化避免冲突
            shortUri = HashUtil.hashToBase62(originalUrl);
            if(!shortUriCreateCacheBloomFilter.contains(reqDTO.getDomain() + "/" + shortUri)){
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
