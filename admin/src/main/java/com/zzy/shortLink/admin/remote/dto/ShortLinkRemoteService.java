package com.zzy.shortLink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzy.shortLink.admin.common.convention.result.Result;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.zzy.shortLink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.zzy.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短链接
     * @param shortLinkCreateReqDTO 创建短链接请求参数
     * @return
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO){
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(shortLinkCreateReqDTO));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 修改短链接
     * @param reqDTO 修改短链接请求参数
     * @return
     */
    default void updateShortLink(ShortLinkUpdateReqDTO reqDTO){
       HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(reqDTO));
    };

    /**
     * 分页查询短链接
     * @param shortLinkPageReqDTO 分页查询短链接请求参数
     * @return
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO){
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", shortLinkPageReqDTO.getGid());
        requestMap.put("current", shortLinkPageReqDTO.getCurrent());
        requestMap.put("size", shortLinkPageReqDTO.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>(){
        });
    }

    /**
     * 查询分组短链接总量
     * @param requestParam 分页查询短链接请求参数
     * @return
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam){
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>(){
        });
    }


}
