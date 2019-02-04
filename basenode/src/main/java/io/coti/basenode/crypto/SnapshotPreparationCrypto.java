package io.coti.basenode.crypto;

import io.coti.basenode.data.SnapshotPreparationData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class SnapshotPreparationCrypto extends SignatureCrypto<SnapshotPreparationData> {

    @Override
    public byte[] getSignatureMessage(SnapshotPreparationData snapshotPreparationData) {

        int byteBufferLength = 0;

        Long lastDspConfirmed = snapshotPreparationData.getLastDspConfirmed();
        byteBufferLength += Long.BYTES;

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(byteBufferLength).putLong(lastDspConfirmed);

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}

