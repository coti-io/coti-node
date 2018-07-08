package io.coti.fullnode.service.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetTransactionRequest;
import io.coti.common.http.GetTransactionsRequest;
import io.coti.common.http.Response;
import org.springframework.http.ResponseEntity;

public interface IPropagationService {
    void propagateToNeighbors(TransactionData transactionData);

    void propagateFromNeighbors(Hash transactionHash);

    ResponseEntity<Response> getTransaction(GetTransactionRequest getTransactionRequest);

    ResponseEntity<Response> getTransactionsFromCurrentNode(GetTransactionsRequest getTransactionsRequest);
}
