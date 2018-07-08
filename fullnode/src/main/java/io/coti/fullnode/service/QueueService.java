package io.coti.fullnode.service;

import io.coti.common.data.Hash;
import io.coti.fullnode.service.interfaces.IQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class QueueService implements IQueueService {

    private ConcurrentLinkedQueue<Hash> tccQueue;

    private ConcurrentLinkedQueue<Hash> updateBalanceQueue;

    @PostConstruct
    private void init(){
        tccQueue = new ConcurrentLinkedQueue<>();
        updateBalanceQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void addToTccQueue(Hash hash) {
        tccQueue.add(hash);
        log.info("Hash {} , was added to tccQueue", hash);
    }

    @Override
    public ConcurrentLinkedQueue<Hash> getTccQueue() {
        return tccQueue;
    }

    @Override
    public void  removeTccQueue() {
        tccQueue.clear();
    }

    @Override
    public void addToUpdateBalanceQueue(Hash hash) {
        updateBalanceQueue.add(hash);
        log.info("Hash {} , was added to updateBalanceQueue", hash);
    }

    @Override
    public ConcurrentLinkedQueue<Hash> getUpdateBalanceQueue() {
        return updateBalanceQueue;
    }
}
