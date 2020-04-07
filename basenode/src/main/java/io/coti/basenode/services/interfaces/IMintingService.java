package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.ClusterStampCurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.math.BigDecimal;
import java.util.Map;

public interface IMintingService {

    void init();

    boolean checkMintingAmountAndAddToAllocatedAmount(TransactionData transactionData);

    void revertMintingAllocation(TransactionData transactionData);

    BigDecimal getTokenAllocatedAmount(Hash tokenHash);

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    void validateMintingBalances();

    void updateMintingBalanceFromClusterStamp(Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap, Hash currencyGenesisAddress);
}
