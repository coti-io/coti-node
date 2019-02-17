package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampStateData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampStateCrypto extends SignatureCrypto<ClusterStampStateData> {

    @Override
    public byte[] getSignatureMessage(ClusterStampStateData clusterStampStateData) {

        int byteBufferLength = 0;

        Long lastDspConfirmed = clusterStampStateData.getTotalConfirmedTransactionsCount();
        byteBufferLength += Long.BYTES;

        ByteBuffer clusterStampDataBuffer = ByteBuffer.allocate(byteBufferLength).putLong(lastDspConfirmed);

        byte[] clusterStampInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}

