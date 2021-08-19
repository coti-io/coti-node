package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.OutputBaseTransactionData;
import io.coti.basenode.data.TransactionData;

import java.util.List;

public interface ITransactionTypeValidation {

    boolean validateBaseTransactions(TransactionData transactionData, Hash nativeCurrencyHash);

    boolean validateInputBaseTransactions(TransactionData transactionData);

    boolean validateOutputBaseTransactions(TransactionData transactionData, boolean skipValidationOfReducedAmount, Hash nativeCurrencyHash);

    boolean validateReducedAmount(List<OutputBaseTransactionData> outputBaseTransactions);
}
