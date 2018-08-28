package io.coti.common;

public class Channel {
    public static String getChannelString(Class<?> classType, NodeType nodeType) {
        return classType.getName() + nodeType.name();
    }
}
