package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.exception.TransactionException;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.http.GetTransactionResponse;
import org.springframework.http.ResponseEntity;

public interface ITransactionService {

    ResponseEntity<AddTransactionResponse> addNewTransaction(AddTransactionRequest request) throws TransactionException;

    TransactionData getTransactionData(Hash transactionHash);

    ResponseEntity<GetTransactionResponse> getTransactionDetails(Hash transactionHash);

    ResponseEntity<AddTransactionResponse> addTransactionFromPropagation(AddTransactionRequest request);
}
