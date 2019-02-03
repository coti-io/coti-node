package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.ObjectDocument;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ITransactionService {
    ResponseEntity<IResponse> insertTransactionJson(Hash hash, String transactionAsJson) throws IOException;

    ResponseEntity<IResponse> getTransactionByHash(Hash hash) throws IOException;

    ResponseEntity<IResponse> getMultiTransactionsFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> insertMultiTransactions(Map<Hash, String> hashToTransactionJsonDataMap);
}
