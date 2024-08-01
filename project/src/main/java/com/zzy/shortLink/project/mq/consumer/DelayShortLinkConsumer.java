package com.zzy.shortLink.project.mq.consumer;

import com.zzy.shortLink.project.common.constant.RedisKeyConstant;
import com.zzy.shortLink.project.common.convention.exception.ServiceException;
import com.zzy.shortLink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.zzy.shortLink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.zzy.shortLink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelayShortLinkConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    public void onMessage(){
        Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("delay-short-link-consumer");
            thread.setDaemon(Boolean.TRUE);// 设置线程为守护线程
            return thread;
        }).execute(() ->{
            RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(RedisKeyConstant.DELAY_QUEUE_STATS_KEY);
            RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            // 无限循环
            for(; ; ){
                try {
                    ShortLinkStatsRecordDTO statsRecordDTO = delayedQueue.poll(); // 尝试从延迟队列中获取元素
                    if(statsRecordDTO != null){
                        if(messageQueueIdempotentHandler.isMessageBeingConsumed(statsRecordDTO.getKeys())){
                            if(messageQueueIdempotentHandler.isAccomplish(statsRecordDTO.getKeys())){
                                return;
                            }
                            throw new ServiceException("消息未完成流程，需要消息队列重试");
                        }
                        try {
                            shortLinkService.shortLinkStats(null, null, statsRecordDTO);
                        }catch (Throwable ignored){
                            messageQueueIdempotentHandler.delMessageProcessed(statsRecordDTO.getKeys());
                            log.error("延迟记录短链接监控消费异常", ignored);
                        }
                        messageQueueIdempotentHandler.setAccomplish(statsRecordDTO.getKeys());
                        continue;
                    }
                    LockSupport.parkUntil(500);
                }catch (Throwable ignored){

                }
            }
        });
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();

    }
}
