package com.zzy.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzy.shortLink.project.dao.entity.*;
import com.zzy.shortLink.project.dao.mapper.*;
import com.zzy.shortLink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import com.zzy.shortLink.project.dto.resp.*;
import com.zzy.shortLink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkAccessLogMapper linkAccessLogMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;

    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        List<LinkAccessStatsDO> listStatsByShortLink = linkAccessStatsMapper.listStatsByShortLink(shortLinkStatsReqDTO);
        if(CollUtil.isEmpty(listStatsByShortLink)){
            return null;
        }
        //基础访问数据
        LinkAccessStatsDO pvUvUipStatsByShortLink = linkAccessLogMapper.findPvUvUipStatsByShortLink(shortLinkStatsReqDTO);

        //基础访问详情
        List<ShortLinkStatsAccessDailyRespDTO> daily = new ArrayList<>();
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(shortLinkStatsReqDTO.getStartDate()), DateUtil.parse(shortLinkStatsReqDTO.getEndDate()), DateField.DAY_OF_MONTH).stream()
                .map(DateUtil::formatDate)
                .toList();
        rangeDates.forEach(each -> listStatsByShortLink.stream()
                .filter(item -> Objects.equals(DateUtil.formatDate(item.getDate()), each))
                .findFirst()
                .ifPresentOrElse(item -> {
                    ShortLinkStatsAccessDailyRespDTO dailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .pv(item.getPv())
                            .uv(item.getUv())
                            .uip(item.getUip())
                            .build();
                    daily.add(dailyRespDTO);
                }, ()->{
                    ShortLinkStatsAccessDailyRespDTO dailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .pv(0)
                            .uv(0)
                            .uip(0)
                            .build();
                    daily.add(dailyRespDTO);
                }));

        //地区访问详情
        List<ShortLinkStatsLocaleCNRespDTO> listLocaleStats = new ArrayList<>();
        List<LinkLocaleStatsDO> localeStatsDOS = linkLocaleStatsMapper.listLocaleStats(shortLinkStatsReqDTO);
        int localeCnSum = localeStatsDOS.stream()
                .mapToInt(LinkLocaleStatsDO::getCnt)
                .sum();

        localeStatsDOS.forEach(each ->{
            double ratio = (double) each.getCnt() / localeCnSum;
            //处理成两位小数
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsLocaleCNRespDTO localeCNRespDTO = ShortLinkStatsLocaleCNRespDTO.builder()
                    .locale(each.getProvince())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            listLocaleStats.add(localeCNRespDTO);
        });

        //小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDO> listHourStatsByShortLink = linkAccessStatsMapper.listHourStatsByShortLink(shortLinkStatsReqDTO);
        for(int i = 0; i < 24; i++){
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    //.map() 方法在这里的作用就是将流中的 LinkAccessStatsDO 对象转换为其 pv 属性的值，从而将流的元素类型从 LinkAccessStatsDO 映射到了 Integer。
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }

        //一周访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        List<LinkAccessStatsDO> weekdayStatsByShortLink = linkAccessStatsMapper.listWeekdayStatsByShortLink(shortLinkStatsReqDTO);
        for(int i = 1; i < 8; i++){
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = weekdayStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }

        //高频IP访问详情
        List<ShortLinkStatsTopIpRespDTO> listTopIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink = linkAccessLogMapper.listTopIpByShortLink(shortLinkStatsReqDTO);
        listTopIpByShortLink.forEach(each ->{
            ShortLinkStatsTopIpRespDTO statsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .build();
            listTopIpStats.add(statsTopIpRespDTO);
        });

        //浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserRespDTOS = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStats = linkBrowserStatsMapper.listBrowserStats(shortLinkStatsReqDTO);
        int browserSum = listBrowserStats.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStats.forEach(each ->{
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            //处理成两位小数
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsBrowserRespDTO browserRespDTO = ShortLinkStatsBrowserRespDTO.builder()
                    .browser(each.get("browser").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .ratio(actualRatio)
                    .build();
            browserRespDTOS.add(browserRespDTO);
        });

        //操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osRespDTOS = new ArrayList<>();
        List<HashMap<String, Object>> listOsStats = linkOsStatsMapper.listOsStatsByShortLink(shortLinkStatsReqDTO);
        int osSum = listOsStats.stream()
                .mapToInt(each -> Integer.parseInt(each.get("cnt").toString()))
                .sum();
        listOsStats.forEach(each ->{
            double ratio = (double) Integer.parseInt(each.get("cnt").toString()) / osSum;
            //处理成两位小数
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsOsRespDTO osRespDTO = ShortLinkStatsOsRespDTO.builder()
                    .os(each.get("os").toString())
                    .cnt(Integer.parseInt(each.get("cnt").toString()))
                    .ratio(actualRatio)
                    .build();
            osRespDTOS.add(osRespDTO);
        });

        //访客访问类型详情
        List<ShortLinkStatsUvRespDTO> uvTypeRespDTOS = new ArrayList<>();
        HashMap<String, Object> uvTypeByShortLink = linkAccessLogMapper.findUvTypeCntByShortLink(shortLinkStatsReqDTO);
        int oldUserCount = Integer.parseInt(uvTypeByShortLink.get("oldUserCount").toString());
        int newUserCount = Integer.parseInt(uvTypeByShortLink.get("newUserCount").toString());
        int uvSum = oldUserCount + newUserCount;
        double oldRatio = (double) oldUserCount / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCount / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        ShortLinkStatsUvRespDTO oldUserRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("oldUser")
                .cnt(oldUserCount)
                .ratio(actualOldRatio)
                .build();
        ShortLinkStatsUvRespDTO newUserRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("newUser")
                .cnt(newUserCount)
                .ratio(actualNewRatio)
                .build();
        uvTypeRespDTOS.add(oldUserRespDTO);
        uvTypeRespDTOS.add(newUserRespDTO);

        //访问设备详情
        List<ShortLinkStatsDeviceRespDTO> deviceRespDTOS = new ArrayList<>();
        List<LinkDeviceStatsDO> linkDeviceStatsDOS = linkDeviceStatsMapper.listDeviceStatsByShortLink(shortLinkStatsReqDTO);
        int deviceSum = linkDeviceStatsDOS.stream()
                .mapToInt(each -> each.getCnt())
                .sum();
        linkDeviceStatsDOS.forEach(each ->{
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsDeviceRespDTO deviceRespDTO = ShortLinkStatsDeviceRespDTO.builder()
                    .device(each.getDevice())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            deviceRespDTOS.add(deviceRespDTO);
        });

        //访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkRespDTOS = new ArrayList<>();
        List<LinkNetworkStatsDO> linkNetworkStatsDOS = linkNetworkStatsMapper.listNetWorkStatsByShortLink(shortLinkStatsReqDTO);
        int netWorkStats = linkNetworkStatsDOS.stream()
                .mapToInt(LinkNetworkStatsDO::getCnt)
                .sum();
        linkNetworkStatsDOS.forEach(each->{
            double ratio = (double) each.getCnt() / netWorkStats;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsNetworkRespDTO networkRespDTO = ShortLinkStatsNetworkRespDTO.builder()
                    .network(each.getNetwork())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            networkRespDTOS.add(networkRespDTO);
        });

        return ShortLinkStatsRespDTO.builder()
                .daily(daily)
                .pv(pvUvUipStatsByShortLink.getPv())
                .uv(pvUvUipStatsByShortLink.getUv())
                .uip(pvUvUipStatsByShortLink.getUip())
                .browserStats(browserRespDTOS)
                .deviceStats(deviceRespDTOS)
                .hourStats(hourStats)
                .osStats(osRespDTOS)
                .topIpStats(listTopIpStats)
                .localeCnStats(listLocaleStats)
                .weekdayStats(weekdayStats)
                .uvTypeStats(uvTypeRespDTOS)
                .networkStats(networkRespDTOS)
                .build();
    }

    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO accessRecordReqDTO) {
        accessRecordReqDTO.setStartDate(accessRecordReqDTO.getStartDate() + " 00:00:00");
        accessRecordReqDTO.setEndDate(accessRecordReqDTO.getEndDate() + " 23:59:59");
        LambdaQueryWrapper<LinkAccessLogDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogDO.class)
                .eq(LinkAccessLogDO::getGid, accessRecordReqDTO.getGid())
                .eq(LinkAccessLogDO::getFullShortUrl, accessRecordReqDTO.getFullShortUrl())
                .between(LinkAccessLogDO::getCreateTime, accessRecordReqDTO.getStartDate(), accessRecordReqDTO.getEndDate())
                .eq(LinkAccessLogDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogDO::getCreateTime);

        IPage<LinkAccessLogDO> linkAccessLogDOIPage = linkAccessLogMapper.selectPage(accessRecordReqDTO, queryWrapper);
        IPage<ShortLinkStatsAccessRecordRespDTO> convertResult = linkAccessLogDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
        List<String> accessLogUsers = convertResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();
        if(CollectionUtil.isEmpty(accessLogUsers)){
            return convertResult;
        }
        List<Map<String, Object>> uvTypeList = linkAccessLogMapper.selectUvTypeByUsers(
                accessRecordReqDTO.getGid(),
                accessRecordReqDTO.getFullShortUrl(),
                accessRecordReqDTO.getStartDate(),
                accessRecordReqDTO.getEndDate(),
                accessLogUsers);

        convertResult.getRecords().stream().forEach(each ->{
            String uvType = uvTypeList.stream()
                    .filter(item -> Objects.equals(item.get("user"), each.getUser()))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            each.setUvType(uvType);
        });
        return convertResult;
    }

    @Override
    public ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO) {
        List<LinkAccessStatsDO> linkAccessStatsDOS = linkAccessStatsMapper.listStatsByGroup(shortLinkGroupStatsReqDTO);
        if(CollUtil.isEmpty(linkAccessStatsDOS)){
            return null;
        }
        //基础访问数据
        LinkAccessStatsDO pvUvUipStatsByGroup = linkAccessLogMapper.pvUvUipStatsByGroup(shortLinkGroupStatsReqDTO);
        //基础访问详情
        List<ShortLinkStatsAccessDailyRespDTO> daily = new ArrayList<>();
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(shortLinkGroupStatsReqDTO.getStartDate()), DateUtil.parse(shortLinkGroupStatsReqDTO.getEndDate()), DateField.DAY_OF_MONTH).stream()
                .map(DateUtil::formatDate)
                .toList();
        rangeDates.forEach(each -> linkAccessStatsDOS.stream()
                .filter(item -> Objects.equals(DateUtil.formatDate(item.getDate()), each))
                .findFirst()
                .ifPresentOrElse(item -> {
                    ShortLinkStatsAccessDailyRespDTO dailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .pv(item.getPv())
                            .uv(item.getUv())
                            .uip(item.getUip())
                            .build();
                    daily.add(dailyRespDTO);
                }, ()->{
                    ShortLinkStatsAccessDailyRespDTO dailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .pv(0)
                            .uv(0)
                            .uip(0)
                            .build();
                    daily.add(dailyRespDTO);
                }));

        //地区访问详情
        List<ShortLinkStatsLocaleCNRespDTO> listLocaleStats = new ArrayList<>();
        List<LinkLocaleStatsDO> localeStatsDOS = linkLocaleStatsMapper.listLocaleByGroup(shortLinkGroupStatsReqDTO);
        int localeCnSum = localeStatsDOS.stream()
                .mapToInt(LinkLocaleStatsDO::getCnt)
                .sum();

        localeStatsDOS.forEach(each ->{
            double ratio = (double) each.getCnt() / localeCnSum;
            //处理成两位小数
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsLocaleCNRespDTO localeCNRespDTO = ShortLinkStatsLocaleCNRespDTO.builder()
                    .locale(each.getProvince())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            listLocaleStats.add(localeCNRespDTO);
        });

        //小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDO> listHourStatsByShortLink = linkAccessStatsMapper.listHourStatsByGroup(shortLinkGroupStatsReqDTO);
        for(int i = 0; i < 24; i++){
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    //.map() 方法在这里的作用就是将流中的 LinkAccessStatsDO 对象转换为其 pv 属性的值，从而将流的元素类型从 LinkAccessStatsDO 映射到了 Integer。
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }

        //一周访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        List<LinkAccessStatsDO> weekdayStatsByShortLink = linkAccessStatsMapper.listWeekdayStatsByGroup(shortLinkGroupStatsReqDTO);
        for(int i = 1; i < 8; i++){
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = weekdayStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }

        //高频IP访问详情
        List<ShortLinkStatsTopIpRespDTO> listTopIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink = linkAccessLogMapper.listTopIpByGroup(shortLinkGroupStatsReqDTO);
        listTopIpByShortLink.forEach(each ->{
            ShortLinkStatsTopIpRespDTO statsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .build();
            listTopIpStats.add(statsTopIpRespDTO);
        });

        //浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserRespDTOS = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStats = linkBrowserStatsMapper.listBrowserStatsByGroup(shortLinkGroupStatsReqDTO);
        int browserSum = listBrowserStats.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStats.forEach(each ->{
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            //处理成两位小数
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsBrowserRespDTO browserRespDTO = ShortLinkStatsBrowserRespDTO.builder()
                    .browser(each.get("browser").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .ratio(actualRatio)
                    .build();
            browserRespDTOS.add(browserRespDTO);
        });

        //操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osRespDTOS = new ArrayList<>();
        List<HashMap<String, Object>> listOsStats = linkOsStatsMapper.listOsStatsByGroup(shortLinkGroupStatsReqDTO);
        int osSum = listOsStats.stream()
                .mapToInt(each -> Integer.parseInt(each.get("cnt").toString()))
                .sum();
        listOsStats.forEach(each ->{
            double ratio = (double) Integer.parseInt(each.get("cnt").toString()) / osSum;
            //处理成两位小数
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsOsRespDTO osRespDTO = ShortLinkStatsOsRespDTO.builder()
                    .os(each.get("os").toString())
                    .cnt(Integer.parseInt(each.get("cnt").toString()))
                    .ratio(actualRatio)
                    .build();
            osRespDTOS.add(osRespDTO);
        });

        //访客访问类型详情
        List<ShortLinkStatsUvRespDTO> uvTypeRespDTOS = new ArrayList<>();
        HashMap<String, Object> uvTypeByShortLink = linkAccessLogMapper.findUvTypeCntByShortGroup(shortLinkGroupStatsReqDTO);
        int oldUserCount = Integer.parseInt(uvTypeByShortLink.get("oldUserCount").toString());
        int newUserCount = Integer.parseInt(uvTypeByShortLink.get("newUserCount").toString());
        int uvSum = oldUserCount + newUserCount;
        double oldRatio = (double) oldUserCount / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCount / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        ShortLinkStatsUvRespDTO oldUserRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("oldUser")
                .cnt(oldUserCount)
                .ratio(actualOldRatio)
                .build();
        ShortLinkStatsUvRespDTO newUserRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("newUser")
                .cnt(newUserCount)
                .ratio(actualNewRatio)
                .build();
        uvTypeRespDTOS.add(oldUserRespDTO);
        uvTypeRespDTOS.add(newUserRespDTO);

        //访问设备详情
        List<ShortLinkStatsDeviceRespDTO> deviceRespDTOS = new ArrayList<>();
        List<LinkDeviceStatsDO> linkDeviceStatsDOS = linkDeviceStatsMapper.listDeviceStatsBykGroup(shortLinkGroupStatsReqDTO);
        int deviceSum = linkDeviceStatsDOS.stream()
                .mapToInt(each -> each.getCnt())
                .sum();
        linkDeviceStatsDOS.forEach(each ->{
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsDeviceRespDTO deviceRespDTO = ShortLinkStatsDeviceRespDTO.builder()
                    .device(each.getDevice())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            deviceRespDTOS.add(deviceRespDTO);
        });

        //访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkRespDTOS = new ArrayList<>();
        List<LinkNetworkStatsDO> linkNetworkStatsDOS = linkNetworkStatsMapper.listNetWorkStatsByGroup(shortLinkGroupStatsReqDTO);
        int netWorkStats = linkNetworkStatsDOS.stream()
                .mapToInt(LinkNetworkStatsDO::getCnt)
                .sum();
        linkNetworkStatsDOS.forEach(each->{
            double ratio = (double) each.getCnt() / netWorkStats;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsNetworkRespDTO networkRespDTO = ShortLinkStatsNetworkRespDTO.builder()
                    .network(each.getNetwork())
                    .cnt(each.getCnt())
                    .ratio(actualRatio)
                    .build();
            networkRespDTOS.add(networkRespDTO);
        });

        return ShortLinkStatsRespDTO.builder()
                .daily(daily)
                .pv(pvUvUipStatsByGroup.getPv())
                .uv(pvUvUipStatsByGroup.getUv())
                .uip(pvUvUipStatsByGroup.getUip())
                .browserStats(browserRespDTOS)
                .deviceStats(deviceRespDTOS)
                .hourStats(hourStats)
                .osStats(osRespDTOS)
                .topIpStats(listTopIpStats)
                .localeCnStats(listLocaleStats)
                .weekdayStats(weekdayStats)
                .uvTypeStats(uvTypeRespDTOS)
                .networkStats(networkRespDTOS)
                .build();
    }

    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO groupStatsAccessRecordReqDTO) {
        groupStatsAccessRecordReqDTO.setStartDate(groupStatsAccessRecordReqDTO.getStartDate() + " 00:00:00");
        groupStatsAccessRecordReqDTO.setEndDate(groupStatsAccessRecordReqDTO.getEndDate() + " 23:59:59");
        LambdaQueryWrapper<LinkAccessLogDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogDO.class)
                .eq(LinkAccessLogDO::getGid, groupStatsAccessRecordReqDTO.getGid())
                .between(LinkAccessLogDO::getCreateTime, groupStatsAccessRecordReqDTO.getStartDate(), groupStatsAccessRecordReqDTO.getEndDate())
                .eq(LinkAccessLogDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogDO::getCreateTime);

        IPage<LinkAccessLogDO> linkAccessLogDOIPage = linkAccessLogMapper.selectPage(groupStatsAccessRecordReqDTO, queryWrapper);
        IPage<ShortLinkStatsAccessRecordRespDTO> convertResult = linkAccessLogDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
        List<String> accessLogUsers = convertResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();
        if(CollectionUtil.isEmpty(accessLogUsers)){
            return convertResult;
        }
        List<Map<String, Object>> uvTypeList = linkAccessLogMapper.selectGroupUvTypeByUsers(
                groupStatsAccessRecordReqDTO.getGid(),
                groupStatsAccessRecordReqDTO.getStartDate(),
                groupStatsAccessRecordReqDTO.getEndDate(),
                accessLogUsers);

        convertResult.getRecords().stream().forEach(each ->{
            String uvType = uvTypeList.stream()
                    .filter(item -> Objects.equals(item.get("user"), each.getUser()))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            each.setUvType(uvType);
        });
        return convertResult;
    }


}
