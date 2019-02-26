package io.coti.storagenode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.data.HistoryNodeVote;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.ValidationService;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
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

@ContextConfiguration(classes = {TransactionService.class, DbConnectorService.class,TransactionStorageValidationService.class,
        HistoryNodeConsensusResult.class, HistoryNodesConsensusService.class, CryptoHelper.class})// , HistoryNodeConsensusCrypto.class, CryptoHelper.class,
//        NodeCryptoHelper.class, ValidationService.class, Transactions.class })
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

    @Test
    public void transactionStorageTest() throws IOException
    {

        // Mocks set-ups
        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);;
//        mockedResponse = ResponseEntity.ok(null);
        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any(),any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList(), any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockValidationService.validateTransactionDataIntegrity(any())).thenReturn(true); // TransactionData.class // to catch also null values


//        historyNodeConsensusCrypto.verifySignature(historyNodeConsensusResult)
        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

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
        boolean bPause = true;
//
////        CryptoHelper
//        responseRetrieveEntity1.getStatusCodeValue();

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