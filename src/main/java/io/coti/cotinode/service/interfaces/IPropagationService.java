package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.GetTransactionRequest;
import io.coti.cotinode.http.GetTransactionResponse;
import io.coti.cotinode.http.Response;
import org.springframework.http.ResponseEntity;

public interface IPropagationService {
    void propagateToNeighbors(AddTransactionRequest request);

    void propagateFromNeighbors(GetTransactionRequest getTransactionRequest) ;

    ResponseEntity<Response> getTransactionFromCurrentNode(GetTransactionRequest getTransactionRequest);
}
