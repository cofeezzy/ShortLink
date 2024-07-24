package com.zzy.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import com.zzy.shortLink.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 统计数据自增
     */
    @Update("""
            update t_link set total_pv = total_pv + #{totalPv}, total_uv = total_uv + #{totalUv}, total_uip = total_uip + #{totalUip}
            where gid = #{gid} and full_short_url = #{fullShortUrl}
            """)
    void incrementStats(@Param("totalPv") Integer totalPv,
                        @Param("totalUv") Integer totalUv,
                        @Param("totalUip") Integer totalUip,
                        @Param("gid") String gid,
                        @Param("fullShortUrl") String fullShortUrl);

    /**
     * 分页统计
     */
    IPage<ShortLinkDO> pageLink(ShortLinkPageReqDTO shortLinkPageReqDTO);
}
