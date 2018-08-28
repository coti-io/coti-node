package io.coti.common.communication;

import io.coti.common.data.NodeType;

public class Channel {
    public static String getChannelString(Class<?> classType, NodeType nodeType) {
        return classType.getName() + nodeType.name();
    }
}
