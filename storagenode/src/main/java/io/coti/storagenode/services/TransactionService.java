package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.http.AddObjectJsonResponse;
import io.coti.storagenode.http.GetObjectBulkJsonResponse;
import io.coti.storagenode.http.GetObjectJsonResponse;
import io.coti.storagenode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private ClientService clientService;

    private String TRANSACTION_INDEX_NAME = "transactions";
    private String TRANSACTION_OBJECT_NAME = "transactionData";

    @PostConstruct
    private void init() {
        try {
            clientService.addIndex(TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<IResponse> insertTransactionJson(Hash hash, String transactionAsJson) throws IOException {
        String insertResponse =
                clientService.insertObjectToDb(hash, transactionAsJson, TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddObjectJsonResponse(
                        STATUS_SUCCESS,
                        TRANSACTION_CREATED_MESSAGE, insertResponse));
    }

    @Override
    public ResponseEntity<IResponse> getMultiTransactionsFromDb(List<Hash> hashes) {
        Map<Hash, String> hashToTransactionFromDbMap = null;

        //TODO: Define logic.
        hashToTransactionFromDbMap = clientService.getMultiObjects(hashes, TRANSACTION_INDEX_NAME);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetObjectBulkJsonResponse(hashToTransactionFromDbMap));
    }

    @Override
    public ResponseEntity<IResponse> insertMultiTransactions(Map<Hash, String> hashToTransactionJsonDataMap) {
        try {
            clientService.insertMultiObjectsToDb(TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME, hashToTransactionJsonDataMap);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        //TODO: Define logic
        return null;

        //clientService.insertMultiObjectsToDb(addressDocumentList);
    }

    @Override
    public ResponseEntity<IResponse> getTransactionByHash(Hash hash) throws IOException {
        String transactionAsJson = clientService.getObjectFromDbByHash(hash, TRANSACTION_INDEX_NAME);
        if (transactionAsJson == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            TRANSACTION_DOESNT_EXIST_MESSAGE,
                            STATUS_ERROR));
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GetObjectJsonResponse(hash, transactionAsJson));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            TRANSACTION_DETAILS_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }
}
