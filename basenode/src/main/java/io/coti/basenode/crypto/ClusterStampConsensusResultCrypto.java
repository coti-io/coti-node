package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampConsensusResult;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampConsensusResultCrypto extends SignatureCrypto<ClusterStampConsensusResult> {

    @Override
    public byte[] getSignatureMessage(ClusterStampConsensusResult clusterStampConsensusResult) {

        int byteBufferLength = 0;

        byte[] clusterStampHashInBytes = clusterStampConsensusResult.getHash().getBytes();
        byteBufferLength += clusterStampHashInBytes.length;

        byte[] isDspConsensusInBytes = (clusterStampConsensusResult.isDspConsensus() + "").getBytes();
        byteBufferLength += isDspConsensusInBytes.length;

        ByteBuffer clusterStampConsensusResultBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(clusterStampHashInBytes)
                .put(isDspConsensusInBytes);

        byte[] clusterStampConsensusResultInBytes = clusterStampConsensusResultBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampConsensusResultInBytes).getBytes();
    }
}

