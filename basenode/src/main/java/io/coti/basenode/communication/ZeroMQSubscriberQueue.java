package io.coti.basenode.communication;

import io.coti.basenode.communication.data.ZeroMQMessageData;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public enum ZeroMQSubscriberQueue {
    HEARTBEAT(new HashSet<>(Collections.singletonList(PublisherHeartBeatData.class))),
    NETWORK(new HashSet<>(Collections.singletonList(NetworkData.class))),
    ADDRESS(new HashSet<>(Collections.singletonList(AddressData.class))),
    TRANSACTION(new HashSet<>(Arrays.asList(TransactionData.class, DspConsensusResult.class)));

    private final BlockingQueue<ZeroMQMessageData> queue = new LinkedBlockingQueue<>();

    private static class ZeroMQSubscriberQueues {
        private static final Map<Class<? extends IPropagatable>, ZeroMQSubscriberQueue> messageTypeToQueueMap = new HashMap<>();
    }

    ZeroMQSubscriberQueue(Set<Class<? extends IPropagatable>> messageTypeSet) {
        messageTypeSet.forEach(messageType -> ZeroMQSubscriberQueues.messageTypeToQueueMap.put(messageType, this));

    }

    public BlockingQueue<ZeroMQMessageData> getQueue() {
        return this.queue;
    }

    public static <T extends IPropagatable> BlockingQueue<ZeroMQMessageData> getQueue(Class<T> messageType) {
        return getQueueEnum(messageType).queue;
    }

    public static <T extends IPropagatable> ZeroMQSubscriberQueue getQueueEnum(Class<T> messageType) {
        return ZeroMQSubscriberQueues.messageTypeToQueueMap.get(messageType);
    }

}
