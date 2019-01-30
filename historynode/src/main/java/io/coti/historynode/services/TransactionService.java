package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.data.ObjectDocument;
import io.coti.historynode.http.AddTransactionJsonResponse;
import io.coti.historynode.http.GetMultiObjectJsonResponse;
import io.coti.historynode.http.GetObjectJsonResponse;
import io.coti.historynode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
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
        if (!validateTransaction(hash, transactionAsJson)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            INVALID_PARAMETERS_MESSAGE,
                            STATUS_ERROR));
        }
        String insertResponse =
                clientService.insertObjectToDb(hash, transactionAsJson, TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionJsonResponse(
                        STATUS_SUCCESS,
                        TRANSACTION_CREATED_MESSAGE, insertResponse));
    }


    @Override
    public ResponseEntity<IResponse> getMultiTransactionsFromDb(Map<Hash, String> hashAndIndexNameMap) throws IOException {
        Map<Hash, String> hashToObjectsFromDbMap = null;
        MultiGetResponse multiGetResponse = clientService.getMultiObjectsFromDb(hashAndIndexNameMap);
        hashToObjectsFromDbMap = new HashMap<>();
        for (MultiGetItemResponse multiGetItemResponse : multiGetResponse.getResponses()) {
            hashToObjectsFromDbMap.put(new Hash(multiGetItemResponse.getId()),
                    new String(multiGetItemResponse.getResponse().getSourceAsBytes()));
        }
        //TODO: Define logic
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetMultiObjectJsonResponse(hashToObjectsFromDbMap));
    }

    @Override
    public ResponseEntity<IResponse> insertMultiObjectsToDb(List<ObjectDocument> transactionDocumentList) throws IOException {
        clientService.insertMultiObjectsToDb(transactionDocumentList);
        //TODO: Define logic
        return null;
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

    public boolean validateTransaction(Hash hash, String transactionAsJsonString) throws IOException {
        // TODO:
        return true;
    }
}
