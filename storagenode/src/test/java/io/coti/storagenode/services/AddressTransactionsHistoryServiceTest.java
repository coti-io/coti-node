package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.HistoryNodeConsensusCrypto;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeValidationService;
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
import static testUtils.TestUtils.ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME;
import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {AddressTransactionsHistoryService.class, DbConnectorService.class, AddressStorageValidationService.class,
        HistoryNodeConsensusResult.class, HistoryNodesConsensusService.class, CryptoHelper.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressTransactionsHistoryServiceTest {

    private static final int NUMBER_OF_ADDRESSES = 4;
    @Autowired
    private AddressTransactionsHistoryService addressTransactionsHistoryService;

    @Autowired
    private DbConnectorService dbConnectorService;

    private ObjectMapper mapper;

    @Autowired
    private AddressStorageValidationService addressStorageValidationService;

    @MockBean
    private HistoryNodeConsensusCrypto mockHistoryNodeConsensusCrypto;

    @MockBean
    private BaseNodeValidationService mockValidationService;

    @MockBean
    private CryptoHelper mockCryptoHelper;

    @MockBean
    private HistoryNodesConsensusService mockHistoryNodesConsensusService;

    @Before
    public void init() {
//        mapper = new ObjectMapper();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
    }

    @Test
    public void AddressTransactionsHistoryTest() throws IOException {
        AddressTransactionsHistory addressTransactionsHistory1 = new AddressTransactionsHistory(generateRandomHash());
        AddressTransactionsHistory addressTransactionsHistory2 = new AddressTransactionsHistory(generateRandomHash());

        String addressTransactionsHistoryAsJson = mapper.writeValueAsString(addressTransactionsHistory1);
        ResponseEntity<IResponse> insertResponseEntity1 = addressTransactionsHistoryService.insertObjectJson(addressTransactionsHistory1.getHash(), addressTransactionsHistoryAsJson, false);
        ResponseEntity<IResponse> insertResponseEntity12 = addressTransactionsHistoryService.insertObjectJson(addressTransactionsHistory2.getHash(), addressTransactionsHistoryAsJson, false);

        IResponse deleteResponse = addressTransactionsHistoryService.deleteObjectByHash(addressTransactionsHistory2.getHash(), false).getBody();

        IResponse getResponse = addressTransactionsHistoryService.getObjectByHash(addressTransactionsHistory1.getHash(), false, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME).getBody();
        Assert.assertTrue(((BaseResponse) (getResponse)).getStatus().equals(STATUS_SUCCESS) &&
                ((GetEntityJsonResponse) deleteResponse).status.equals(STATUS_SUCCESS));
    }

    @Test
    public void multiAddressTransactionsHistoryTest() throws IOException {
        Map<Hash, String> hashToAddressTransactionsHistoryJsonDataMap = new HashMap<>();
        List<AddressTransactionsHistory> addressTransactionsHistories = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressTransactionsHistory addressTransactionsHistory = new AddressTransactionsHistory(generateRandomHash());
            addressTransactionsHistories.add(addressTransactionsHistory);
            hashToAddressTransactionsHistoryJsonDataMap.put(addressTransactionsHistory.getHash(), mapper.writeValueAsString(addressTransactionsHistory));
        }
        addressTransactionsHistoryService.insertMultiObjects(hashToAddressTransactionsHistoryJsonDataMap, false);

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(addressTransactionsHistories.get(0).getHash());
        deleteHashes.add(addressTransactionsHistories.get(1).getHash());

        IResponse deleteResponse = addressTransactionsHistoryService.deleteMultiObjectsFromDb(deleteHashes, false).getBody();

        List<Hash> GetHashes = new ArrayList<>();
        GetHashes.add(addressTransactionsHistories.get(2).getHash());
        GetHashes.add(addressTransactionsHistories.get(3).getHash());

        IResponse response = addressTransactionsHistoryService.getMultiObjectsFromDb(GetHashes, false, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME).getBody();

        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(addressTransactionsHistories.get(0).getHash()).equals(STATUS_OK)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(addressTransactionsHistories.get(1).getHash()).equals(STATUS_OK));
    }

    @Test
    public void singleAddressStoreRetrieveTest() throws IOException
    {
        // Mocks set-ups
        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);
        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any(),any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList(), any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockValidationService.validateAddress(any(Hash.class))).thenReturn(Boolean.TRUE);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<AddressTransactionsHistory> addressTxsHistories = new ArrayList<>();
        Map<Hash, String> hashToAddressTxsHistoryJsonDataMap = new HashMap<>();

        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressTransactionsHistory addressTransactionsHistory = new AddressTransactionsHistory(generateRandomHash());
            addressTxsHistories.add(addressTransactionsHistory);
            hashToAddressTxsHistoryJsonDataMap.put(addressTransactionsHistory.getHash(), mapper.writeValueAsString(addressTransactionsHistory));
        }

        // Store Address
        HistoryNodeConsensusResult consensus = new HistoryNodeConsensusResult(addressTxsHistories.get(0).getHash());
        ResponseEntity<IResponse> storeResponse = addressStorageValidationService.storeObjectToStorage(addressTxsHistories.get(0).getHash(),
                hashToAddressTxsHistoryJsonDataMap.get(addressTxsHistories.get(0).getHash()), consensus);
        Assert.assertTrue( storeResponse.getStatusCode().equals(HttpStatus.OK) );

        // Retrieve Address
        ResponseEntity<IResponse> retrievedAddressResponse = addressStorageValidationService.retrieveObjectFromStorage(addressTxsHistories.get(0).getHash(), consensus);
        Assert.assertTrue( retrievedAddressResponse.getStatusCode().equals(HttpStatus.OK) );
        AddressTransactionsHistory retrievedAddress = mapper.readValue(String.valueOf(retrievedAddressResponse.getBody()), AddressTransactionsHistory.class);

        Assert.assertTrue( retrievedAddress.equals(addressTxsHistories.get(0)) );

        boolean bPause = true;
    }


    @Test
    public void multipleAddressStoreRetrieveTest() throws IOException
    {
        // Mocks set-ups
        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);
        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any(),any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList(), any(HistoryNodeConsensusResult.class))).thenReturn(mockedResponse);
        when(mockValidationService.validateAddress(any(Hash.class))).thenReturn(Boolean.TRUE);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<AddressTransactionsHistory> addressTxsHistories = new ArrayList<>();
        Map<Hash, String> hashToAddressTxsHistoryJsonDataMap = new HashMap<>();

        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressTransactionsHistory addressTransactionsHistory = new AddressTransactionsHistory(generateRandomHash());
            addressTxsHistories.add(addressTransactionsHistory);
            hashToAddressTxsHistoryJsonDataMap.put(addressTransactionsHistory.getHash(), mapper.writeValueAsString(addressTransactionsHistory));
        }

        // Store Addresses
        Hash hash0 = addressTxsHistories.get(0).getHash();
        HistoryNodeConsensusResult consensus = new HistoryNodeConsensusResult(hash0);
        ResponseEntity<IResponse> storeResponse = addressStorageValidationService.storeMultipleObjectsToStorage(hashToAddressTxsHistoryJsonDataMap, consensus);
        Assert.assertTrue( storeResponse.getStatusCode().equals(HttpStatus.OK) );

        // Retrieve Addresses
        ResponseEntity<IResponse> retrievedAddressResponse = addressStorageValidationService.retrieveObjectFromStorage(hash0, consensus);
        Assert.assertTrue( retrievedAddressResponse.getStatusCode().equals(HttpStatus.OK) );
        AddressTransactionsHistory retrievedAddress = mapper.readValue(String.valueOf(retrievedAddressResponse.getBody()), AddressTransactionsHistory.class);
        Assert.assertTrue( retrievedAddress.equals(addressTxsHistories.get(0)) );

        List<Hash> addressesToGet = new ArrayList<>();
        Hash hash1 = addressTxsHistories.get(1).getHash();
        Hash hash2 = addressTxsHistories.get(2).getHash();
        addressesToGet.add(hash1);
        addressesToGet.add(hash2);
        Map<Hash, ResponseEntity<IResponse>> hashResponseEntityMap = addressStorageValidationService.retrieveMultipleObjectsFromStorage(addressesToGet, consensus);
        Assert.assertTrue( hashResponseEntityMap.get(hash1).getStatusCode().equals(HttpStatus.OK) );
        Assert.assertTrue( hashResponseEntityMap.get(hash2).getStatusCode().equals(HttpStatus.OK) );
        AddressTransactionsHistory retrievedAddress1 = mapper.readValue(String.valueOf( hashResponseEntityMap.get(hash1).getBody()), AddressTransactionsHistory.class);
        AddressTransactionsHistory retrievedAddress2 = mapper.readValue(String.valueOf( hashResponseEntityMap.get(hash2).getBody()), AddressTransactionsHistory.class);
        Assert.assertTrue( retrievedAddress1.equals(addressTxsHistories.get(1)) );
        Assert.assertTrue( retrievedAddress2.equals(addressTxsHistories.get(2)) );
    }


}