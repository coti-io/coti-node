package io.coti.common.data;

public enum ZMQChannel {
    ZERO_SPEND_VOTING_ANSWER("Voting answer"),
    ZERO_SPEND_ZS_TRANSACTION("Zero Spend Transaction");

    public String channelName;
    ZMQChannel(String channelName){
        this.channelName = channelName;
    }

}
