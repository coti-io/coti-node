package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ITransactionService {
    ResponseEntity<IResponse> insertTransactionJson(Hash hash, String transactionAsJson);

    ResponseEntity<IResponse> getTransactionByHash(Hash hash);

    ResponseEntity<IResponse> getMultiTransactionsFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> insertMultiTransactions(Map<Hash, String> hashToObjectJsonDataMap);

    ResponseEntity<IResponse> deleteMultiTransactionsFromDb(List<Hash> hashes);

    ResponseEntity<IResponse> deleteTransactionByHash(Hash hash);
}
