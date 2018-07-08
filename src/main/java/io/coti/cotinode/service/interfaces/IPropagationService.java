package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.GetTransactionRequest;
import io.coti.cotinode.http.GetTransactionsRequest;
import io.coti.cotinode.http.Response;
import org.springframework.http.ResponseEntity;

public interface IPropagationService {
    void propagateToNeighbors(TransactionData transactionData);

    void propagateFromNeighbors(Hash transactionHash);

    ResponseEntity<Response> getTransaction(GetTransactionRequest getTransactionRequest);

    ResponseEntity<Response> getTransactionsFromCurrentNode(GetTransactionsRequest getTransactionsRequest);
}
