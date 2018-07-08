package io.coti.fullnode.service;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetTransactionsRequest;
import io.coti.common.http.GetTransactionsResponse;
import io.coti.common.http.Response;
import org.springframework.http.ResponseEntity;

public interface IPropagationSender {
    void propagateTransactionToNeighbor(TransactionData transactionData, String nodeIp);

    ResponseEntity<Response> propagateTransactionFromNeighbor(Hash transactionHash, String nodeIp);

    GetTransactionsResponse propagateMultiTransactionFromNeighbor(GetTransactionsRequest getTransactionsRequest, String nodeIp);

    //void propagateMultiTransactionToNeighbor(AddTransactionsRequest request, String nodeIp);
}
