package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.math.BigDecimal;

public interface IMintingService {

    void init();

    boolean checkMintingAmountAndAddToRequestedAmount(TransactionData transactionData);

    void revertMintingRequestedReserve(TransactionData transactionData);

    void updateMintedAmount(TransactionData transactionData);

    BigDecimal getTokenMintedAmount(Hash tokenHash);

    BigDecimal getTokenRequestedMintingAmount(Hash tokenHash);
}
