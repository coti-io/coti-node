package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.FluxSink;

import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface ITransactionService {

    void init();

    void getTransactionBatch(long startingIndex, HttpServletResponse response);

    ResponseEntity<IResponse> getSingleTransaction(GetTransactionRequest getTransactionRequest);

    void getTransactionBatch(long startingIndex, FluxSink sink);

    void handlePropagatedTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes);

    Thread monitorTransactionThread(String type, AtomicLong transactionNumber, AtomicLong receivedTransactionNumber);

    int totalPostponedTransactions();

    void addToExplorerIndexes(TransactionData transactionData);

    boolean isTransactionReceived(Hash transactionHash);

    void addReceivedTransactionHash(Hash transactionHash);
}
