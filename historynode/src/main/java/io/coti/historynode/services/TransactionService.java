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
import io.coti.historynode.crypto.GetTransactionsByAddressRequestCrypto;
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
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;


@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    private final String END_POINT_STORE = "/transactions";
    private final String END_POINT_RETRIEVE = "/transactions/reactive";

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
    private GetTransactionsByAddressRequestCrypto getTransactionsByAddressRequestCrypto;
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
        return transactions.getByHash(unconfirmedTransactionHash);
    }

    protected void updateUnconfirmedTransactionsNotFromClusterStamp(List<Hash> unconfirmedTransactionHashesFromClusterStamp) {
        List<Hash> unconfirmedTransactionsHashesFromDB = getLocalTransactionsUnconfirmed();
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
            if (!getTransactionsByAddressRequestCrypto.verifySignature(getTransactionsByAddressRequest)) {
                customResponse.printResponse(new Response(INVALID_SIGNATURE, STATUS_ERROR), HttpStatus.UNAUTHORIZED.value());
                return;
            }
            List<Hash> transactionHashes = getTransactionHashesToRetrieve(getTransactionsByAddressRequest);
            getTransactions(transactionHashes, response);
        } catch (Exception e) {

        }
    }

    public void getTransactionsByDate(GetTransactionsByDateRequest getTransactionsByDateRequest, HttpServletResponse response) {

        List<Hash> transactionHashes = getTransactionHashesByDate(getTransactionsByDateRequest.getDate());
        getTransactions(transactionHashes, response);
    }

    private void getTransactions(List<Hash> transactionHashes, HttpServletResponse response) {
        try {
            CustomHttpServletResponse customResponse = new CustomHttpServletResponse(response);
            PrintWriter output = response.getWriter();
            if (transactionHashes.isEmpty()) {
                customResponse.printResponse("[]", HttpStatus.OK.value());
            }
            chunkService.startOfChunk(output);

            retrieveTransactions(transactionHashes, output);

            chunkService.endOfChunk(output);

        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }

    }

    private void retrieveTransactions(List<Hash> transactionHashes, PrintWriter output) {
        List<Hash> transactionHashesToRetrieveFromElasticSearch = new ArrayList<>();
        getTransactionsFromLocal(transactionHashes, transactionHashesToRetrieveFromElasticSearch, output);
        if (!transactionHashesToRetrieveFromElasticSearch.isEmpty()) {
            getTransactionFromElasticSearch(transactionHashesToRetrieveFromElasticSearch, output);
        }
    }


    private void getTransactionsFromLocal(List<Hash> transactionHashes, List<Hash> transactionsHashesToRetrieveFromElasticSearch, PrintWriter output) {

        transactionHashes.forEach(transactionHash -> {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData != null) {
                chunkService.transactionHandler(transactionData, output);
            } else {
                transactionsHashesToRetrieveFromElasticSearch.add(transactionHash);
            }
        });
    }

    private void getTransactionFromElasticSearch(List<Hash> transactionsHashes, PrintWriter output) {
        RestTemplate restTemplate = new RestTemplate();
        CustomRequestCallBack requestCallBack = new CustomRequestCallBack(jacksonSerializer, new GetHistoryTransactionsRequest(transactionsHashes));
        chunkService.transactionHandler(responseExtractor ->
                        restTemplate.execute(storageServerAddress + END_POINT_RETRIEVE, HttpMethod.POST, requestCallBack, responseExtractor)
                , output);

    }

    public List<Hash> getTransactionHashesToRetrieve(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        Hash addressHash = getTransactionsByAddressRequest.getAddress();
        if (addressHash == null) {
            return new ArrayList<>();
        }
        return getTransactionHashesByAddressAndDates(addressHash, getTransactionsByAddressRequest.getStartDate(), getTransactionsByAddressRequest.getEndDate());
    }


    public List<Hash> getTransactionHashesByDate(Instant date) {
        if (date == null) {
            return new ArrayList<>();
        }
        AddressTransactionsByDate addressTransactionsByDate = addressTransactionsByDates.getByHash(calculateHashByTime(date));
        if (addressTransactionsByDate == null || addressTransactionsByDate.getTransactionHashes() == null || addressTransactionsByDate.getTransactionHashes().isEmpty()) {
            return new ArrayList<>();
        }
        return addressTransactionsByDate.getTransactionHashes().stream().collect(Collectors.toList());
    }

    private List<Hash> getTransactionHashesByAddressAndDates(Hash address, LocalDate startDate, LocalDate endDate) {
        List<Hash> transactionHashes = new ArrayList<>();
        AddressTransactionsByAddress addressTransactionsByAddress = addressTransactionsByAddresses.getByHash(address);
        if (addressTransactionsByAddress == null) {
            return transactionHashes;
        }

        startDate = (startDate != null) ? startDate : addressTransactionsByAddress.getStartDate();
        endDate = (endDate != null) ? endDate : calculateInstantLocalDate(Instant.now());

        HashMap<LocalDate, HashSet<Hash>> transactionHashesByDates = addressTransactionsByAddress.getTransactionHashesByDates();
        while (!startDate.isAfter(endDate)) {
            transactionHashesByDates.get(startDate).forEach(transactionHash -> transactionHashes.add(transactionHash));
            startDate = startDate.plusDays(1);
        }
        return transactionHashes;
    }


    public void addToHistoryTransactionIndexes(TransactionData transactionData) {
        Instant attachmentTime = transactionData.getAttachmentTime();
        LocalDate attachmentLocalDate = calculateInstantLocalDate(attachmentTime);
        Hash hashByDate = calculateHashByLocalDate(attachmentLocalDate);
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
                transactionsByAddress = new AddressTransactionsByAddress(transactionAddressHash, new HashMap<>(), attachmentLocalDate);
            }
            if (transactionsByAddress.getStartDate().isAfter(attachmentLocalDate)) {
                transactionsByAddress.setStartDate(attachmentLocalDate);
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

    protected Hash calculateHashByTime(Instant date) {
        LocalDate localDate = calculateInstantLocalDate(date);
        return calculateHashByLocalDate(localDate);
    }

    public LocalDate calculateInstantLocalDate(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        return LocalDate.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth());
    }

    private Hash calculateHashByLocalDate(LocalDate localDate) {
        return CryptoHelper.cryptoHash(localDate.toString().getBytes());
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

        ResponseEntity<AddHistoryEntitiesResponse> storeEntitiesToStorageResponse = storeEntitiesByType(storageServerAddress + END_POINT_STORE, addEntitiesBulkRequest);
        return storeEntitiesToStorageResponse;
    }

}
