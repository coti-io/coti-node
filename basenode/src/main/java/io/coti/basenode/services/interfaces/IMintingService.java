package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

public interface IMintingService {

    void init();

    boolean checkMintingAmountAndUpdateMintableAmount(TransactionData transactionData);

    void revertMintingAllocation(TransactionData transactionData);

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    void doTokenMinting(TransactionData transactionData);
}
