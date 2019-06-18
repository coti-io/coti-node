package io.coti.basenode.communication.data;

import lombok.Data;

@Data
public class ZeroMQMessageData {
    private String channel;
    private byte[] message;

    public ZeroMQMessageData(String channel, byte[] message) {
        this.channel = channel;
        this.message = message;
    }
}
