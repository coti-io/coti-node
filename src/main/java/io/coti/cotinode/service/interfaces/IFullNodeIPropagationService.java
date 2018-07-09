package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.GetTransactionsResponse;
import io.coti.cotinode.http.Response;
import org.springframework.http.ResponseEntity;

public interface IFullNodeIPropagationService {
    void propagateToDspNode(TransactionData transactionData);

    ResponseEntity<Response> propagateFromDspNode(Hash transactionHash, String dspNode);

    GetTransactionsResponse propagateAllFromDspNode(int index);
}
