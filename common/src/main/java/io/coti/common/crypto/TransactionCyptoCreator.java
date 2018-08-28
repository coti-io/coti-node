package io.coti.common.crypto;

import io.coti.common.crypto.Interfaces.IPrivateKey;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import lombok.Data;


@Data
public class TransactionCyptoCreator {

    private TransactionData transactionData;
    private TransactionCryptoWrapper transactionCryptoWrapper;

    public TransactionCyptoCreator(TransactionData transactionData) {
        this.transactionData = transactionData;
        this.transactionCryptoWrapper = new TransactionCryptoWrapper(transactionData);
    }

    public void signTransaction() {

        if (transactionData.getHash() == null)
            transactionCryptoWrapper.setTransactionHash();

        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {

            if (baseTransactionData instanceof IPrivateKey) {
                String privateKey = ((IPrivateKey) baseTransactionData).getPrivateKey();

                SignatureData signatureData = CryptoHelper.SignBytes(this.transactionData.getHash().getBytes(), privateKey);
                baseTransactionData.setSignatureData(signatureData);
            }
        }
    }
}
