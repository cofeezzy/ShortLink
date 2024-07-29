package com.zzy.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.shortLink.project.common.convention.exception.ClientException;
import com.zzy.shortLink.project.common.convention.exception.ServiceException;
import com.zzy.shortLink.project.common.enums.ValiDateTypeEnum;
import com.zzy.shortLink.project.config.GotoDomainWhiteListConfiguration;
import com.zzy.shortLink.project.dao.entity.*;
import com.zzy.shortLink.project.dao.mapper.*;
import com.zzy.shortLink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.zzy.shortLink.project.dto.resp.*;
import com.zzy.shortLink.project.mq.producer.DelayShortLinkStatsProducer;
import com.zzy.shortLink.project.service.LinkStatsTodayService;
import com.zzy.shortLink.project.service.ShortLinkService;
import com.zzy.shortLink.project.toolkit.HashUtil;
import com.zzy.shortLink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.zzy.shortLink.project.common.constant.RedisKeyConstant.*;
import static com.zzy.shortLink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static com.zzy.shortLink.project.toolkit.LinkUtil.*;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCacheBloomFilter;
    private final ShortLinkGoToMapper shortLinkGoToMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogMapper linkAccessLogMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkStatsTodayService linkStatsTodayService;
    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO) {
        verificationWhitelist(reqDTO.getOriginUrl());
        String shortLinkSuffix = generateSuffix(reqDTO);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .fullShortUrl(fullShortUrl)
                .domain(createShortLinkDefaultDomain)
                .originUrl(reqDTO.getOriginUrl())
                .shortUri(shortLinkSuffix)
                .gid(reqDTO.getGid())
                .createdType(reqDTO.getCreatedType())
                .validDateType(reqDTO.getValidDateType())
                .validDate(reqDTO.getValidDate())
                .enableStatus(0)
                .favicon(getFavicon(reqDTO.getOriginUrl()))
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
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                reqDTO.getOriginUrl(),
                getLinkCacheValidTime(reqDTO.getValidDate()), TimeUnit.MILLISECONDS
        );
        shortUriCreateCacheBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(reqDTO.getOriginUrl())
                .gid(reqDTO.getGid())
                .build();
    }

    @Override
    public ShortLinkBatchCreateRespDTO createShortLinkBatch(ShortLinkBatchCreateReqDTO batchCreateReqDTO) {
        List<String> originUrls = batchCreateReqDTO.getOriginUrls();
        List<String> describes = batchCreateReqDTO.getDescribes();
        List<ShortLinkBaseInfoRespDTO> resultList = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(batchCreateReqDTO, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try{
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO infoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .describe(describes.get(i))
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .build();
                resultList.add(infoRespDTO);
            }catch (Throwable ex){
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(resultList.size())
                .baseInfoList(resultList)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO reqDTO) {
        verificationWhitelist(reqDTO.getOriginUrl());
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
            RReadWriteLock rReadWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, reqDTO.getFullShortUrl()));
            RLock rLock = rReadWriteLock.writeLock();
            if(!rLock.tryLock()){
                throw new ServiceException("短链接正在被访问，请稍后再试……");
            }
            try{
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
                        .favicon(getFavicon(reqDTO.getOriginUrl()))
                        .createdType(hasShortLinkDO.getCreatedType())
                        .gid(reqDTO.getGid())
                        .originUrl(reqDTO.getOriginUrl())
                        .describe(reqDTO.getDescribe())
                        .validDateType(reqDTO.getValidDateType())
                        .validDate(reqDTO.getValidDate())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .delTime(0L)
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayDOLambdaQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, reqDTO.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayDOLambdaQueryWrapper);
                if(!CollUtil.isEmpty(linkStatsTodayDOList)){
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList());
                    linkStatsTodayDOList.forEach(each -> each.setGid(reqDTO.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                LambdaQueryWrapper<ShortLinkGoTODO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGoTODO.class)
                        .eq(ShortLinkGoTODO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(ShortLinkGoTODO::getGid, hasShortLinkDO.getGid());
                ShortLinkGoTODO shortLinkGoTODO = shortLinkGoToMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGoToMapper.deleteById(shortLinkGoTODO.getId());
                shortLinkGoTODO.setGid(reqDTO.getGid());
                shortLinkGoToMapper.insert((shortLinkGoTODO));
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(reqDTO.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(reqDTO.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(reqDTO.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(reqDTO.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(reqDTO.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogDO> linkAccessLogUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogDO.class)
                        .eq(LinkAccessLogDO::getFullShortUrl, reqDTO.getFullShortUrl())
                        .eq(LinkAccessLogDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessLogDO::getDelFlag, 0);
                LinkAccessLogDO linkAccessLogDO = LinkAccessLogDO.builder()
                        .gid(reqDTO.getGid())
                        .build();
                linkAccessLogMapper.update(linkAccessLogDO, linkAccessLogUpdateWrapper);
            }finally {
                rLock.unlock();
            }
        }
        if(!Objects.equals(hasShortLinkDO.getValidDateType(), reqDTO.getValidDateType())
                || !Objects.equals(hasShortLinkDO.getValidDate(), reqDTO.getValidDate())
                || !Objects.equals(hasShortLinkDO.getOriginUrl(), reqDTO.getOriginUrl())
                ){
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, reqDTO.getFullShortUrl()));
            Date currentDate = new Date();
            if(hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().before(new Date())){
                if(Objects.equals(reqDTO.getValidDateType(), ValiDateTypeEnum.PERMANENT.getType())
                || reqDTO.getValidDate().after(currentDate)){
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, reqDTO.getFullShortUrl()));
                }
            }
        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(shortLinkPageReqDTO);
        return resultPage.convert(each -> {
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
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @SneakyThrows
    @Override
    public void restoreUri(String shortUri, ServletRequest request, ServletResponse response) {
        //判断是不是在布隆过滤器
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each ->":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if(StrUtil.isNotBlank(originalLink)){
            ShortLinkStatsRecordDTO statsRecordDTO = buildLinkStatsRecordDTO(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecordDTO);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        //解决缓存穿透问题，通过布隆过滤器，布隆过滤器的误判可能判成存在，但是实际上数据库不存在的情况，此时还要查询数据库
        //整体是布隆过滤器加上缓存空值两种搭配起来再加上分布式锁的解决方案
        //最后还有一个问题：假如大量并发请求尝试看访问一个不存在的短链接，且正好被布隆过滤器误判定存在，而此时还没有缓存null，
        // 意味着第一个拿到锁的线程将会查库并且重构空缓存，但是后面的线程会重复执行第一个线程的步骤，因此在获取锁以后还需要增加一个二次判空
        boolean contains = shortUriCreateCacheBloomFilter.contains(fullShortUrl);
        if(!contains){
            //布隆过滤器基本不会误判不存在，所以这里如果是true，说明一定不存在,跳转404。
            //如果误判存在(或者曾经的短链接移至回收站)，查空值缓存，如果没有查到则查数据库，如果依然没有找到，那么后续构建缓存空值，如果存在，构建key
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if(StrUtil.isNotBlank(gotoIsNullShortLink)){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try{
            //缓存击穿解决方案：锁的双重判定。防止第一次请求加载后剩余的请求也走一遍流程
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if(StrUtil.isNotBlank(originalLink)){
                ShortLinkStatsRecordDTO statsRecordDTO = buildLinkStatsRecordDTO(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecordDTO);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            //缓存穿透解决：同样是双重判定锁。二次检查空值缓存
            gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
            if(StrUtil.isNotBlank(gotoIsNullShortLink)){
                return;
            }
            //空值缓存的确不存在，走入数据库查询，先查路由表中存入的gid
            LambdaQueryWrapper<ShortLinkGoTODO> shortLinkGoTOQueryWrapper = Wrappers.lambdaQuery(ShortLinkGoTODO.class)
                    .eq(ShortLinkGoTODO::getFullShortUrl, fullShortUrl);
            ShortLinkGoTODO shortLinkGoTODO = shortLinkGoToMapper.selectOne(shortLinkGoTOQueryWrapper);
            if(shortLinkGoTODO == null){
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGoTODO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if(shortLinkDO == null || (shortLinkDO.getValidDate()!=null && shortLinkDO.getValidDate().before(new Date()))){
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
            ShortLinkStatsRecordDTO statsRecordDTO = buildLinkStatsRecordDTO(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecordDTO);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock();
        }
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordDTO(String fullShortUrl, ServletRequest request, ServletResponse response){
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = ()->{
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
        };
        if(ArrayUtil.isNotEmpty(cookies)){
            Arrays.stream(cookies)
                    .filter(each -> each.getName().equals("uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        //redis的set自动去重
                        Long added = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                        uvFirstFlag.set(added != null && added >0L);
                    }, addResponseCookieTask);
        }else {
            //Cookie为空，第一次访问
            addResponseCookieTask.run();
        }
        String remoteAddr = getRealAddress((HttpServletRequest) request);
        String os = getOS((HttpServletRequest) request);
        String browser = getBrowser((HttpServletRequest) request);
        String device = getDevice((HttpServletRequest) request);
        String network = getNetwork((HttpServletRequest) request);
        Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded>0L;
        return ShortLinkStatsRecordDTO.builder()
                .browser(browser)
                .device(device)
                .fullShortUrl(fullShortUrl)
                .network(network)
                .os(os)
                .remoteAddr(remoteAddr)
                .uipFirstFlag(uipFirstFlag)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .build();
    }

    @Override
    public void shortLinkStats(String fullShortUrl,String gid, ShortLinkStatsRecordDTO statsRecord){
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if(!rLock.tryLock()){
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
        try{
            if(StrUtil.isBlank(gid)){
                LambdaQueryWrapper<ShortLinkGoTODO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGoTODO.class)
                        .eq(ShortLinkGoTODO::getFullShortUrl, fullShortUrl);
                ShortLinkGoTODO shortLinkGoTODO = shortLinkGoToMapper.selectOne(queryWrapper);
                gid = shortLinkGoTODO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(statsRecord.getUipFirstFlag()? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag()? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAmapKey);
            localeParamMap.put("ip", statsRecord.getRemoteAddr());
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infoCode = localeResultObj.getString("infocode");
            String actualProvince = "未知";
            String actualCity = "未知";
            if(StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode,"10000")){
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .date(new Date())
                        .province(actualProvince = unknownFlag ? actualProvince : province)
                        .city(actualCity = unknownFlag ? actualCity : localeResultObj.getString("city"))
                        .adcode(unknownFlag? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleStats(linkLocaleStatsDO);
            }
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .os(statsRecord.getOs())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(statsRecord.getBrowser())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(statsRecord.getDevice())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(statsRecord.getNetwork())
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
            LinkAccessLogDO linkAccessLogsDO = LinkAccessLogDO.builder()
                    .user(statsRecord.getUv())
                    .ip(statsRecord.getRemoteAddr())
                    .browser(statsRecord.getBrowser())
                    .os(statsRecord.getOs())
                    .network(statsRecord.getNetwork())
                    .device(statsRecord.getDevice())
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .build();
            linkAccessLogMapper.insert(linkAccessLogsDO);
            baseMapper.incrementStats(1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0,gid, fullShortUrl);
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .todayPv(1)
                    .todayUv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .todayUip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkStatsTodayMapper.shortLinkTodayStats(linkStatsTodayDO);
        }catch (Throwable ex){
            log.error("短链接访问量统计异常", ex);
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

    @SneakyThrows
    private String getFavicon(String url){
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            Element link = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if(link != null){
                return link.attr("abs:href");
            }
        }
        return null;
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }
}
