package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkBrowserStatsDO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {

    /**
     * 记录浏览器访问数据
     */
    @Insert("""
           INSERT INTO t_link_browser_stats (full_short_url, gid, date, cnt, browser, create_time, update_time, del_flag)
           VALUES( #{linkBrowserStats.fullShortUrl}, #{linkBrowserStats.gid}, #{linkBrowserStats.date}, #{linkBrowserStats.cnt}, #{linkBrowserStats.browser}, NOW(), NOW(), 0 ) ON DUPLICATE KEY UPDATE
           cnt = cnt + #{linkBrowserStats.cnt};
           """)
    void shortLinkBrowserStats(@Param("linkBrowserStats") LinkBrowserStatsDO linkBrowserStatsDO);

    /**
     * 根据短链接获取指定日期内浏览器监控数据
     */
    @Select("""
            select browser, sum(cnt) as count from t_link_browser_stats 
            where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            and date between #{param.startDate} and #{param.endDate}
            group by full_short_url, gid, browser;
            """)
    List<HashMap<String, Object>> listBrowserStats(@Param("param")ShortLinkStatsReqDTO statsReqDTO);

}
