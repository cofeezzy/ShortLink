package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkAccessStatsDO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接基础访问监控持久层
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录基础访问数据
     * @param linkAccessStatsDO
     */
    @Insert("INSERT INTO t_link_access_stats (full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag ) " +
            "VALUES( #{linkAccessStats.fullShortUrl}, #{linkAccessStats.gid}, #{linkAccessStats.date}, #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, #{linkAccessStats.hour}, #{linkAccessStats.weekday}, NOW(), NOW(), 0 ) ON DUPLICATE KEY UPDATE pv = pv + #{linkAccessStats.pv}, " +
            "uv = uv + #{linkAccessStats.uv}, " +
            "uip = uip + #{linkAccessStats.uip};")
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("""
            select date, sum(pv) as pv, sum(uv) as uv, sum(uip) as uip
            from t_link_access_stats
            where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            and date between #{param.startDate} and #{param.endDate}
            group by full_short_url, gid, date
            """)
    List<LinkAccessStatsDO> listStatsByShortLink(@Param("param") ShortLinkStatsReqDTO statsReqDTO);

    /**
     * 根据短链接获取指定日期内小时监控数据
     */
    @Select("""
            select hour, sum(pv) as pv
            from t_link_access_stats
            where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            and date BETWEEN #{param.startDate} and #{param.endDate}
            group by full_short_url, gid, date, hour
            """)
    List<LinkAccessStatsDO> listHourStatsByShortLink(@Param("param") ShortLinkStatsReqDTO statsReqDTO);

    /**
     * 根据短链接获取指定日期内星期监控数据
     */
    @Select("""
            select weekday, sum(pv) as pv
            from t_link_access_stats
            where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            and date BETWEEN #{param.startDate} and #{param.endDate}
            group by full_short_url, gid, date, weekday
            """)
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsReqDTO statsReqDTO);
}
