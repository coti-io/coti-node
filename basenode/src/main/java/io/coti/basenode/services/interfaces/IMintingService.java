package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;

public interface IMintingService {

    void init();

    boolean checkMintingAmountAndAddToRequestedAmount(TransactionData transactionData);

    void revertMintingRequestedReserve(TransactionData transactionData);

    void updateMintedAmount(TransactionData transactionData);
}
