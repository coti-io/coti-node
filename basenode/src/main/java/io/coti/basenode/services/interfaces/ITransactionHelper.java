package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.http.GetTransactionBatchResponse;

import java.util.List;

public interface ITransactionHelper {

    boolean validateBaseTransactionAmounts(List<BaseTransactionData> baseTransactions);

    boolean validateBaseTransactionsDataIntegrity(TransactionData transactionData);

    boolean validateTransactionCrypto(TransactionData transactionData);

    boolean validateTransactionType(TransactionData transactionData);

    boolean validateTrustScore(TransactionData transactionData);

    void startHandleTransaction(TransactionData transactionData);

    void endHandleTransaction(TransactionData transactionData);

    boolean checkBalancesAndAddToPreBalance(TransactionData transactionData);

    void attachTransactionToCluster(TransactionData transactionData);

    void setTransactionStateToSaved(TransactionData transactionData);

    void setTransactionStateToFinished(TransactionData transactionData);

    boolean handleVoteConclusionResult(DspConsensusResult dspConsensusResult);

    boolean isConfirmed(TransactionData transactionData);

    boolean isDspConfirmed(TransactionData transactionData);

    boolean isTransactionExists(TransactionData transactionData);

    boolean isTransactionHashExists(Hash transactionHash);

    boolean validateBaseTransactionTrustScoreNodeResults(TransactionData transactionData);

    boolean validateBaseTransactionTrustScoreNodeResult(ITrustScoreNodeValidatable baseTransactionData);

    boolean isTransactionHashProcessing(Hash transactionHash);

    boolean isTransactionAlreadyPropagated(TransactionData transactionData);

    long getTotalTransactions();

    long incrementTotalTransactions();

    GetTransactionBatchResponse getTransactionBatch(long startingIndex);

    void addNoneIndexedTransaction(TransactionData transactionData);

    void removeNoneIndexedTransaction(TransactionData transactionData);

    PaymentInputBaseTransactionData getPaymentInputBaseTransaction(TransactionData transactionData);
}
