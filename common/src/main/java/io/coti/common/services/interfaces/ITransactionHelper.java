package io.coti.common.services.interfaces;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.TransactionData;

import java.util.List;

public interface ITransactionHelper {

    boolean isLegalBalance(List<BaseTransactionData> baseTransactions);

    boolean validateTransaction(TransactionData transactionData);

    boolean startHandleTransaction(TransactionData transactionData);

    void endHandleTransaction(TransactionData transactionData);

    boolean checkBalancesAndAddToPreBalance(TransactionData transactionData);

    void attachTransactionToCluster(TransactionData transactionData);

    void setTransactionStateToSaved(TransactionData transactionData);

    void setTransactionStateToFinished(TransactionData transactionData);

}
