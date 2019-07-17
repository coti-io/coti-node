package io.coti.historynode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.ReceiverBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.historynode.crypto.TransactionsRequestCrypto;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.data.AddressTransactionsByDate;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsByDateRequest;
import io.coti.historynode.http.HistoryTransactionResponse;
import io.coti.historynode.http.data.HistoryTransactionResponseData;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.EMPTY_SEARCH_RESULT;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.TRANSACTIONS_NOT_FOUND;


@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    @Value("${storage.server.address}")
    protected String storageServerAddress;

    @Autowired
    private Transactions transactions;
    @Autowired
    private StorageConnector storageConnector;

    @Autowired
    private AddressTransactionsByAddresses addressTransactionsByAddresses;
    @Autowired
    private AddressTransactionsByDates addressTransactionsByDates;
    @Autowired
    private TransactionsRequestCrypto transactionsRequestCrypto;
    @Autowired
    private IValidationService validationService;

    protected String endpoint = null;
    private ObjectMapper mapper;

    private Queue<TransactionData> transactionsToValidate;
    private AtomicBoolean isValidatorRunning;

    @PostConstruct
    public void init() {
        transactionsToValidate = new PriorityQueue<>();
        isValidatorRunning = new AtomicBoolean(false);
        super.init();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        endpoint = "/transactions";
    }

    @Scheduled(fixedRate = 1000)
    private void checkAttachedTransactions() {
        if (!isValidatorRunning.compareAndSet(false, true)) {
            return;
        }
        while (!transactionsToValidate.isEmpty()) {
            TransactionData transactionData = transactionsToValidate.remove();
            log.debug("History node Fully Checking transaction: {}", transactionData.getHash());
            if( validationService.fullValidation(transactionData) ) {
                // Update Indexing structures with details from the new transaction
                addToHistoryTransactionIndexes(transactionData);
            }
        }
        isValidatorRunning.set(false);
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        super.continueHandlePropagatedTransaction(transactionData);
        log.debug("Continue to handle propagated transaction {} by history node", transactionData.getHash());
        transactionsToValidate.add(transactionData);
    }

    public List<Hash> getLocalTransactionsUnconfirmed() {
        ArrayList<Hash> localTransactionsUnconfirmed = new ArrayList<>();
        transactions.forEach(transactionData -> {
            if (!transactionData.isTrustChainConsensus() || !transactionData.getDspConsensusResult().isDspConsensus()) {
                localTransactionsUnconfirmed.add(transactionData.getHash());
            }
        });
        return localTransactionsUnconfirmed;
    }

    public TransactionData getTransactionFromRecovery(Hash unconfirmedTransactionHash) {
        //TODO 7/7/2019 tomer: implement logic to retrieve updated transaction data from recovery server ?
        return transactions.getByHash(unconfirmedTransactionHash);
    }

    protected void updateUnconfirmedTransactionsNotFromClusterStamp(List<Hash> unconfirmedTransactionHashesFromClusterStamp) {
        List<Hash> unconfirmedTransactionsHashesFromDB = getLocalTransactionsUnconfirmed();
        // If any transaction is not in the unconfirmed transactions of the cluster-stamp, verify status from {?} recovery server and update accordingly
        List<Hash> intersectionUnconfirmedTransactions = new ArrayList<>(unconfirmedTransactionsHashesFromDB);
        intersectionUnconfirmedTransactions.retainAll(unconfirmedTransactionHashesFromClusterStamp);
        List<Hash> unconfirmedTransactionsHashesNotInClusterStamp = new ArrayList<>(unconfirmedTransactionsHashesFromDB);
        unconfirmedTransactionsHashesNotInClusterStamp.removeAll(intersectionUnconfirmedTransactions);
        unconfirmedTransactionsHashesNotInClusterStamp.forEach(unconfirmedTransactionHash -> {
            transactions.put(getTransactionFromRecovery(unconfirmedTransactionHash));
        });
    }


    public void deleteLocalUnconfirmedTransactions() {
        transactions.forEach(transactionData -> {
            if (!transactionData.isTrustChainConsensus() || !transactionData.getDspConsensusResult().isDspConsensus()) {
                transactions.deleteByHash(transactionData.getHash());
            }
        });
    }



    public ResponseEntity<IResponse> getTransactionsByAddress(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        //TODO 7/2/2019 tomer: Commented for initial integration testing, uncomment after adding signature in tests
        // Verify signature
//        if(!transactionsRequestCrypto.verifySignature(getTransactionsByAddressRequest)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
//        }

        List<Hash> transactionsHashes = getTransactionsHashesToRetrieve(getTransactionsByAddressRequest);
        return getTransactions(transactionsHashes);
    }

    public ResponseEntity<IResponse> getTransactionsByDate(GetTransactionsByDateRequest getTransactionsByDateRequest) {

        List<Hash> transactionsHashes = getTransactionsHashesByDate(getTransactionsByDateRequest.getDate()) ;
        return getTransactions(transactionsHashes);
    }

    private ResponseEntity<IResponse> getTransactions(List<Hash> transactionsHashes) {
        if (transactionsHashes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(TRANSACTIONS_NOT_FOUND, EMPTY_SEARCH_RESULT));
        }
        HashMap<Hash, TransactionData> collectedTransactions = retrieveTransactions(transactionsHashes);

        return ResponseEntity.status(HttpStatus.OK).body(new HistoryTransactionResponse(new HistoryTransactionResponseData(collectedTransactions)));
    }

    private HashMap<Hash, TransactionData> retrieveTransactions(List<Hash> transactionsHashes) {
        // Retrieve transactions from local DB, Storage and merge results
        List<Hash> transactionsHashesToRetrieveFromStorage = new ArrayList<>();
        HashMap<Hash, TransactionData> retrievedTransactionsFromDB = getTransactionsFromLocal(transactionsHashes, transactionsHashesToRetrieveFromStorage);
        HashMap<Hash, TransactionData> retrievedTransactionsFromElasticSearch = getTransactionFromElasticSearch(transactionsHashesToRetrieveFromStorage);
        HashMap<Hash, TransactionData> collectedTransactions = new HashMap<>(retrievedTransactionsFromDB);
        collectedTransactions.putAll(retrievedTransactionsFromElasticSearch);
        return collectedTransactions;
    }

    private HashMap<Hash, TransactionData> getTransactionFromElasticSearch(List<Hash> transactionsHashesToRetrieveFromStorage) {
        HashMap<Hash, TransactionData> retrievedTransactionsFromStorage = new HashMap<>();
        Map<Hash, String> entitiesBulkResponses = null;
        if(!transactionsHashesToRetrieveFromStorage.isEmpty()) {
            entitiesBulkResponses = getTransactionsDataFromElasticSearch(transactionsHashesToRetrieveFromStorage).getBody().getHashToEntitiesFromDbMap();
            if(entitiesBulkResponses == null || entitiesBulkResponses.isEmpty()) {
                log.error("No transactions were retrieved from storage");
                for(Hash transactionHash : transactionsHashesToRetrieveFromStorage) {
                    retrievedTransactionsFromStorage.putIfAbsent(transactionHash, null);
                }
            } else {
                retrievedTransactionsFromStorage =
                        getRetrievedTransactionsFromStorageResponseEntity(transactionsHashesToRetrieveFromStorage, entitiesBulkResponses);
            }
        }
        return retrievedTransactionsFromStorage;
    }

    private HashMap<Hash, TransactionData> getTransactionsFromLocal(List<Hash> transactionsHashes, List<Hash> transactionsHashesToRetrieveFromElasticSearch) {
        HashMap<Hash, TransactionData> retrievedTransactionsFromLocal = new HashMap<>();
        for (Hash transactionsHash : transactionsHashes) {
            TransactionData localTransactionDataByHash = transactions.getByHash(transactionsHash);
            if( localTransactionDataByHash != null) {
                retrievedTransactionsFromLocal.put(transactionsHash, localTransactionDataByHash);
            } else {
                transactionsHashesToRetrieveFromElasticSearch.add(transactionsHash);
            }
        }
        return retrievedTransactionsFromLocal;
    }

    private HashMap<Hash, TransactionData> getRetrievedTransactionsFromStorageResponseEntity(List<Hash> transactionsHashes, Map<Hash, String> entitiesBulkResponses) {
        HashMap<Hash, TransactionData> retrievedTransactions = new HashMap<>();
        transactionsHashes.forEach(transactionHash -> {
            String transactionRetrievedAsJson = entitiesBulkResponses.get(transactionHash);
            if(transactionRetrievedAsJson != null ) {
                try {
                    TransactionData transactionData = mapper.readValue(transactionRetrievedAsJson, TransactionData.class);
                    retrievedTransactions.put(transactionHash, transactionData);
                } catch (IOException e) {
                    log.error("Failed to read value for {}", transactionHash);
                    e.printStackTrace();
                    retrievedTransactions.putIfAbsent(transactionHash, null);
                }
            } else {
                retrievedTransactions.putIfAbsent(transactionHash, null);
                log.error("Failed to retrieve value for {}", transactionHash);
            }
        });
        return retrievedTransactions;
    }

    private ResponseEntity<EntitiesBulkJsonResponse> getTransactionsDataFromElasticSearch(List<Hash> transactionsHashes) {
        // Retrieve transactions from storage
        GetHistoryTransactionsRequest getHistoryTransactionsRequest = new GetHistoryTransactionsRequest(transactionsHashes);
        RestTemplate restTemplate = new RestTemplate();
        endpoint = "/transactions";
        ResponseEntity responseEntity = storageConnector.retrieveFromStorage(storageServerAddress + endpoint, getHistoryTransactionsRequest, EntitiesBulkJsonResponse.class);
        return responseEntity;
    }




    private List<Hash> getTransactionsHashesToRetrieve(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        if( getTransactionsByAddressRequest.getAddress()==null ) {
            return new ArrayList<>();
        }
        if( getTransactionsByAddressRequest.getStartDate()==null || getTransactionsByAddressRequest.getEndDate()==null ) {
            return getTransactionsHashesByAddress(getTransactionsByAddressRequest.getAddress());
        }
        return getTransactionsHashesByAddressAndDates(getTransactionsByAddressRequest.getAddress(), getTransactionsByAddressRequest.getStartDate(), getTransactionsByAddressRequest.getEndDate());
    }


    private List<Hash> getTransactionsHashesByDate(Instant date) {
        if(date==null) {
            return new ArrayList<>();
        }
        AddressTransactionsByDate addressTransactionsByDate = addressTransactionsByDates.getByHash(getHashByLocalDate(getLocalDateByInstant(date)));
        if(addressTransactionsByDate==null || addressTransactionsByDate.getTransactionsAddresses()==null || addressTransactionsByDate.getTransactionsAddresses().isEmpty()) {
            return new ArrayList<>();
        }
        return addressTransactionsByDate.getTransactionsAddresses().stream().collect(Collectors.toList());
    }

    private List<Hash> getTransactionsHashesByDates(Instant startDate, Instant endDate) {
        if(startDate==null || endDate==null || startDate.isAfter(endDate)) {
            return new ArrayList<>();
        }
        LocalDate localStartDate = getLocalDateByInstant(startDate);
        LocalDate localEndDate = getLocalDateByInstant(endDate);
        List<LocalDate> localDatesBetween = getLocalDatesBetween(localStartDate, localEndDate);

        List<Hash> collectedTransactionsHashes = localDatesBetween.stream().map(localDate -> addressTransactionsByDates.getByHash(getHashByLocalDate(localDate)))
                .flatMap(transactionsByDate -> transactionsByDate.getTransactionsAddresses().stream())
                .collect(Collectors.toList());
        return collectedTransactionsHashes;
    }

    private Hash getHashByLocalDate(LocalDate localDate) {
        return CryptoHelper.cryptoHash(localDate.atStartOfDay().toString().getBytes());
    }

    private LocalDate getLocalDateByInstant(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        return LocalDate.of(ldt.getYear(), ldt.getMonth(),ldt.getDayOfMonth());
    }

    public static List<LocalDate> getLocalDatesBetween(LocalDate startDate, LocalDate endDate) {
        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));
        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
    }

    private List<Hash> getTransactionsHashesByAddress(Hash address) {
        AddressTransactionsByAddress addressTransactionsByAddress = addressTransactionsByAddresses.getByHash(address);
        if(addressTransactionsByAddress==null) {
            return new ArrayList<>();
        }
        HashMap<LocalDate, HashSet<Hash>> transactionHashesByDates = addressTransactionsByAddress.getTransactionHashesByDates();
        return transactionHashesByDates.keySet().stream().flatMap(key -> transactionHashesByDates.get(key).stream()).collect(Collectors.toList());
    }

    private List<Hash> getTransactionsHashesByAddressAndDates(Hash address, Instant startDate, Instant endDate) {
        AddressTransactionsByAddress addressTransactionsByAddress = addressTransactionsByAddresses.getByHash(address);
        if(addressTransactionsByAddress==null) {
            return new ArrayList<>();
        }
        HashMap<LocalDate, HashSet<Hash>> transactionHashesByDates = addressTransactionsByAddress.getTransactionHashesByDates();
        List<LocalDate> localDatesBetween = getLocalDatesBetween(getLocalDateByInstant(startDate),getLocalDateByInstant(endDate));

        return localDatesBetween.stream().filter(localDate->  transactionHashesByDates.containsKey(localDate)).
                flatMap(localDate -> transactionHashesByDates.get(localDate).stream()).collect(Collectors.toList());
    }


    public void addToHistoryTransactionIndexes(TransactionData transactionData) {
        Instant attachmentTime = transactionData.getAttachmentTime();
        Hash hashByDate = calculateHashByAttachmentTime(attachmentTime);
        LocalDate attachmentLocalDate = calculateInstantLocalDate(attachmentTime);
        HashSet<Hash> relatedAddressHashes = getRelatedAddresses(transactionData);

        AddressTransactionsByDate transactionsByDateHash = addressTransactionsByDates.getByHash(hashByDate);
        if(transactionsByDateHash== null) {
            HashSet<Hash> addresses = new HashSet<>();
            addresses.add(transactionData.getHash());
            addressTransactionsByDates.put(new AddressTransactionsByDate(attachmentTime, addresses));
        } else {
            transactionsByDateHash.getTransactionsAddresses().add(transactionData.getHash());
            addressTransactionsByDates.put(transactionsByDateHash);
        }


        for(Hash transactionAddressHash : relatedAddressHashes) {
            HashMap<LocalDate, HashSet<Hash>> transactionHashesMap = new HashMap<>();
            AddressTransactionsByAddress transactionsByAddressHash = addressTransactionsByAddresses.getByHash(transactionAddressHash);
            if(transactionsByAddressHash==null) {
                HashSet<Hash> transactionHashes = new HashSet<>();
                if( !transactionHashes.add(transactionData.getHash()) ) {
                    log.info("{} was already present by address", transactionData);
                }
                transactionHashesMap.put(attachmentLocalDate, transactionHashes);
                addressTransactionsByAddresses.put(new AddressTransactionsByAddress(transactionAddressHash, transactionHashesMap));
            } else {
                HashSet<Hash> transactionsHashesByDate = transactionsByAddressHash.getTransactionHashesByDates().get(hashByDate);
                if(transactionsHashesByDate == null) {
                    HashSet<Hash> transactionsHashes = new HashSet<>();
                    transactionsHashes.add(transactionData.getHash());
                    transactionsByAddressHash.getTransactionHashesByDates().put(attachmentLocalDate, transactionsHashes);
                } else {
                    transactionsHashesByDate.add(transactionData.getHash());
                }
                addressTransactionsByAddresses.put(transactionsByAddressHash);
            }
        }
    }



    private HashSet<Hash> getRelatedAddresses(TransactionData transactionData) {
        HashSet<Hash> hashes = new HashSet<>();
        hashes.add(transactionData.getSenderHash());
        for(BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if(baseTransactionData instanceof ReceiverBaseTransactionData) {
                hashes.add(baseTransactionData.getAddressHash());
            }
        }
        return hashes;
    }

    protected Hash calculateHashByAttachmentTime(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        LocalDate localDate = LocalDate.of(ldt.getYear(), ldt.getMonth(),ldt.getDayOfMonth());
        return CryptoHelper.cryptoHash(localDate.atStartOfDay().toString().getBytes());
    }

    public LocalDate calculateInstantLocalDate(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        return LocalDate.of(ldt.getYear(), ldt.getMonth(),ldt.getDayOfMonth());
    }

    protected ResponseEntity<EntitiesBulkJsonResponse> storeEntitiesByType(String url, AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return storageConnector.storeInStorage(storageServerAddress + endpoint, addEntitiesBulkRequest, EntitiesBulkJsonResponse.class);
    }

    public ResponseEntity<EntitiesBulkJsonResponse> storeEntities(List<? extends IEntity> entities) {

        AddEntitiesBulkRequest addEntitiesBulkRequest = new AddEntitiesBulkRequest();
        entities.forEach(entity ->
                {
                    try {
                        addEntitiesBulkRequest.getHashToEntityJsonDataMap().put(entity.getHash(), mapper.writeValueAsString(entity));
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                    }
                }
        );

        ResponseEntity<EntitiesBulkJsonResponse> storeEntitiesToStorageResponse = storeEntitiesByType(storageServerAddress + endpoint, addEntitiesBulkRequest);
        return storeEntitiesToStorageResponse;
    }

}
