package io.coti.basenode.crypto;

import io.coti.basenode.data.messages.VoteMessageData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class VoteMessageCrypto extends SignatureCrypto<VoteMessageData> {

    @Override
    public byte[] getSignatureMessage(VoteMessageData generalVoteMessage) {
        byte[] stateMessageInBytes = generalVoteMessage.getMessageInBytes();
        byte[] voteHashInBytes = generalVoteMessage.getVoteHash().getBytes();

        ByteBuffer broadcastDataBuffer = ByteBuffer.allocate(Long.BYTES + stateMessageInBytes.length + voteHashInBytes.length + 1)
                .putLong(generalVoteMessage.getCreateTime().toEpochMilli()).put(stateMessageInBytes).put(voteHashInBytes).put(generalVoteMessage.isVote() ? (byte) 1 : (byte) 0);
        return CryptoHelper.cryptoHash(broadcastDataBuffer.array()).getBytes();
    }
}
