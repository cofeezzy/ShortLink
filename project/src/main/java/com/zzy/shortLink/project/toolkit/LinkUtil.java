package com.zzy.shortLink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.zzy.shortLink.project.common.constant.ShortLinkConstant.DEFAULT_LINK_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短链接缓存有效时间
     * @param validateDate 有效期时间
     * @return 有效期时间戳
     */
    public static long getLinkCacheValidTime(Date validateDate){
        return Optional.ofNullable(validateDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_LINK_CACHE_VALID_TIME);
    }
}
