package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import reactor.core.publisher.FluxSink;

import javax.servlet.http.HttpServletResponse;
import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface ITransactionService {

    void init();

    void getTransactionBatch(long startingIndex, HttpServletResponse response);

    void getTransactionBatch(long startingIndex, FluxSink sink);

    void handlePropagatedTransaction(TransactionData transactionData);

    void handlePropagatedInvalidTransaction(InvalidTransactionData invalidTransactionData);

    void handleMissingTransaction(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, EnumMap<InitializationTransactionHandlerType, ExecutorData> missingTransactionExecutorMap);

    Thread monitorTransactionThread(String type, AtomicLong transactionNumber, AtomicLong receivedTransactionNumber, String monitorThreadName);

    int totalPostponedTransactions();

    void addDataToMemory(TransactionData transactionData);
}
