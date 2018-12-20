package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.BaseTransactionData;
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

        try {
            if (transactionData.getHash() == null) {
                transactionCrypto.setTransactionHash(transactionData);
            }
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
                BaseTransactionCrypto.valueOf(baseTransactionData.getClass().getSimpleName()).signMessage(transactionData, baseTransactionData);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Hash getAddress() {
        return nodeCryptoHelper.getNodeAddress();
    }
}