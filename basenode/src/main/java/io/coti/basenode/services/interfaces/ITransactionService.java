package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import io.coti.basenode.http.*;
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

    ResponseEntity<IResponse> setIndexToTransactions(SetIndexesRequest setIndexesRequest);

    void shutdown();

    ResponseEntity<IResponse> getRejectedTransactions();

    void handleNewTransactionFromFullNode(TransactionData data);

    ResponseEntity<Response> addNewTransaction(AddTransactionRequest addTransactionRequest);

    ResponseEntity<IResponse> repropagateTransactionByWallet(RepropagateTransactionRequest repropagateTransactionRequest);

    ResponseEntity<IResponse> getTransactionDetails(Hash transactionHash, boolean b);

    void getTransactions(GetTransactionsRequest getTransactionsRequest, HttpServletResponse response);

    ResponseEntity<IResponse> getAddressTransactions(Hash address);

    void getAddressTransactionBatch(GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response, boolean b);

    void getAddressTransactionBatchByTimestamp(GetAddressTransactionBatchByTimestampRequest getAddressTransactionBatchByTimestampRequest, HttpServletResponse response, boolean b);

    void getAddressTransactionBatchByDate(GetAddressTransactionBatchByDateRequest getAddressTransactionBatchByDateRequest, HttpServletResponse response, boolean b);

    void getAddressRejectedTransactionBatch(GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response);

    ResponseEntity<IResponse> getLastTransactions();

    ResponseEntity<IResponse> getTotalTransactions();

    ResponseEntity<IResponse> getTransactionsByPage(int page);

    ResponseEntity<IResponse> repropagateTransactionByAdmin(RepropagateTransactionByAdminRequest repropagateTransactionByAdminRequest);

    ResponseEntity<IResponse> setReceiverBaseTransactionOwner(TransactionRequest transactionRequest);

    void getTransactionsByAddress(GetTransactionsByAddressRequest getTransactionsByAddressRequest, HttpServletResponse response);

    void getTransactionsByDate(GetTransactionsByDateRequest getTransactionsByDateRequest, HttpServletResponse response);
}
