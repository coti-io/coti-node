package io.coti.basenode.communication;

import io.coti.basenode.data.NodeType;

public class Channel {

    public static String getChannelString(Class<?> classType, NodeType publisherType, NodeType subscriberType, String publisherAddressAndPort) {
        return getChannelString(classType, publisherAddressAndPort) + "-" + publisherType.name() + "-" + subscriberType.name();
    }

    public static String getChannelString(Class<?> classType, String publisherAddressAndPort) {
        return classType.getName() + "-" + publisherAddressAndPort;
    }
}
