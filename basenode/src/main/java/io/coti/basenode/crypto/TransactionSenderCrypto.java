package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class TransactionSenderCrypto extends SignatureValidationCrypto<TransactionData> {
    @Override
    public byte[] getSignatureMessage(TransactionData transactionData) {
        byte[] transactionHashInBytes = transactionData.getHash().getBytes();
        byte[] transactionTypeInBytes = transactionData.getType().toString().getBytes();
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(transactionData.getCreateTime().toEpochMilli()).array();
        byte[] transactionDescriptionInBytes = transactionData.getTransactionDescription().getBytes(StandardCharsets.UTF_8);

        ByteBuffer transactionSenderBuffer = ByteBuffer.allocate(transactionHashInBytes.length + transactionTypeInBytes.length + createTimeInBytes.length + transactionDescriptionInBytes.length)
                .put(transactionHashInBytes).put(transactionTypeInBytes).put(createTimeInBytes).put(transactionDescriptionInBytes);

        return CryptoHelper.cryptoHash(transactionSenderBuffer.array()).getBytes();
    }

    @Override
    public SignatureData getSignature(TransactionData transactionData) {
        return transactionData.getSenderSignature();
    }

    @Override
    public Hash getSignerHash(TransactionData transactionData) {
        return transactionData.getSenderHash();
    }
}
