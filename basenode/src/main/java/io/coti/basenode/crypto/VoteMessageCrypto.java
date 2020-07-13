package io.coti.basenode.crypto;

import io.coti.basenode.data.messages.VoteMessageData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class VoteMessageCrypto extends SignatureCrypto<VoteMessageData> {

    @Override
    public byte[] getSignatureMessage(VoteMessageData voteMessage) {
        byte[] stateMessageInBytes = voteMessage.getMessageInBytes();
        byte[] voteHashInBytes = voteMessage.getVoteHash().getBytes();

        ByteBuffer broadcastDataBuffer = ByteBuffer.allocate(Long.BYTES + stateMessageInBytes.length + voteHashInBytes.length + 1)
                .putLong(voteMessage.getCreateTime().toEpochMilli()).put(stateMessageInBytes).put(voteHashInBytes).put(voteMessage.isVote() ? (byte) 1 : (byte) 0);
        return CryptoHelper.cryptoHash(broadcastDataBuffer.array()).getBytes();
    }
}
