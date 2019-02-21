package io.coti.basenode.crypto;

import io.coti.basenode.data.DspClusterStampVoteData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DspClusterStampVoteCrypto extends SignatureCrypto<DspClusterStampVoteData> {

    @Override
    public byte[] getSignatureMessage(DspClusterStampVoteData dspClusterStampVoteData) {

        int byteBufferLength = 0;

        byte[] clusterStampHashInBytes = dspClusterStampVoteData.getHash().getBytes();
        byteBufferLength += clusterStampHashInBytes.length;

        ByteBuffer isDValidaClusterStampInBytes = ByteBuffer.allocate(1);
        isDValidaClusterStampInBytes.put(dspClusterStampVoteData.isValidClusterStamp() ? (byte) 1 : (byte) 0);
        byteBufferLength++;

        ByteBuffer clusterStampVoteDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(clusterStampHashInBytes)
                .put(isDValidaClusterStampInBytes.array());

        byte[] clusterStampVoteDataInBytes = clusterStampVoteDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampVoteDataInBytes).getBytes();
    }
}

