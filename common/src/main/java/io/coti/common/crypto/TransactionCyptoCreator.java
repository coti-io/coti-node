package io.coti.common.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import lombok.Data;


@Data
public class TransactionCyptoCreator {

    private TransactionData txData;
    private TransactionCryptoWrapper transactionCryptoWrapper;

    public TransactionCyptoCreator(TransactionData tx) {
        this.txData = tx;
        this.transactionCryptoWrapper = new TransactionCryptoWrapper(tx);
    }

    public void signTransaction() {

        if (txData.getHash() == null)
            transactionCryptoWrapper.setTransactionHash();

        for (BaseTransactionData bxData : txData.getBaseTransactions()) {

            if (bxData instanceof IPrivateKey) {
                String privateKey = ((IPrivateKey) bxData).getPrivateKey();

                SignatureData signatureData = CryptoHelper.SignBytes(this.txData.getHash().getBytes(), privateKey);
                bxData.setSignatureData(signatureData);
            }
        }
    }
}
