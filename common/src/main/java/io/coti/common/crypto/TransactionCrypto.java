package io.coti.common.crypto;

import io.coti.common.data.TransactionData;
import org.springframework.stereotype.Service;

@Service
public class TransactionCrypto extends SignatureCreationCrypto<TransactionData> {

    @Override
    public byte[] getMessageInBytes(TransactionData transactionData) {
        return transactionData.getHash().getBytes();
    }
}
