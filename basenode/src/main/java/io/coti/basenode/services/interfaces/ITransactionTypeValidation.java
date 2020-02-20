package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.OutputBaseTransactionData;
import io.coti.basenode.data.TransactionData;

import java.util.List;

public interface ITransactionTypeValidation {

    boolean validateBaseTransactions(TransactionData transactionData);

    boolean validateInputBaseTransactions(TransactionData transactionData);

    boolean validateOutputBaseTransactions(TransactionData transactionData, boolean skipValidationOfReducedAmount);

    boolean validateReducedAmount(List<OutputBaseTransactionData> outputBaseTransactions);
}
