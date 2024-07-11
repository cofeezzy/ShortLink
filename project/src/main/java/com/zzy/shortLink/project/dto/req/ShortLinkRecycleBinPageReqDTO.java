package com.zzy.shortLink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzy.shortLink.project.dao.entity.ShortLinkDO;
import lombok.Data;

import java.util.List;

@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {

    private List<String> gidList;
}
