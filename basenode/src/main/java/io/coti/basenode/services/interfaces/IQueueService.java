package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface IQueueService {
    void addToTccQueue(Hash hash);

    ConcurrentLinkedQueue<Hash> getTccQueue();

    void removeTccQueue();

    void addToUpdateBalanceQueue(TccInfo tccInfo);

    ConcurrentLinkedQueue<TccInfo> getUpdateBalanceQueue();

}
