package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkLocaleStatsDO;
import com.zzy.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 *地区统计访问实体持久层
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    /**
     * 记录地区访问表
     */
    @Insert("""   
            INSERT INTO t_link_locale_stats (full_short_url, gid, date, cnt, country, province, city, adcode, create_time, update_time, del_flag )
                            VALUES( #{linkLocaleStats.fullShortUrl}, #{linkLocaleStats.gid}, #{linkLocaleStats.date}, 
                            #{linkLocaleStats.cnt}, #{linkLocaleStats.country}, #{linkLocaleStats.province}, #{linkLocaleStats.city}, #{linkLocaleStats.adcode},
                            NOW(), NOW(), 0 ) ON DUPLICATE KEY UPDATE
                            cnt = cnt + #{linkLocaleStats.cnt};
            """)
    void shortLinkLocaleStats(@Param("linkLocaleStats") LinkLocaleStatsDO linkLocaleStatsDO);

    /**
     * 根据短链接获取指定日期内的地区监控数据
     */
    @Select("""
            select province, sum(cnt) as cnt from t_link_locale_stats where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            and date between #{param.startDate} and #{param.endDate}
            group by gid, full_short_url, province;
            """)
    List<LinkLocaleStatsDO> listLocaleStats(@Param("param") ShortLinkStatsReqDTO statsReqDTO);

    /**
     * 根据分组获取指定日期内的地区监控数据
     */
    @Select("""
            select province, sum(cnt) as cnt from t_link_locale_stats where gid = #{param.gid}
            and date between #{param.startDate} and #{param.endDate}
            group by gid, province;
            """)
    List<LinkLocaleStatsDO> listLocaleByGroup(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

}
