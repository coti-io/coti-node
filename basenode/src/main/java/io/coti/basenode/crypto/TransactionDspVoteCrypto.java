package io.coti.basenode.crypto;

import io.coti.basenode.data.TransactionDspVote;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class TransactionDspVoteCrypto extends SignatureCrypto<TransactionDspVote> {

    @Override
    public byte[] getSignatureMessage(TransactionDspVote transactionDspVote) {

        byte[] transactionHashInBytes = transactionDspVote.getTransactionHash().getBytes();

        ByteBuffer validTransactionBuffer = ByteBuffer.allocate(1);
        validTransactionBuffer.put(transactionDspVote.isValidTransaction() ? (byte) 1 : (byte) 0);

        ByteBuffer dspVoteMessageBuffer = ByteBuffer.allocate(transactionHashInBytes.length + 1).
                put(transactionHashInBytes).put(validTransactionBuffer.array());

        return CryptoHelper.cryptoHash(dspVoteMessageBuffer.array()).getBytes();
    }
}
