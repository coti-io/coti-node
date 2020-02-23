package io.coti.basenode.communication;

import io.coti.basenode.data.NodeType;

public class Channel {

    private Channel() {

    }

    public static String getChannelString(Class<?> classType, String publisherAddressAndPort, NodeType publisherType, NodeType subscriberType) {
        return getChannelString(classType, publisherAddressAndPort) + "-" + publisherType.name() + "-" + subscriberType.name();
    }

    public static String getChannelString(Class<?> classType, String publisherAddressAndPort) {
        return classType.getName() + "-" + publisherAddressAndPort;
    }
}
