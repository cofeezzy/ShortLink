package com.zzy.shortLink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.shortLink.project.dao.entity.LinkStatsTodayDO;
import com.zzy.shortLink.project.dao.mapper.LinkStatsTodayMapper;
import com.zzy.shortLink.project.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}
