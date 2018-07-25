package io.coti.common.services.interfaces;

import io.coti.common.http.AddTransactionZeroSpendRequest;
import io.coti.common.http.AddTransactionZeroSpendResponse;
import org.springframework.http.ResponseEntity;

public interface IAddZeroSpendTransactionService {
    ResponseEntity<AddTransactionZeroSpendResponse> addTransaction(AddTransactionZeroSpendRequest addTransactionRequest);
}
