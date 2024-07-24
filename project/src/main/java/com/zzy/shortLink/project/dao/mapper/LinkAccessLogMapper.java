package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.shortLink.project.dao.entity.LinkAccessLogDO;
import com.zzy.shortLink.project.dao.entity.LinkAccessStatsDO;
import com.zzy.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.zzy.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问日志持久层
 */
public interface LinkAccessLogMapper extends BaseMapper<LinkAccessLogDO> {

    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    @Select("""
             select ip, COUNT(ip) count from t_link_access_logs where full_short_url = #{param.fullShortUrl}
             and gid = #{param.gid}
             and create_time between  CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59')
             group by ip, gid, full_short_url
             order by count desc
             limit 5
             """)
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param")ShortLinkStatsReqDTO statsReqDTO);

    /**
     * 根据分组获取指定日期内高频访问IP数据
     */
    @Select("""
             select ip, COUNT(ip) count from t_link_access_logs where
             gid = #{param.gid}
             and create_time between  CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59')
             group by ip, gid
             order by count desc
             limit 5
             """)
    List<HashMap<String, Object>> listTopIpByGroup(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    /**
     * 根据短链接获取指定日期内新老访客数据
     */
    @Select("""
            select sum(old_user) as oldUserCount, sum(new_user) as newUserCount
                    from (select case when count(distinct date(create_time)) > 1 then 1 else 0 end old_user,
                                 case when count(distinct date(create_time)) = 1 and max(create_time) >= #{param.startDate} and
                                 max(create_time)<=#{param.endDate} then 1 else 0 end new_user
                    from t_link_access_logs where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
                    group by user)as user_counts;
            """)
    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param")ShortLinkStatsReqDTO statsReqDTO);

    @Select("""
            select sum(old_user) as oldUserCount, sum(new_user) as newUserCount
                    from (select case when count(distinct date(create_time)) > 1 then 1 else 0 end old_user,
                                 case when count(distinct date(create_time)) = 1 and max(create_time) >= #{param.startDate} and
                                 max(create_time)<=#{param.endDate} then 1 else 0 end new_user
                    from t_link_access_logs where gid = #{param.gid}
                    group by user)as user_counts;
            """)
    HashMap<String, Object> findUvTypeCntByShortGroup(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);

    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='accessLogUsers' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>")
    List<Map<String, Object>> selectUvTypeByUsers(@Param("gid") String gid,
                                                  @Param("fullShortUrl") String fullShortUrl,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("accessLogUsers") List<String> accessLogUsers);

    /**
     * 根据分组获取用户信息
     */
    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    gid = #{gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='accessLogUsers' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>")
    List<Map<String, Object>> selectGroupUvTypeByUsers(@Param("gid") String gid,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("accessLogUsers") List<String> accessLogUsers);

    /**
     * 根据短链接获取指定日期内的PV,UV,UIP数据
     */
    @Select("""
            select  COUNT(user) as pv, COUNT(distinct user) as uv, COUNT(distinct ip) as uip
            from t_link_access_logs
            where full_short_url = #{param.fullShortUrl} and gid = #{param.gid}
            AND create_time BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59')
            group by full_short_url, gid;
            """)
    LinkAccessStatsDO findPvUvUipStatsByShortLink(@Param("param") ShortLinkStatsReqDTO statsReqDTO);


    /**
     * 根据分组获取指定日期内星期监控数据
     */
    @Select("""
            select  COUNT(user) as pv, COUNT(distinct user) as uv, COUNT(distinct ip) as uip
            from t_link_access_logs
            where gid = #{param.gid}
            AND create_time BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59')
            group by gid;
            """)
    LinkAccessStatsDO pvUvUipStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO shortLinkGroupStatsReqDTO);



}
