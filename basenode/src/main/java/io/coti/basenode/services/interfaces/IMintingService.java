package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.ClusterStampCurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.util.Map;

public interface IMintingService {

    void init();

    boolean checkMintingAmountAndAddToAllocatedAmount(TransactionData transactionData);

    void revertMintingAllocation(TransactionData transactionData);

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    void validateMintingBalances();

    void updateMintingBalanceFromClusterStamp(Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap, Hash currencyGenesisAddress);
}
