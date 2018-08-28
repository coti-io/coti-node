package unitTest.crypto;

import io.coti.common.data.DspVote;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DspVoteCrypto extends SignatureCrypto<DspVote> {
    @Override
    public byte[] getMessageInBytes(DspVote dspVote) {

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
