package com.zzy.shortLink.project.mq.producer;

import cn.hutool.core.lang.UUID;
import com.zzy.shortLink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.zzy.shortLink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissionClient;


    /**
     * 延迟发送统计信息
     * @param statsRecordDTO
     */
    public void send(ShortLinkStatsRecordDTO statsRecordDTO){
        statsRecordDTO.setKeys(UUID.fastUUID().toString());
        RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissionClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
        RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissionClient.getDelayedQueue(blockingDeque);
        delayedQueue.offer(statsRecordDTO, 5, TimeUnit.SECONDS);
    }
}
