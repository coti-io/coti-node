package io.coti.zerospend.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.Interfaces.IPrivateKey;
import io.coti.basenode.crypto.TransactionCryptoWrapper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import lombok.Data;


@Data
public class TransactionCryptoCreator {

    private TransactionData transactionData;
    private TransactionCryptoWrapper transactionCryptoWrapper;

    public TransactionCryptoCreator(TransactionData transactionData) {
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