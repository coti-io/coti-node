package io.coti.historynode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.basenode.http.GetEntitiesBulkResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.historynode.crypto.GetTransactionsByAddressRequestCrypto;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsRequest;
import io.coti.historynode.http.HistoryTransactionResponse;
import io.coti.historynode.http.data.HistoryTransactionResponseData;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDatesHistories;
import io.coti.historynode.services.interfaces.IHistoryTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class HistoryTransactionService extends EntityService implements IHistoryTransactionService {

    @Value("${storage.server.address}")
    protected String storageServerAddress;

    @Autowired
    AddressTransactionsByDatesHistories addressTransactionsByDatesHistories;
    @Autowired
    private Transactions transactions;
    @Autowired
    private IStorageConnector storageConnector;

    @Autowired
    private AddressTransactionsByAddresses addressTransactionsByAddresses;
    @Autowired
    private GetTransactionsByAddressRequestCrypto getTransactionsByAddressRequestCrypto;

    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
//        mapper = new ObjectMapper();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        endpoint = "/transactions";
    }

    @Override
    public ResponseEntity<IResponse> getTransactionsDetails(GetTransactionsRequest getTransactionRequest) {
        List<TransactionData> transactionsList = new ArrayList<>();
        List<Hash> hashList = new ArrayList<>();
        GetEntitiesBulkRequest getEntitiesBulkRequest = new GetEntitiesBulkRequest(hashList);

        List<Hash> transactionHashes = getTransactionsHashesByAddresses(getTransactionRequest);
        for (Hash transactionHash : transactionHashes) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData == null) {
                getEntitiesBulkRequest.getHashes().add(transactionHash);
            } else {
                transactionsList.add(transactionData);
            }
        }

        transactionsList.addAll(getTransactionsDetailsFromStorage(getEntitiesBulkRequest));
        ResponseEntity<IResponse> response = ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(null);
        try {
            response = ResponseEntity
                    .status(HttpStatus.OK)
//                    .body((new GetTransactionsResponse(transactionsList)); //TODO: For initial compilation prior to merge
            .body(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


    @Override
    public void deleteLocalUnconfirmedTransactions() {
        transactions.forEach(transactionData -> {
            if (!transactionData.isTrustChainConsensus() || !transactionData.getDspConsensusResult().isDspConsensus()) {
                transactions.deleteByHash(transactionData.getHash());
            }
        });
    }

    private List<TransactionData> getTransactionsDetailsFromStorage(GetEntitiesBulkRequest getEntitiesBulkRequest) {
        List<TransactionData> transactionsList = new ArrayList<>();
        if (!getEntitiesBulkRequest.getHashes().isEmpty()) {
            ResponseEntity<IResponse> response
                    = storageConnector.getForObject(storageServerAddress + endpoint, ResponseEntity.class, getEntitiesBulkRequest);
            if (response.getStatusCode() == HttpStatus.OK) {
                //TODO: Convert http message body to transactions, and add to transactions
            }
        }
        return transactionsList;
    }

    private List<Hash> getTransactionsHashesByAddresses(GetTransactionsRequest getTransactionRequest) {
        List<Hash> transactionHashes = new ArrayList<>();
        long startDate = getTransactionRequest.getStartingDate() != null ? getTransactionRequest.getStartingDate().getTime() : Long.MIN_VALUE;
        long endDate = getTransactionRequest.getEndingDate() != null ? getTransactionRequest.getEndingDate().getTime() : Long.MAX_VALUE;
        getTransactionRequest.getAddressesHashes().forEach(addressHash ->
                transactionHashes.addAll(
                        addressTransactionsByDatesHistories.getByHash(addressHash)
                                .getTransactionsHistory().subMap(startDate, endDate).values()));
        return transactionHashes;
    }



    public ResponseEntity<IResponse> getTransactionsByAddress(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        // Verify signature //TODO: Commented for initial integration testing, uncomment after adding signature in tests
//        if(!getTransactionsByAddressRequestCrypto.verifySignature(getTransactionsByAddressRequest)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
//        }

        // Retrieve matching transactions hashes from relevant index
        AddressTransactionsByAddress addressTransactionsByAddress = addressTransactionsByAddresses.getByHash(getTransactionsByAddressRequest.getAddress());
        HashMap<Hash, HashSet<Hash>> transactionHashesByDates = addressTransactionsByAddress.getTransactionHashesByDates();
        List<Hash> transactionsHashes = transactionHashesByDates.keySet().stream().flatMap(key -> transactionHashesByDates.get(key).stream()).collect(Collectors.toList());
        if( transactionsHashes.isEmpty() ) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(TRANSACTIONS_NOT_FOUND, EMPTY_SEARCH_RESULT));
        }

        // Retrieve transactions from storage
        GetEntitiesBulkRequest getEntitiesBulkRequest = new GetEntitiesBulkRequest(transactionsHashes);

        RestTemplate restTemplate = new RestTemplate();
        endpoint = "/transactions";

        ResponseEntity<GetEntitiesBulkResponse> stringResponseEntity =
                restTemplate.postForEntity(storageServerAddress + endpoint,  getEntitiesBulkRequest,   GetEntitiesBulkResponse.class);
        Map<Hash, String> entitiesBulkResponses = stringResponseEntity.getBody().getEntitiesBulkResponses();
        if(entitiesBulkResponses == null || entitiesBulkResponses.isEmpty()) {
            log.error("No transactions were retrieved");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(SERVER_ERROR, EMPTY_SEARCH_RESULT));
        }

        ArrayList<TransactionData> retrievedTransactions = new ArrayList<>();
        transactionsHashes.forEach(transactionHash -> {
            String transactionRetrievedAsJson = entitiesBulkResponses.get(transactionHash);
            if(transactionRetrievedAsJson != null ) {
                try {
                    TransactionData transactionData = mapper.readValue(transactionRetrievedAsJson, TransactionData.class);
                    retrievedTransactions.add(transactionData);
                } catch (IOException e) {
                    log.error("Failed to read value for {}", transactionHash);
                    e.printStackTrace();
                }
            } else {
                log.error("Failed to retrieve value for {}", transactionHash);
            }
        });
        return ResponseEntity.status(HttpStatus.OK).body( new HistoryTransactionResponse(new HistoryTransactionResponseData(retrievedTransactions)));
    }
}
