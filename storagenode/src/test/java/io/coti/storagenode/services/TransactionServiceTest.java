package io.coti.storagenode.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetHistoryTransactionsRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.DbConnectorService;
import org.elasticsearch.rest.RestStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.TestUtils.createRandomTransaction;

@JsonIgnoreProperties(ignoreUnknown = true)
@ContextConfiguration(classes = {ObjectService.class, DbConnectorService.class, TransactionStorageService.class,
        CryptoHelper.class, AddressStorageService.class, JacksonSerializer.class
})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    private static final int NUMBER_OF_TRANSACTIONS = 4;

    @Autowired
    private ObjectService transactionService;

    @Autowired
    private DbConnectorService dbConnectorService;

    @Autowired
    private TransactionStorageService transactionStorageValidationService;

    @Autowired
    private JacksonSerializer jacksonSerializer;

    @MockBean
    private BaseNodeValidationService mockValidationService; // mockValidationService

    @MockBean
    private Transactions mockTransactions;

    @MockBean
    private CryptoHelper mockCryptoHelper;

    @MockBean
    private AddressStorageService addressStorageService;


    private ObjectMapper mapper;

    @Before
    public void init() {
//        mapper = new ObjectMapper();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
    }

    @Test
    public void transactionTest() throws IOException {
        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

        String transactionAsJson = mapper.writeValueAsString(transactionData1);
        transactionService.insertObjectJson(transactionData1.getHash(), transactionAsJson, false, ElasticSearchData.TRANSACTIONS);

        RestStatus deleteRestStatus = transactionService.deleteObjectByHash(transactionData2.getHash(), false, ElasticSearchData.TRANSACTIONS);
        Assert.assertTrue(deleteRestStatus.equals(RestStatus.NOT_FOUND));
        String objectJsonByHash = transactionService.getObjectByHash(transactionData1.getHash(), false, ElasticSearchData.TRANSACTIONS);
        Assert.assertTrue(mapper.readValue(objectJsonByHash, TransactionData.class).equals(transactionData1));
        Assert.assertTrue(jacksonSerializer.deserialize(objectJsonByHash).equals(transactionData1));
    }

    @Test
    public void insertMultiObjects_sameAttachmentDate() throws IOException {
        Map<Hash, String> hashToTransactionJsonDataMap = new HashMap<>();
        List<TransactionData> TransactionDataList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            TransactionData transactionData = createRandomTransaction();
            transactionData.setAttachmentTime(Instant.now());
            TransactionDataList.add(transactionData);
            hashToTransactionJsonDataMap.put(transactionData.getHash(), mapper.writeValueAsString(transactionData));
        }
        Map<Hash, RestStatus> hashToRestStatusInsertResponseMap = transactionService.insertMultiObjects(hashToTransactionJsonDataMap, false, ElasticSearchData.TRANSACTIONS);

        Assert.assertTrue(hashToRestStatusInsertResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.CREATED)));
        Assert.assertTrue(hashToRestStatusInsertResponseMap.keySet().containsAll(hashToTransactionJsonDataMap.keySet()));

        List<Hash> storedHashes = hashToTransactionJsonDataMap.keySet().stream().collect(Collectors.toList());
        Map<Hash, String> hashToRestStatusGetResponseMap = transactionService.getMultiObjectsFromDb(storedHashes, false, ElasticSearchData.TRANSACTIONS);
        Assert.assertTrue(hashToRestStatusGetResponseMap.keySet().stream().allMatch(hash -> storedHashes.contains(hash)));
        Assert.assertTrue(hashToRestStatusGetResponseMap.entrySet().stream().allMatch(entry -> TransactionDataList.contains(jacksonSerializer.deserialize(entry.getValue()))));
    }

    @Test
    public void multiTransactionTest() throws IOException {
        Map<Hash, String> hashToTransactionJsonDataMap = new HashMap<>();
        List<TransactionData> TransactionDataList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            TransactionData transactionData = createRandomTransaction();
            TransactionDataList.add(transactionData);
            hashToTransactionJsonDataMap.put(transactionData.getHash(), mapper.writeValueAsString(transactionData));
        }
        Map<Hash, RestStatus> hashToRestStatusInsertResponseMap = transactionService.insertMultiObjects(hashToTransactionJsonDataMap, false, ElasticSearchData.TRANSACTIONS);

        Assert.assertTrue(hashToRestStatusInsertResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.CREATED)));
        Assert.assertTrue(hashToRestStatusInsertResponseMap.keySet().containsAll(hashToTransactionJsonDataMap.keySet()));

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(TransactionDataList.get(0).getHash());
        deleteHashes.add(TransactionDataList.get(1).getHash());

        Map<Hash, RestStatus> hashToRestStatusDeleteResponseMap = transactionService.deleteMultiObjectsFromDb(deleteHashes, false, ElasticSearchData.TRANSACTIONS);
        Assert.assertTrue(hashToRestStatusDeleteResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.OK)));

        List<Hash> getHashes = new ArrayList<>();
        getHashes.add(TransactionDataList.get(2).getHash());
        getHashes.add(TransactionDataList.get(3).getHash());

        Map<Hash, String> hashToRestStatusGetResponseMap = transactionService.getMultiObjectsFromDb(getHashes, false, ElasticSearchData.TRANSACTIONS);

        Assert.assertTrue(hashToRestStatusGetResponseMap.keySet().stream().allMatch(hash -> getHashes.contains(hash)));
    }


    @Test
    public void singleTxsStoreRetrieveTest() throws IOException {
        // Mocks set-ups
//        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);
//        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any())).thenReturn(mockedResponse);
//        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList())).thenReturn(mockedResponse);
        when(mockValidationService.validateTransactionDataIntegrity(any())).thenReturn(true); // TransactionData.class // to catch also null values

        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        String transactionAsJson1 = mapper.writeValueAsString(transactionData1);
        String transactionAsJson2 = mapper.writeValueAsString(transactionData2);

        ResponseEntity<IResponse> responseResponseEntity1 = transactionStorageValidationService.storeObjectToStorage(
                transactionData1.getHash(), transactionAsJson1);
        ResponseEntity<IResponse> responseResponseEntity2 = transactionStorageValidationService.storeObjectToStorage(
                transactionData2.getHash(), transactionAsJson2);

        Hash txHash = transactionData1.getHash();
        TransactionData retrievedTransactionData1 = transactionStorageValidationService.retrieveObjectFromStorage(txHash);

        Hash txHash2 = transactionData2.getHash();
        TransactionData retrievedTransactionData2 = transactionStorageValidationService.retrieveObjectFromStorage(txHash2);

        Assert.assertTrue(retrievedTransactionData1.equals(transactionData1));
        Assert.assertTrue(retrievedTransactionData2.equals(transactionData2));
    }

    @Test
    public void multipleTxsStoreRetrieveTest() throws IOException {
        // Setups and Mocks
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

//        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);
        ;
//        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any())).thenReturn(mockedResponse);
//        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList())).thenReturn(mockedResponse);
        when(mockValidationService.validateTransactionDataIntegrity(any())).thenReturn(true); // TransactionData.class // to catch also null values

        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

        String transactionAsJson1 = mapper.writeValueAsString(transactionData1);
        String transactionAsJson2 = mapper.writeValueAsString(transactionData2);

        Map<Hash, String> txDataJsonToHash = new HashMap<>();
        txDataJsonToHash.put(transactionData1.getHash(), transactionAsJson1);
        txDataJsonToHash.put(transactionData2.getHash(), transactionAsJson2);

        ResponseEntity<IResponse> responseResponseEntity1 = transactionStorageValidationService.storeMultipleObjectsToStorage(txDataJsonToHash);
        Assert.assertTrue(responseResponseEntity1.getStatusCode().equals(HttpStatus.OK));
        Assert.assertTrue(((AddHistoryEntitiesResponse) responseResponseEntity1.getBody()).getStatus().equals("Success"));
        Assert.assertTrue(((AddHistoryEntitiesResponse) responseResponseEntity1.getBody()).getHashToStoreResultMap().values().stream().allMatch(Boolean::booleanValue));

        TransactionData retrievedTransactionData1 = transactionStorageValidationService.retrieveObjectFromStorage(transactionData1.getHash());
        TransactionData retrievedTransactionData2 = transactionStorageValidationService.retrieveObjectFromStorage(transactionData2.getHash());

        Assert.assertTrue(transactionData1.equals(retrievedTransactionData1));
        Assert.assertTrue(transactionData2.equals(retrievedTransactionData2));

        // Add entries again as after retrieval they were removed from cold-storage, which is currently the same as main-storage
        responseResponseEntity1 = transactionStorageValidationService.storeMultipleObjectsToStorage(txDataJsonToHash);
        Assert.assertTrue(responseResponseEntity1.getStatusCode().equals(HttpStatus.OK));
        Assert.assertTrue(((BaseResponse) responseResponseEntity1.getBody()).getStatus().equals("Success"));
        Assert.assertTrue(((AddHistoryEntitiesResponse) responseResponseEntity1.getBody()).getHashToStoreResultMap().values().stream().allMatch(Boolean::booleanValue));


        List<Hash> hashes = new ArrayList<>();
        hashes.add(transactionData1.getHash());
        hashes.add(transactionData2.getHash());

        GetHistoryTransactionsRequest bulkRequest = new GetHistoryTransactionsRequest(hashes);
        Map<Hash, String> hashToTransactionDataAsJson = transactionStorageValidationService.retrieveMultipleObjectsFromStorage(bulkRequest.getTransactionHashes());


        String entityAsJsonFromES3 = hashToTransactionDataAsJson.get(transactionData1.getHash());
        TransactionData txDataDeserializedFromES3 = jacksonSerializer.deserialize(entityAsJsonFromES3);
        String entityAsJsonFromES4 = String.valueOf(hashToTransactionDataAsJson.get(transactionData2.getHash()));
        TransactionData txDataDeserializedFromES4 = jacksonSerializer.deserialize(entityAsJsonFromES4);

        Assert.assertTrue(txDataDeserializedFromES3.equals(transactionData1));
        Assert.assertTrue(txDataDeserializedFromES4.equals(transactionData2));
    }

//    @Deprecated
//    private HistoryNodeConsensusResult createHistoryNodeConsensusResultForTx(TransactionData transactionData) throws JsonProcessingException {
////        HistoryNodeConsensusResult histNodeConsensusResult = new HistoryNodeConsensusResult(transactionData.getHash());
//        histNodeConsensusResult.setHistoryNodeMasterHash(transactionData.getSenderHash());
//        histNodeConsensusResult.setSignature(transactionData.getSenderSignature());
//        List<HistoryNodeVote> historyNodesVotes = new ArrayList<>();
//
//        boolean isValidRequest = true;
//        Hash requestHash = transactionData.getHash();
//        HistoryNodeVote historyNodeVote = new HistoryNodeVote(requestHash, isValidRequest);
//        historyNodesVotes.add(historyNodeVote);
//        histNodeConsensusResult.setHistoryNodesVotesList(historyNodesVotes);
//
//        Map<Hash, String> hashToObjectJsonDataMap = new HashMap<>();
//        hashToObjectJsonDataMap.put(transactionData.getHash(), mapper.writeValueAsString(transactionData) );
//        histNodeConsensusResult.setHashToObjectJsonDataMap(hashToObjectJsonDataMap);
//
//        return histNodeConsensusResult;
//    }


}