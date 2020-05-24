package io.coti.basenode.crypto;

import io.coti.basenode.data.messages.GeneralMessage;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class GeneralMessageCrypto extends SignatureCrypto<GeneralMessage> {

    @Override
    public byte[] getSignatureMessage(GeneralMessage generalMessage) {
        byte[] stateMessageInBytes = generalMessage.getMessagePayload().getMessageInBytes();

        ByteBuffer broadcastDataBuffer = ByteBuffer.allocate(Long.BYTES + stateMessageInBytes.length)
                .putLong(generalMessage.getCreateTime().toEpochMilli()).put(stateMessageInBytes);
        return CryptoHelper.cryptoHash(broadcastDataBuffer.array()).getBytes();
    }
}
