package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.service.interfaces.IQueueService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class QueueService implements IQueueService {

    private ConcurrentLinkedQueue<Hash> tccQueue;

    private ConcurrentLinkedQueue<Hash> updateBalanceQueue;

    private ConcurrentLinkedQueue<Hash> transactionsQueue;

    @PostConstruct
    private void init(){
        tccQueue = new ConcurrentLinkedQueue<>();
        updateBalanceQueue = new ConcurrentLinkedQueue<>();
        transactionsQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void addToTccQueue(Hash hash) {
        tccQueue.add(hash);
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
    public void addToTransactionQueue(Hash hash) {
        transactionsQueue.add(hash);
    }

    @Override
    public ConcurrentLinkedQueue<Hash> getTransactionQueue() {
        return transactionsQueue;
    }

    @Override
    public void addToUpdateBalanceQueue(Hash hash) {
        updateBalanceQueue.add(hash);
    }

    @Override
    public ConcurrentLinkedQueue<Hash> getUpdateBalanceQueue() {
        return updateBalanceQueue;
    }
}
