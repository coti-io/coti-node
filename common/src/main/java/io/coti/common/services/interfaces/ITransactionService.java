package io.coti.common.services.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.AddTransactionRequest;
import io.coti.common.http.Response;
import org.springframework.http.ResponseEntity;

public interface ITransactionService {

    ResponseEntity<Response> addNewTransaction(AddTransactionRequest request) throws TransactionException;

    TransactionData getTransactionData(Hash transactionHash);

    ResponseEntity<Response> getTransactionDetails(Hash transactionHash);

    ResponseEntity<Response> getAddressTransactions(Hash addressHash);

}
