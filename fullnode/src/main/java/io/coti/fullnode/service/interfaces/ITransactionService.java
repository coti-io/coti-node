package io.coti.fullnode.service.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.AddTransactionRequest;
import io.coti.common.http.Response;
import io.coti.fullnode.exception.TransactionException;
import org.springframework.http.ResponseEntity;

public interface ITransactionService {

    ResponseEntity<Response> addNewTransaction(AddTransactionRequest request) throws TransactionException;

    TransactionData getTransactionData(Hash transactionHash);

    ResponseEntity<Response> getTransactionDetails(Hash transactionHash);

    void addTransactionFromPropagation(TransactionData transactionData);

    TransactionData getLastTransactionHash();

    void propagateMultiTransactionFromDsp();

}
