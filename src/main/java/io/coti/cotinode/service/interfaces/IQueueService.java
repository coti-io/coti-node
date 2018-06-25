package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface IQueueService {
    public void addToTccQueue(Hash hash);

    public  ConcurrentLinkedQueue<Hash> getTccQueue();

    public void removeTccQueue();

    public void addToTransactionQueue(Hash hash);

    public ConcurrentLinkedQueue<Hash> getTransactionQueue();

    public void addToUpdateBalanceQueue(Hash hash);

    public ConcurrentLinkedQueue<Hash> getUpdateBalanceQueue();

}
