package io.coti.storagenode.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.ValidationService;
import io.coti.storagenode.http.GetEntitiesBulkJsonResponse;
import io.coti.storagenode.http.GetEntityJsonResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static io.coti.storagenode.http.HttpStringConstants.STATUS_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.TestUtils.createRandomTransaction;

@JsonIgnoreProperties(ignoreUnknown=true)
@ContextConfiguration(classes = {TransactionService.class, DbConnectorService.class,TransactionStorageValidationService.class,
        HistoryNodeConsensusResult.class, HistoryNodesConsensusService.class, CryptoHelper.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    private static final int NUMBER_OF_TRANSACTIONS = 4;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DbConnectorService dbConnectorService;

    @Autowired
    private TransactionStorageValidationService transactionStorageValidationService;

    @MockBean
    private HistoryNodeConsensusCrypto mockHistoryNodeConsensusCrypto;

    @MockBean
    private ValidationService mockValidationService; // mockValidationService

    @MockBean
    private Transactions mockTransactions;

    @MockBean
    private CryptoHelper mockCryptoHelper;

    @MockBean
    private HistoryNodesConsensusService mockHistoryNodesConsensusService;


    private ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper();
    }

    @Test
    public void transactionTest() throws IOException {
        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

        String transactionAsJson = mapper.writeValueAsString(transactionData1);
        transactionService.insertObjectJson(transactionData1.getHash(), transactionAsJson, false);

        IResponse deleteResponse = transactionService.deleteObjectByHash(transactionData2.getHash(), false).getBody();

        GetEntityJsonResponse response = (GetEntityJsonResponse) transactionService.getObjectByHash(transactionData1.getHash(), false).getBody();
        Assert.assertTrue(response.getStatus().equals(STATUS_SUCCESS) &&
                ((GetEntityJsonResponse) deleteResponse).status.equals(STATUS_SUCCESS));
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
        transactionService.insertMultiObjects(hashToTransactionJsonDataMap, false);

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(TransactionDataList.get(0).getHash());
        deleteHashes.add(TransactionDataList.get(1).getHash());

        IResponse deleteResponse = transactionService.deleteMultiObjectsFromDb(deleteHashes, false).getBody();

        List<Hash> GetHashes = new ArrayList<>();
        GetHashes.add(TransactionDataList.get(2).getHash());
        GetHashes.add(TransactionDataList.get(3).getHash());

        IResponse response = transactionService.getMultiObjectsFromDb(GetHashes, false).getBody();

        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(TransactionDataList.get(0).getHash()).equals(STATUS_OK)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(TransactionDataList.get(1).getHash()).equals(STATUS_OK));
    }

//    @Test
//    public void transactionDataMappingTest() throws IOException
//    {
//        TransactionData transactionData1 = createRandomTransaction();
//
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
//        Hash hash = generateRandomHash(64);
//        byte[] serializedHash1 = mapper.writeValueAsBytes(hash);
//        Hash deserializedHash1 = mapper.readValue(serializedHash1, Hash.class);
//        boolean eqHashMapperIgnore = deserializedHash1.equals(hash);
//
//        byte[] serializedTxData1 = SerializationUtils.serialize(transactionData1);
//        TransactionData deserializedTxData1 = SerializationUtils.deserialize(serializedTxData1);
//        boolean equals = deserializedTxData1.equals(transactionData1);
//
//
////        boolean equals1 = serializedTxData1.toString().getBytes().equals(serializedTxData1);
//
////        String tmpStr = mapper.readValue(serializedTxData1, String.class);
////        byte[] tmpBytes = mapper.writeValueAsBytes(tmpStr);
////        boolean equalBytesTmp = tmpBytes.equals(serializedTxData1);
//
//
//        String bytesToString = serializedTxData1.toString();
//        String bytesToString2 = mapper.writeValueAsString(serializedTxData1);
//
//
//        TransactionData txDataDeserialized = mapper.readValue(bytesToString2, TransactionData.class);
//        boolean equalStrings = txDataDeserialized.equals(transactionData1);
////
////        String transactionAsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transactionData1);
//
//        Assert.assertTrue(deserializedHash1.equals(hash));
//        Assert.assertTrue(txDataDeserialized.equals(transactionData1));
//
//        int stam = 7;
//    }

    @Test
    public void singleTxsStoreRetrieveTest() throws IOException
    {
        // Mocks set-ups
        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);
        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any(),any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList(), any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockValidationService.validateTransactionDataIntegrity(any())).thenReturn(true); // TransactionData.class // to catch also null values

        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        String transactionAsJson1 = mapper.writeValueAsString(transactionData1);
        String transactionAsJson2 = mapper.writeValueAsString(transactionData2);

        HistoryNodeConsensusResult historyNodeConsensusResult1 = createHistoryNodeConsensusResultForTx(transactionData1);
        HistoryNodeConsensusResult historyNodeConsensusResult2 = createHistoryNodeConsensusResultForTx(transactionData2);

        ResponseEntity<IResponse> responseResponseEntity1 = transactionStorageValidationService.storeObjectToStorage(
                transactionData1.getHash(), transactionAsJson1, historyNodeConsensusResult1);
        ResponseEntity<IResponse> responseResponseEntity2 = transactionStorageValidationService.storeObjectToStorage(
                transactionData2.getHash(), transactionAsJson2, historyNodeConsensusResult2);

        Hash txHash = transactionData1.getHash();
        ResponseEntity<IResponse> responseRetrieveEntity1 = transactionStorageValidationService.retrieveObjectFromStorage(txHash, historyNodeConsensusResult1);
        String entityAsJsonFromES = String.valueOf(responseRetrieveEntity1.getBody());
        TransactionData txDataDeserializedFromES1 = mapper.readValue(entityAsJsonFromES, TransactionData.class);

        Hash txHash2 = transactionData2.getHash();
        ResponseEntity<IResponse> responseRetrieveEntity2 = transactionStorageValidationService.retrieveObjectFromStorage(txHash2, historyNodeConsensusResult2);
        String entityAsJsonFromES2 = String.valueOf(responseRetrieveEntity2.getBody());
        TransactionData txDataDeserializedFromES2 = mapper.readValue(entityAsJsonFromES2, TransactionData.class);


        boolean eqFromES = txDataDeserializedFromES1.equals(transactionData1);

        Assert.assertTrue(txDataDeserializedFromES1.equals(transactionData1));
        Assert.assertTrue(txDataDeserializedFromES2.equals(transactionData2));
    }

    @Test
    public void multipleTxsStoreRetrieveTest() throws IOException
    {
        // Setups and Mocks
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);;
        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any(),any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList(), any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockValidationService.validateTransactionDataIntegrity(any())).thenReturn(true); // TransactionData.class // to catch also null values

        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

        String transactionAsJson1 = mapper.writeValueAsString(transactionData1);
        String transactionAsJson2 = mapper.writeValueAsString(transactionData2);

        // TODO update method to receive Map of TxData instead of one
        HistoryNodeConsensusResult historyNodeConsensusResult1 = createHistoryNodeConsensusResultForTx(transactionData1);
        HistoryNodeConsensusResult historyNodeConsensusResult2 = createHistoryNodeConsensusResultForTx(transactionData2);

        Map<Hash, String> txDataJsonToHash = new HashMap<>();
        txDataJsonToHash.put(transactionData1.getHash(),transactionAsJson1);
        txDataJsonToHash.put(transactionData2.getHash(),transactionAsJson2);

        ResponseEntity<IResponse> responseResponseEntity1 = transactionStorageValidationService.storeMultipleObjectsToStorage(txDataJsonToHash, historyNodeConsensusResult1);
        Assert.assertTrue(responseResponseEntity1.getStatusCode().equals(HttpStatus.OK));

        ResponseEntity<IResponse> responseEntity1 = transactionStorageValidationService.retrieveObjectFromStorage(transactionData1.getHash(), historyNodeConsensusResult1);
        ResponseEntity<IResponse> responseEntity2 = transactionStorageValidationService.retrieveObjectFromStorage(transactionData2.getHash(), historyNodeConsensusResult2);

        String entityAsJsonFromES1 = String.valueOf(responseEntity1.getBody());
        TransactionData txDataDeserializedFromES1 = mapper.readValue(entityAsJsonFromES1, TransactionData.class);
        String entityAsJsonFromES2 = String.valueOf(responseEntity2.getBody());
        TransactionData txDataDeserializedFromES2 = mapper.readValue(entityAsJsonFromES2, TransactionData.class);

        Assert.assertTrue(txDataDeserializedFromES1.equals(transactionData1));
        Assert.assertTrue(txDataDeserializedFromES2.equals(transactionData2));

        // Add entries again as after retrieval they were removed from cold-storage, which is currently the same as main-storage
        responseResponseEntity1 = transactionStorageValidationService.storeMultipleObjectsToStorage(txDataJsonToHash, historyNodeConsensusResult1);
        Assert.assertTrue(responseResponseEntity1.getStatusCode().equals(HttpStatus.OK));

        List<Hash> hashes = new ArrayList<>();
        hashes.add( transactionData1.getHash() );
        hashes.add( transactionData2.getHash() );
        Map<Hash, ResponseEntity<IResponse>> hashResponseEntityMap = transactionStorageValidationService.retrieveMultipleObjectsFromStorage(hashes, historyNodeConsensusResult1);

        String entityAsJsonFromES3 = String.valueOf( hashResponseEntityMap.get( transactionData1.getHash() ).getBody() );
        TransactionData txDataDeserializedFromES3 = mapper.readValue(entityAsJsonFromES3, TransactionData.class);
        String entityAsJsonFromES4 = String.valueOf( hashResponseEntityMap.get( transactionData2.getHash() ).getBody() );
        TransactionData txDataDeserializedFromES4 = mapper.readValue(entityAsJsonFromES4, TransactionData.class);

        Assert.assertTrue(txDataDeserializedFromES3.equals(transactionData1));
        Assert.assertTrue(txDataDeserializedFromES4.equals(transactionData2));
    }

    private HistoryNodeConsensusResult createHistoryNodeConsensusResultForTx(TransactionData transactionData) throws JsonProcessingException {
        HistoryNodeConsensusResult histNodeConsensusResult = new HistoryNodeConsensusResult(transactionData.getHash());
        histNodeConsensusResult.setHistoryNodeMasterHash(transactionData.getSenderHash());
        histNodeConsensusResult.setSignature(transactionData.getSenderSignature());
        List<HistoryNodeVote> historyNodesVotes = new ArrayList<>();

        boolean isValidRequest = true;
        Hash requestHash = transactionData.getHash();
        HistoryNodeVote historyNodeVote = new HistoryNodeVote(requestHash, isValidRequest);
        historyNodesVotes.add(historyNodeVote);
        histNodeConsensusResult.setHistoryNodesVotesList(historyNodesVotes);

        Map<Hash, String> hashToObjectJsonDataMap = new HashMap<>();
        hashToObjectJsonDataMap.put(transactionData.getHash(), mapper.writeValueAsString(transactionData) );
        histNodeConsensusResult.setHashToObjectJsonDataMap(hashToObjectJsonDataMap);

        return histNodeConsensusResult;
    }


}