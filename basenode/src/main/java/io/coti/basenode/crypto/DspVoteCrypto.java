package io.coti.basenode.crypto;

import io.coti.basenode.data.DspVote;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DspVoteCrypto extends SignatureCrypto<DspVote> {
    @Override
    public byte[] getSignatureMessage(DspVote dspVote) {

        byte[] transactionHashInBytes = dspVote.getTransactionHash().getBytes();

        ByteBuffer validTransactionBuffer = ByteBuffer.allocate(1);
        validTransactionBuffer.put(dspVote.isValidTransaction() ? (byte) 1 : (byte) 0);

        ByteBuffer dspVoteMessageBuffer = ByteBuffer.allocate(transactionHashInBytes.length + 1).
                put(transactionHashInBytes).put(validTransactionBuffer.array());

        byte[] dspVoteMessageInBytes = dspVoteMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(dspVoteMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
