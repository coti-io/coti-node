package io.coti.basenode.crypto.interfaces;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface IBaseTransactionCrypto {
    byte[] getMessageInBytes(BaseTransactionData baseTransactionData);

    void setBaseTransactionHash(BaseTransactionData baseTransactionData) throws ClassNotFoundException;

    byte[] getSignatureMessage(TransactionData transactionData) throws ClassNotFoundException;

    byte[] getSignatureMessage(TransactionData transactionData, TrustScoreNodeResultData trustScoreNodeResultData) throws ClassNotFoundException;

    void signMessage(TransactionData transactionData, BaseTransactionData baseTransactionData, int index) throws ClassNotFoundException;

    <T extends BaseTransactionData & ITrustScoreNodeValidatable> void signMessage(TransactionData transactionData, T baseTransactionData, TrustScoreNodeResultData trustScoreNodeResultData) throws ClassNotFoundException;

    String getPublicKey(BaseTransactionData receiverBaseTransactionData);

    String getPublicKey(TrustScoreNodeResultData trustScoreNodeResultData);

    Hash createBaseTransactionHashFromData(BaseTransactionData baseTransactionData);

    boolean isBaseTransactionValid(TransactionData transactionData, BaseTransactionData baseTransactionData);

    boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) throws ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException;

}
