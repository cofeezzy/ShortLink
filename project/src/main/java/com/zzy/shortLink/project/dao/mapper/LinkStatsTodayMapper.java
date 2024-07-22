package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接今日统计持久层
 */
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /**
     * 记录每日访问数据
     */
    @Insert("""
            INSERT INTO t_link_stats_today (full_short_url, gid, date,today_pv, today_uv, today_uip, create_time, update_time, del_flag ) 
            VALUES( #{linkTodayStats.fullShortUrl}, #{linkTodayStats.gid}, #{linkTodayStats.date}, #{linkTodayStats.todayPv}, 
            #{linkTodayStats.todayUv}, #{linkTodayStats.todayUip}, NOW(), NOW(), 0 ) ON DUPLICATE KEY UPDATE
            today_uv = today_uv + #{linkTodayStats.todayUv}, today_pv = today_pv + #{linkTodayStats.todayPv}, today_uip = today_uip + #{linkTodayStats.todayUip};
            """)
    void shortLinkTodayStats(@Param("linkTodayStats") LinkStatsTodayDO linkStatsTodayDO);
}
