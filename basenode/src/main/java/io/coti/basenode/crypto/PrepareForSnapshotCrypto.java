package io.coti.basenode.crypto;

import io.coti.basenode.data.PrepareForSnapshot;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class PrepareForSnapshotCrypto extends SignatureCrypto<PrepareForSnapshot> {

    @Override
    public byte[] getSignatureMessage(PrepareForSnapshot prepareForSnapshot) {

        int byteBufferLength = 0;

        Long lastDspConfirmed = prepareForSnapshot.getLastDspConfirmed();
        byteBufferLength += Long.BYTES;

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(byteBufferLength).putLong(lastDspConfirmed);

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}

