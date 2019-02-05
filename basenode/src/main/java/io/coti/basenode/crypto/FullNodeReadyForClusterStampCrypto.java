package io.coti.basenode.crypto;

import io.coti.basenode.data.FullNodeReadyForClusterStampData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class FullNodeReadyForClusterStampCrypto extends SignatureCrypto<FullNodeReadyForClusterStampData> {

    @Override
    public byte[] getSignatureMessage(FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData) {

        int byteBufferLength = 0;

        Long lastDspConfirmed = fullNodeReadyForClusterStampData.getLastDspConfirmed();
        byteBufferLength += Long.BYTES;

        ByteBuffer clusterStampReadyDataBuffer = ByteBuffer.allocate(byteBufferLength).putLong(lastDspConfirmed);

        byte[] clusterStampReadyDataInBytes = clusterStampReadyDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampReadyDataInBytes).getBytes();
    }
}

