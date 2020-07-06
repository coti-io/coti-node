package io.coti.basenode.communication;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.data.messages.VoteMessageData;

public class Channel {

    private Channel() {

    }

    public static String getChannelString(Class<?> classType, String publisherAddressAndPort, NodeType publisherType, NodeType subscriberType) {
        return getChannelString(classType, publisherAddressAndPort) + "-" + publisherType.name() + "-" + subscriberType.name();
    }

    public static String getChannelString(Class<?> classType, String publisherAddressAndPort) {
        String classSimpleName;
        final String STATE_MESSAGE_DATA = StateMessageData.class.getSimpleName();
        final String VOTE_MESSAGE_DATA = VoteMessageData.class.getSimpleName();
        classSimpleName = classType.getSimpleName();

        if(STATE_MESSAGE_DATA.length()<=classSimpleName.length() && STATE_MESSAGE_DATA.equals(classSimpleName.substring(classSimpleName.length() - STATE_MESSAGE_DATA.length()))) {
            return StateMessageData.class.getName() + "-" + publisherAddressAndPort;
        }

        if(VOTE_MESSAGE_DATA.length()<=classSimpleName.length() && VOTE_MESSAGE_DATA.equals(classSimpleName.substring(classSimpleName.length() - VOTE_MESSAGE_DATA.length()))){
            return VoteMessageData.class.getName() + "-" + publisherAddressAndPort;
        }

        return classType.getName() + "-" + publisherAddressAndPort;
    }
}
