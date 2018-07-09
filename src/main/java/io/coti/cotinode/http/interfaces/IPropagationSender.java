package io.coti.cotinode.http.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.*;
import org.springframework.http.ResponseEntity;

public interface IPropagationSender {
    void propagateTransactionToNeighbor(TransactionData transactionData, String nodeIp);

    ResponseEntity<Response> propagateTransactionFromNeighbor(Hash transactionHash, String nodeIp);

    GetTransactionsResponse propagateMultiTransactionFromNeighbor(GetTransactionsRequest getTransactionsRequest, String nodeIp);

    GetTransactionsResponse propagateMultiTransactionFromNeighbor(int index, String nodeIp);

    //void propagateMultiTransactionToNeighbor(AddTransactionsRequest request, String nodeIp);
}
