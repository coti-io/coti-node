package io.coti.basenode.crypto;

import io.coti.basenode.data.HistoryNodeVote;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class HistoryNodeVoteCrypto extends SignatureCrypto<HistoryNodeVote>
{
    @Override
    public byte[] getSignatureMessage(HistoryNodeVote historyNodeVote) {

        byte[] transactionHashInBytes = historyNodeVote.getHash().getBytes();

        ByteBuffer validTransactionBuffer = ByteBuffer.allocate(1);
        validTransactionBuffer.put(historyNodeVote.isValidRequest() ? (byte) 1 : (byte) 0);

        ByteBuffer historyNodeVoteMessageBuffer = ByteBuffer.allocate(transactionHashInBytes.length + 1).
                put(transactionHashInBytes).put(validTransactionBuffer.array());

        byte[] historyNodeVoteMessageInBytes = historyNodeVoteMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(historyNodeVoteMessageInBytes).getBytes();
        return cryptoHashedMessage;

    }
}
