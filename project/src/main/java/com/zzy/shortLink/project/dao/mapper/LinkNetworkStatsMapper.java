package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkNetworkStatsDO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 记录访问设备监控数据
     */
    @Insert("""
            INSERT INTO t_link_network_stats (full_short_url, gid, date, cnt, network, create_time, update_time, del_flag)
            VALUES( #{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.gid}, #{linkNetworkStats.date}, #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0)
            ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkNetworkStats.cnt};
            """)
    void shortLinkNetworkState(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStatsDO);

    /**
     * 根据短链接获取指定日期内的监控数据
     */
    @Select("""
            select network, sum(cnt) as cnt from t_link_network_stats 
            where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            and date between #{param.startDate} and #{param.endDate}
            group by full_short_url, gid, network;
            """)
    List<LinkNetworkStatsDO> listNetWorkStatsByShortLink(@Param("param")ShortLinkStatsReqDTO statsReqDTO);
}
