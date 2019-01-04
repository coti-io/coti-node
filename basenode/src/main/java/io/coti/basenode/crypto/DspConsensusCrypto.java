package io.coti.basenode.crypto;

import io.coti.basenode.data.DspConsensusResult;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Date;

@Service
public class DspConsensusCrypto extends SignatureCrypto<DspConsensusResult> {
    @Override
    public byte[] getSignatureMessage(DspConsensusResult dspConsensusResult) {
        byte[] transactionHashInBytes = dspConsensusResult.getTransactionHash().getBytes();

        ByteBuffer indexBuffer = ByteBuffer.allocate(8);
        indexBuffer.putLong(dspConsensusResult.getIndex());

        Date indexingTime = dspConsensusResult.getIndexingTime();
        int timestamp = (int) (indexingTime.getTime());

        ByteBuffer indexingTimeBuffer = ByteBuffer.allocate(4);
        indexingTimeBuffer.putInt(timestamp);

        ByteBuffer dspConsensusMessageBuffer = ByteBuffer.allocate(transactionHashInBytes.length + 8 + 4).
                put(transactionHashInBytes).put(indexBuffer.array()).put(indexingTimeBuffer.array());

        byte[] dspConsensusMessageInBytes = dspConsensusMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(dspConsensusMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
