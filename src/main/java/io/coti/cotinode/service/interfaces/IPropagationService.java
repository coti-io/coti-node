package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddTransactionRequest;

public interface IPropagationService {
    void propagateToNeighbors(AddTransactionRequest request);

    void getTransactionFromNeighbors(Hash transactionHash);
}
