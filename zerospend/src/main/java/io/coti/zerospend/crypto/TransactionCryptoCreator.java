package io.coti.zerospend.crypto;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionCryptoCreator {
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;

    public void signBaseTransactions(TransactionData transactionData) {

        if (transactionData.getHash() == null) {
            transactionCrypto.setTransactionHash(transactionData);
        }
        transactionData.getBaseTransactions().forEach(baseTransactionData -> baseTransactionData.setSignatureData(nodeCryptoHelper.signMessage(transactionData.getHash().getBytes())));

    }

    public Hash getAddress() {
        return nodeCryptoHelper.getNodeAddress();
    }
}