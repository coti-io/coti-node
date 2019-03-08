package io.coti.basenode.communication;

import io.coti.basenode.data.NodeType;

public class Channel {
    public static String getChannelString(Class<?> classType, NodeType publisherType, NodeType subscriberType) {
        return classType.getName() + ":" + publisherType.name() + ":" + subscriberType.name();
    }
}
