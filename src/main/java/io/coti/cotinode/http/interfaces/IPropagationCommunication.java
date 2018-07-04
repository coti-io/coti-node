package io.coti.cotinode.http.interfaces;

import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.GetTransactionRequest;

public interface IPropagationCommunication {
    void propagateTransactionToNeighbor(AddTransactionRequest request, String nodeIp);

    void propagateTransactionFromNeighbor(GetTransactionRequest getTransactionRequest, String nodeIp);
}
