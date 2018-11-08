package io.coti.basenode.crypto.interfaces;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

public interface IBaseTransactionCrypto {
    byte[] getMessageInBytes(BaseTransactionData baseTransactionData);
    void setBaseTransactionHash(BaseTransactionData baseTransactionData);
    byte[] getSignatureMessage(TransactionData transactionData) throws ClassNotFoundException;
    String getPublicKey(BaseTransactionData baseTransactionData);
    Hash createBaseTransactionHashFromData(BaseTransactionData baseTransactionData);
    boolean isBaseTransactionValid(TransactionData transactionData, BaseTransactionData baseTransactionData);
    boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData);

}
