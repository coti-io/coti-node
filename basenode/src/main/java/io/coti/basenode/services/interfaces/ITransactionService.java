package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.FluxSink;

import javax.servlet.http.HttpServletResponse;
import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface ITransactionService {

    void init();

    void getTransactionBatch(long startingIndex, long endingIndex, HttpServletResponse response, boolean isExtended, boolean isIncludeRuntimeTrustScore);

    void getTransactionBatch(long startingIndex, HttpServletResponse response);

    void getTransactionBatch(long startingIndex, FluxSink<byte[]> sink);

    ResponseEntity<IResponse> getNoneIndexedTransactions();

    void getNoneIndexedTransactionBatch(HttpServletResponse response, boolean isExtended);

    ResponseEntity<IResponse> getPostponedTransactions();

    void handlePropagatedTransaction(TransactionData transactionData);

    void handlePropagatedRejectedTransaction(RejectedTransactionData rejectedTransactionData);

    void handleMissingTransaction(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, EnumMap<InitializationTransactionHandlerType, ExecutorData> missingTransactionExecutorMap);

    Thread monitorTransactionThread(String type, AtomicLong transactionNumber, AtomicLong receivedTransactionNumber, String monitorThreadName);

    int totalPostponedTransactions();

    void addDataToMemory(TransactionData transactionData);

    long getRejectedTransactionsSize();
}
