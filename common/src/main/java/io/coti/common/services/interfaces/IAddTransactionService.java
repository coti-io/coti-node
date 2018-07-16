package io.coti.common.services.interfaces;

import io.coti.common.http.AddTransactionZeroSpendRequest;
import io.coti.common.http.AddTransactionZeroSpendResponse;
import org.springframework.http.ResponseEntity;

public interface IAddTransactionService {

    ResponseEntity<AddTransactionZeroSpendResponse> addTransaction(AddTransactionZeroSpendRequest addTransactionRequest);

}
