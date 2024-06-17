package com.zzy.shortLink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.shortLink.admin.dao.entity.GroupDO;
import com.zzy.shortLink.admin.dao.mapper.GroupMapper;
import com.zzy.shortLink.admin.service.GroupService;
import com.zzy.shortLink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (!hasGid(gid));
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .username(groupName)
                .build();

        baseMapper.insert(groupDO);
    }

    private boolean hasGid(String gid){

        LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getGid, gid)
                //TODO 设置用户名
                .eq(GroupDO::getUsername, null);
        GroupDO hasGroup = baseMapper.selectOne(eq);
        return hasGroup == null;
    }

}
