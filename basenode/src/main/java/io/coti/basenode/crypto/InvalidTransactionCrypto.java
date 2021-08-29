package io.coti.basenode.crypto;

import io.coti.basenode.data.InvalidTransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Slf4j
@Component
public class InvalidTransactionCrypto extends SignatureCrypto<InvalidTransactionData> {

    @Override
    public byte[] getSignatureMessage(InvalidTransactionData invalidTransactionData) {
        byte[] invalidTransactionHashInBytes = invalidTransactionData.getHash().getBytes();
        byte[] invalidationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(invalidTransactionData.getInvalidationTime().toEpochMilli()).array();
        ByteBuffer transactionBuffer = ByteBuffer.allocate(invalidTransactionHashInBytes.length + invalidationTimeInBytes.length)
                .put(invalidTransactionHashInBytes).put(invalidationTimeInBytes);
        return CryptoHelper.cryptoHash(transactionBuffer.array()).getBytes();
    }
}
