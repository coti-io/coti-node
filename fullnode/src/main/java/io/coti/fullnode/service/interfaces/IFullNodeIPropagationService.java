package io.coti.fullnode.service.interfaces;


import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetTransactionsResponse;
import io.coti.common.http.Response;
import org.springframework.http.ResponseEntity;

public interface IFullNodeIPropagationService {
    void propagateToDspNode(TransactionData transactionData);

    ResponseEntity<Response> propagateFromDspNode(Hash transactionHash, String dspNode);

    GetTransactionsResponse propagateAllFromDspNode(int index);
}
