package io.coti.basenode.crypto;

import io.coti.basenode.data.GeneralVoteResult;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class GeneralVoteCrypto extends SignatureCrypto<GeneralVoteResult> {

    @Override
    public byte[] getSignatureMessage(GeneralVoteResult generalVoteResult) {
        byte[] generalVoteTypeBytes = generalVoteResult.getGeneralVoteType().name().getBytes();
        byte[] generalVoteMessageBytes = generalVoteResult.getGeneralVoteMessage().getBytes();
        byte[] generalVoteHashBytes = generalVoteResult.getHash().getBytes();

        ByteBuffer generalVoteBuffer = ByteBuffer.allocate(generalVoteTypeBytes.length + generalVoteMessageBytes.length + generalVoteHashBytes.length)
                .put(generalVoteTypeBytes).put(generalVoteMessageBytes).put(generalVoteHashBytes);
        return CryptoHelper.cryptoHash(generalVoteBuffer.array()).getBytes();
    }
}
