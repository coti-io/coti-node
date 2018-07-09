package io.coti.common.services.interfaces;

import io.coti.common.data.Hash;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface IQueueService {
    void addToTccQueue(Hash hash);

    ConcurrentLinkedQueue<Hash> getTccQueue();

    void removeTccQueue();

    void addToUpdateBalanceQueue(Hash hash);

    ConcurrentLinkedQueue<Hash> getUpdateBalanceQueue();

}
