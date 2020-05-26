package io.coti.basenode.crypto;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class GeneralVoteCrypto extends SignatureCrypto<GeneralVoteMessage> {

    @Override
    public byte[] getSignatureMessage(GeneralVoteMessage generalVoteMessage) {
        byte[] stateMessageInBytes = generalVoteMessage.getMessagePayload().getMessageInBytes();

        ByteBuffer broadcastDataBuffer = ByteBuffer.allocate(Long.BYTES + stateMessageInBytes.length + 1)
                .putLong(generalVoteMessage.getCreateTime().toEpochMilli()).put(stateMessageInBytes).put(generalVoteMessage.isVote() ? (byte) 1 : (byte) 0);
        return CryptoHelper.cryptoHash(broadcastDataBuffer.array()).getBytes();
    }
}
