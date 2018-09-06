package io.coti.zerospend.crypto;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCryptoWrapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionCryptoCreator {
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    public void signBaseTransactions(TransactionData transactionData) {

        if (transactionData.getHash() == null) {
            TransactionCryptoWrapper transactionCryptoWrapper = new TransactionCryptoWrapper(transactionData);
            transactionCryptoWrapper.setTransactionHash();
        }
        transactionData.getBaseTransactions().forEach(baseTransactionData -> baseTransactionData.setSignatureData(nodeCryptoHelper.signMessage(transactionData.getHash().getBytes())));

    }

    public Hash getAddress() {
        return nodeCryptoHelper.getNodeAddress();
    }
}