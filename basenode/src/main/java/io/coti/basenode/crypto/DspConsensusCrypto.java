package io.coti.basenode.crypto;

import io.coti.basenode.data.DspConsensusResult;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Instant;

@Component
public class DspConsensusCrypto extends SignatureCrypto<DspConsensusResult> {

    @Override
    public byte[] getSignatureMessage(DspConsensusResult dspConsensusResult) {
        byte[] transactionHashInBytes = dspConsensusResult.getTransactionHash().getBytes();

        byte[] indexInBytes = ByteBuffer.allocate(Long.BYTES).putLong(dspConsensusResult.getIndex()).array();

        Instant indexingTime = dspConsensusResult.getIndexingTime();
        byte[] indexingTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(indexingTime.toEpochMilli()).array();

        ByteBuffer dspConsensusMessageBuffer = ByteBuffer.allocate(transactionHashInBytes.length + indexInBytes.length + indexingTimeInBytes.length).
                put(transactionHashInBytes).put(indexInBytes).put(indexingTimeInBytes);

        byte[] dspConsensusMessageInBytes = dspConsensusMessageBuffer.array();
        return CryptoHelper.cryptoHash(dspConsensusMessageInBytes).getBytes();
    }
}
