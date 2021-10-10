package io.coti.basenode.crypto;

import io.coti.basenode.data.RejectedTransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Slf4j
@Component
public class RejectedTransactionCrypto extends SignatureCrypto<RejectedTransactionData> {

    @Override
    public byte[] getSignatureMessage(RejectedTransactionData rejectedTransactionData) {
        byte[] rejectedTransactionHashInBytes = rejectedTransactionData.getHash().getBytes();
        byte[] rejectionTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(rejectedTransactionData.getRejectionTime().toEpochMilli()).array();
        ByteBuffer transactionBuffer = ByteBuffer.allocate(rejectedTransactionHashInBytes.length + rejectionTimeInBytes.length)
                .put(rejectedTransactionHashInBytes).put(rejectionTimeInBytes);
        return CryptoHelper.cryptoHash(transactionBuffer.array()).getBytes();
    }
}
