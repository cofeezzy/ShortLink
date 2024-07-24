package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkDeviceStatsDO;
import com.zzy.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 统计访问设备监控持久层
 */
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {

    /**
     * 记录访问设备监控数据
     */
    @Insert("""
            INSERT INTO t_link_device_stats (full_short_url, gid, date, cnt, device, create_time, update_time, del_flag)
            VALUES( #{linkDeviceStats.fullShortUrl}, #{linkDeviceStats.gid}, #{linkDeviceStats.date}, #{linkDeviceStats.cnt}, #{linkDeviceStats.device}, NOW(), NOW(), 0) 
            ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkDeviceStats.cnt};
            """)
    void shortLinkDeviceState(@Param("linkDeviceStats") LinkDeviceStatsDO linkDeviceStatsDO);

    /**
     * 根据短链接获取指定日期内访问设备监控数据
     */
    @Select("""
             select device, sum(cnt) as cnt from t_link_device_stats where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
             and date between #{param.startDate} and #{param.endDate}
             group by gid, full_short_url, device;
             """)
    List<LinkDeviceStatsDO> listDeviceStatsByShortLink(@Param("param")ShortLinkStatsReqDTO statsReqDTO);

    /**
     * 根据短链接组获取指定日期内访问设备监控数据
     */
    @Select("""
             select device, sum(cnt) as cnt from t_link_device_stats where gid = #{param.gid}
             and date between #{param.startDate} and #{param.endDate}
             group by gid, device;
             """)
    List<LinkDeviceStatsDO> listDeviceStatsBykGroup(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

}
