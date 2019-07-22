package io.coti.historynode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.http.*;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.historynode.crypto.TransactionsRequestCrypto;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.data.AddressTransactionsByDate;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsByDateRequest;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;


@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    private final String END_POINT = "/transactions";

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
    private ChunkService chunkService;
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;
    private ObjectMapper mapper;

    @Override
    public void init() {
        super.init();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        log.debug("Continue to handle propagated transaction {} by history node", transactionData.getHash());
        addToHistoryTransactionIndexes(transactionData);
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


    public void getTransactionsByAddress(GetTransactionsByAddressRequest getTransactionsByAddressRequest, HttpServletResponse response) {
        try {
            CustomHttpServletResponse customResponse = new CustomHttpServletResponse(response);
            if (!transactionsRequestCrypto.verifySignature(getTransactionsByAddressRequest)) {
                customResponse.printResponse(new Response(INVALID_SIGNATURE, STATUS_ERROR), HttpStatus.UNAUTHORIZED.value());
                return;
            }
            List<Hash> transactionsHashes = getTransactionsHashesToRetrieve(getTransactionsByAddressRequest);
            getTransactions(transactionsHashes, response);
        } catch (Exception e) {

        }
    }

    public void getTransactionsByDate(GetTransactionsByDateRequest getTransactionsByDateRequest, HttpServletResponse response) {

        List<Hash> transactionsHashes = getTransactionsHashesByDate(getTransactionsByDateRequest.getDate());
        getTransactions(transactionsHashes, response);
    }

    private void getTransactions(List<Hash> transactionsHashes, HttpServletResponse response) {
        try {
            CustomHttpServletResponse customResponse = new CustomHttpServletResponse(response);
            if (transactionsHashes.isEmpty()) {
                customResponse.printResponse("[]", HttpStatus.OK.value());
            }
            chunkService.startOfChunk(response);

            retrieveTransactions(transactionsHashes, response);

            chunkService.endOfChunk(response);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void retrieveTransactions(List<Hash> transactionHashes, HttpServletResponse response) {
        List<Hash> transactionsHashesToRetrieveFromElasticSearch = new ArrayList<>();
        getTransactionsFromLocal(transactionHashes, transactionsHashesToRetrieveFromElasticSearch, response);
        if (!transactionsHashesToRetrieveFromElasticSearch.isEmpty()) {
            getTransactionFromElasticSearch(transactionsHashesToRetrieveFromElasticSearch, response);
        }
    }

    private void getTransactionFromElasticSearch(List<Hash> transactionsHashesToRetrieve, HttpServletResponse response) {

        getTransactionsDataFromElasticSearch(transactionsHashesToRetrieve, response);
    }

    private void getTransactionsFromLocal(List<Hash> transactionHashes, List<Hash> transactionsHashesToRetrieveFromElasticSearch, HttpServletResponse response) {

        transactionHashes.forEach(transactionHash -> {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData != null) {
                chunkService.transactionHandler(transactionData, response);
            } else {
                transactionsHashesToRetrieveFromElasticSearch.add(transactionHash);
            }
        });
    }

    private void getTransactionsDataFromElasticSearch(List<Hash> transactionsHashes, HttpServletResponse response) {
        RestTemplate restTemplate = new RestTemplate();
        CustomRequestCallBack requestCallBack = new CustomRequestCallBack(jacksonSerializer, new GetHistoryTransactionsRequest(transactionsHashes));
        chunkService.transactionHandler(responseExtractor -> {
            restTemplate.execute(storageServerAddress + END_POINT, HttpMethod.POST, requestCallBack, responseExtractor);
        }, response);

    }

    private List<Hash> getTransactionsHashesToRetrieve(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        if (getTransactionsByAddressRequest.getAddress() == null) {
            return new ArrayList<>();
        }
        if (getTransactionsByAddressRequest.getStartDate() == null || getTransactionsByAddressRequest.getEndDate() == null) {
            return getTransactionsHashesByAddress(getTransactionsByAddressRequest.getAddress());
        }
        return getTransactionsHashesByAddressAndDates(getTransactionsByAddressRequest.getAddress(), getTransactionsByAddressRequest.getStartDate(), getTransactionsByAddressRequest.getEndDate());
    }


    private List<Hash> getTransactionsHashesByDate(Instant date) {
        if (date == null) {
            return new ArrayList<>();
        }
        AddressTransactionsByDate addressTransactionsByDate = addressTransactionsByDates.getByHash(getHashByLocalDate(getLocalDateByInstant(date)));
        if (addressTransactionsByDate == null || addressTransactionsByDate.getTransactionHashes() == null || addressTransactionsByDate.getTransactionHashes().isEmpty()) {
            return new ArrayList<>();
        }
        return addressTransactionsByDate.getTransactionHashes().stream().collect(Collectors.toList());
    }


    private Hash getHashByLocalDate(LocalDate localDate) {
        return CryptoHelper.cryptoHash(localDate.atStartOfDay().toString().getBytes());
    }

    private LocalDate getLocalDateByInstant(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        return LocalDate.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth());
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
        if (addressTransactionsByAddress == null) {
            return new ArrayList<>();
        }
        HashMap<LocalDate, HashSet<Hash>> transactionHashesByDates = addressTransactionsByAddress.getTransactionHashesByDates();
        return transactionHashesByDates.keySet().stream().flatMap(key -> transactionHashesByDates.get(key).stream()).collect(Collectors.toList());
    }

    private List<Hash> getTransactionsHashesByAddressAndDates(Hash address, Instant startDate, Instant endDate) {
        AddressTransactionsByAddress addressTransactionsByAddress = addressTransactionsByAddresses.getByHash(address);
        if (addressTransactionsByAddress == null) {
            return new ArrayList<>();
        }
        HashMap<LocalDate, HashSet<Hash>> transactionHashesByDates = addressTransactionsByAddress.getTransactionHashesByDates();
        List<LocalDate> localDatesBetween = getLocalDatesBetween(getLocalDateByInstant(startDate), getLocalDateByInstant(endDate));

        return localDatesBetween.stream().filter(localDate -> transactionHashesByDates.containsKey(localDate)).
                flatMap(localDate -> transactionHashesByDates.get(localDate).stream()).collect(Collectors.toList());
    }


    public void addToHistoryTransactionIndexes(TransactionData transactionData) {
        Instant attachmentTime = transactionData.getAttachmentTime();
        Hash hashByDate = calculateHashByAttachmentTime(attachmentTime);
        LocalDate attachmentLocalDate = calculateInstantLocalDate(attachmentTime);
        HashSet<Hash> relatedAddressHashes = getRelatedAddresses(transactionData);

        AddressTransactionsByDate transactionsByDateHash = addressTransactionsByDates.getByHash(hashByDate);
        if (transactionsByDateHash == null) {
            transactionsByDateHash = new AddressTransactionsByDate(attachmentTime, new HashSet<>());
        }
        transactionsByDateHash.getTransactionHashes().add(transactionData.getHash());
        addressTransactionsByDates.put(transactionsByDateHash);

        relatedAddressHashes.forEach(transactionAddressHash -> {
            AddressTransactionsByAddress transactionsByAddress = addressTransactionsByAddresses.getByHash(transactionAddressHash);
            if (transactionsByAddress == null) {
                transactionsByAddress = new AddressTransactionsByAddress(transactionAddressHash, new HashMap<>());
            }
            HashSet<Hash> transactionsHashesByDate = transactionsByAddress.getTransactionHashesByDates().get(attachmentLocalDate);
            if (transactionsHashesByDate == null) {
                transactionsHashesByDate = new HashSet<>();
                transactionsByAddress.getTransactionHashesByDates().put(attachmentLocalDate, transactionsHashesByDate);
            }
            transactionsHashesByDate.add(transactionData.getHash());
            addressTransactionsByAddresses.put(transactionsByAddress);
        });
    }

    private HashSet<Hash> getRelatedAddresses(TransactionData transactionData) {
        HashSet<Hash> hashes = new HashSet<>();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> hashes.add(baseTransactionData.getAddressHash()));
        return hashes;
    }

    protected Hash calculateHashByAttachmentTime(Instant date) {
        LocalDate localDate = calculateInstantLocalDate(date);
        return CryptoHelper.cryptoHash(localDate.toString().getBytes());
    }

    public LocalDate calculateInstantLocalDate(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        return LocalDate.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth());
    }

    protected ResponseEntity<AddHistoryEntitiesResponse> storeEntitiesByType(String url, AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return storageConnector.storeInStorage(url, addEntitiesBulkRequest, AddHistoryEntitiesResponse.class);
    }

    public ResponseEntity<AddHistoryEntitiesResponse> storeEntities(List<? extends IEntity> entities) {

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

        ResponseEntity<AddHistoryEntitiesResponse> storeEntitiesToStorageResponse = storeEntitiesByType(storageServerAddress + END_POINT, addEntitiesBulkRequest);
        return storeEntitiesToStorageResponse;
    }

}
