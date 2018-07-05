package io.coti.cotinode.http.interfaces;

import io.coti.cotinode.http.*;
import org.springframework.http.ResponseEntity;

public interface IPropagationCommunication {
    void propagateTransactionToNeighbor(AddTransactionRequest request, String nodeIp);

    ResponseEntity<Response> propagateTransactionFromNeighbor(GetTransactionRequest getTransactionRequest, String nodeIp);

    GetTransactionsResponse propagateMultiTransactionFromNeighbor(GetTransactionsRequest getTransactionsRequest, String nodeIp);

    //void propagateMultiTransactionToNeighbor(AddTransactionsRequest request, String nodeIp);
}
