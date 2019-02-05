package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampPreparationData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampPreparationCrypto extends SignatureCrypto<ClusterStampPreparationData> {

    @Override
    public byte[] getSignatureMessage(ClusterStampPreparationData clusterStampPreparationData) {

        int byteBufferLength = 0;

        Long lastDspConfirmed = clusterStampPreparationData.getLastDspConfirmed();
        byteBufferLength += Long.BYTES;

        ByteBuffer clusterStampDataBuffer = ByteBuffer.allocate(byteBufferLength).putLong(lastDspConfirmed);

        byte[] clusterStampPreparationDataInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampPreparationDataInBytes).getBytes();
    }
}

