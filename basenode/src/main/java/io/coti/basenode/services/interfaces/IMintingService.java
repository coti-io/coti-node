package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.math.BigDecimal;
import java.util.Set;

public interface IMintingService {

    void init();

    boolean checkMintingAmountAndAddToAllocatedAmount(TransactionData transactionData);

    void revertMintingAllocation(TransactionData transactionData);

    BigDecimal getTokenAllocatedAmount(Hash tokenHash);

    void handleExistingTransaction(TransactionData transactionData);

    void validateMintingBalances();

    void updateMintingBalanceFromClusterStamp(Set<Hash> keySet, Hash currencyGenesisAddress);
}
