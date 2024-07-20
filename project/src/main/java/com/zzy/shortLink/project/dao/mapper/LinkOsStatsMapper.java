package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkOsStatsDO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 操作系统统计访问持久层
 */
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    /**
     * 记录os访问数据
     * @param linkOsStatsDO
     */
    @Insert("""
            INSERT INTO t_link_os_stats (full_short_url, gid, date, cnt, os, create_time, update_time, del_flag ) 
            VALUES( #{linkOsStats.fullShortUrl}, #{linkOsStats.gid}, #{linkOsStats.date}, #{linkOsStats.cnt},  #{linkOsStats.os}, NOW(), NOW(), 0 ) ON DUPLICATE KEY UPDATE
            cnt = cnt + #{linkOsStats.cnt};
            """)
    void shortLinkStats(@Param("linkOsStats") LinkOsStatsDO linkOsStatsDO);

    /**
     * 根据短链接获取指定日期内的操作系统监控数据
     */
    @Select("""
            select os, sum(cnt) as cnt from t_link_os_stats
            where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            and date between #{param.startDate} and #{param.endDate}
            group by full_short_url, gid, os;
            """)
    List<HashMap<String, Object>> listOsStatsByShortLink(@Param("param") ShortLinkStatsReqDTO statsReqDTO);
}
