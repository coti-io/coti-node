package io.coti.basenode.crypto;

import io.coti.basenode.data.messages.StateMessageData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class StateMessageCrypto extends SignatureCrypto<StateMessageData> {

    @Override
    public byte[] getSignatureMessage(StateMessageData stateMessageData) {
        byte[] stateMessageInBytes = stateMessageData.getMessageInBytes();

        ByteBuffer broadcastDataBuffer = ByteBuffer.allocate(Long.BYTES + stateMessageInBytes.length)
                .putLong(stateMessageData.getCreateTime().toEpochMilli()).put(stateMessageInBytes);
        return CryptoHelper.cryptoHash(broadcastDataBuffer.array()).getBytes();
    }
}
