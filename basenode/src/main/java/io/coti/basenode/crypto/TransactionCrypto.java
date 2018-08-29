package io.coti.basenode.crypto;

import io.coti.basenode.data.TransactionData;
import org.springframework.stereotype.Service;

@Service
public class TransactionCrypto extends SignatureCrypto<TransactionData> {

    @Override
    public byte[] getMessageInBytes(TransactionData transactionData) {
        return transactionData.getHash().getBytes();
    }
}
