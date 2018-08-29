package io.coti.basenode.communication;

import io.coti.basenode.data.NodeType;

public class Channel {
    public static String getChannelString(Class<?> classType, NodeType nodeType) {
        return classType.getName() + nodeType.name();
    }
}
