package io.coti.zero_spend.services.interfaces;

import io.coti.common.data.TransactionData;
import io.coti.zero_spend.http.AddTransactionRequest;
import io.coti.zero_spend.http.AddTransactionResponse;
import org.springframework.http.ResponseEntity;

public interface IAddTransactionService {

    ResponseEntity<AddTransactionResponse> addTransaction(AddTransactionRequest addTransactionRequest);

}
