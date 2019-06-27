package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetEntitiesBulkResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.storagenode.database.DbConnectorService;
import io.coti.storagenode.http.GetEntitiesBulkJsonResponse;
import io.coti.storagenode.http.GetEntityJsonResponse;
import io.coti.storagenode.model.AddressService;
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


@ContextConfiguration(classes = {AddressService.class, DbConnectorService.class, AddressStorageService.class,
        HistoryNodesConsensusService.class, CryptoHelper.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressTransactionsHistoryServiceTest {

    private static final int NUMBER_OF_ADDRESSES = 4;
    @Autowired
    private AddressService addressService;

    @Autowired
    private DbConnectorService dbConnectorService;

    private ObjectMapper mapper;

    @Autowired
    private AddressStorageService addressStorageValidationService;

//    @MockBean
//    private HistoryNodeConsensusCrypto mockHistoryNodeConsensusCrypto;

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
    public void AddressDataTest() throws IOException {
        AddressData addressData1 = new AddressData(generateRandomHash());
        AddressData addressData2 = new AddressData(generateRandomHash());

        String addressDataAsJson = mapper.writeValueAsString(addressData1);
        ResponseEntity<IResponse> insertResponseEntity1 = addressService.insertObjectJson(addressData1.getHash(), addressDataAsJson, false);
        ResponseEntity<IResponse> insertResponseEntity12 = addressService.insertObjectJson(addressData2.getHash(), addressDataAsJson, false);

        IResponse deleteResponse = addressService.deleteObjectByHash(addressData2.getHash(), false).getBody();

        IResponse getResponse = addressService.getObjectByHash(addressData1.getHash(), false, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME).getBody();
        Assert.assertTrue(((BaseResponse) (getResponse)).getStatus().equals(STATUS_SUCCESS) &&
                ((GetEntityJsonResponse) deleteResponse).status.equals(STATUS_SUCCESS));
    }

    @Test
    public void multiAddressDataTest() throws IOException {
        Map<Hash, String> hashToAddressDataJsonDataMap = new HashMap<>();
        List<AddressData> addressTransactionsHistories = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressData addressData = new AddressData(generateRandomHash());
            addressTransactionsHistories.add(addressData);
            hashToAddressDataJsonDataMap.put(addressData.getHash(), mapper.writeValueAsString(addressData));
        }
        addressService.insertMultiObjects(hashToAddressDataJsonDataMap, false);

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(addressTransactionsHistories.get(0).getHash());
        deleteHashes.add(addressTransactionsHistories.get(1).getHash());

        IResponse deleteResponse = addressService.deleteMultiObjectsFromDb(deleteHashes, false).getBody();

        List<Hash> GetHashes = new ArrayList<>();
        GetHashes.add(addressTransactionsHistories.get(2).getHash());
        GetHashes.add(addressTransactionsHistories.get(3).getHash());

        IResponse response = addressService.getMultiObjectsFromDb(GetHashes, false, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME).getBody();

        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(addressTransactionsHistories.get(0).getHash()).equals(STATUS_OK)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(addressTransactionsHistories.get(1).getHash()).equals(STATUS_OK));
    }

    @Test
    public void singleAddressStoreRetrieveTest() throws IOException
    {
        // Mocks set-ups
//        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);
        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any())).thenReturn(mockedResponse);
        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList())).thenReturn(mockedResponse);
        when(mockValidationService.validateAddress(any(Hash.class))).thenReturn(Boolean.TRUE);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<AddressData> addressTxsHistories = new ArrayList<>();
        Map<Hash, String> hashToAddressTxsHistoryJsonDataMap = new HashMap<>();

        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressData addressData = new AddressData(generateRandomHash());
            addressTxsHistories.add(addressData);
            hashToAddressTxsHistoryJsonDataMap.put(addressData.getHash(), mapper.writeValueAsString(addressData));
        }

        // Store Address
//        HistoryNodeConsensusResult consensus = new HistoryNodeConsensusResult(addressTxsHistories.get(0).getHash());
        ResponseEntity<IResponse> storeResponse = addressStorageValidationService.storeObjectToStorage(addressTxsHistories.get(0).getHash(),
                hashToAddressTxsHistoryJsonDataMap.get(addressTxsHistories.get(0).getHash()));
        Assert.assertTrue( storeResponse.getStatusCode().equals(HttpStatus.OK) );

        // Retrieve Address
        ResponseEntity<IResponse> retrievedAddressResponse = addressStorageValidationService.retrieveObjectFromStorage(addressTxsHistories.get(0).getHash());
        Assert.assertTrue( retrievedAddressResponse.getStatusCode().equals(HttpStatus.OK) );
        AddressData retrievedAddress = mapper.readValue(String.valueOf(retrievedAddressResponse.getBody()), AddressData.class);

        Assert.assertTrue( retrievedAddress.equals(addressTxsHistories.get(0)) );

        boolean bPause = true;
    }


    @Test
    public void multipleAddressStoreRetrieveTest() throws IOException
    {
        // Mocks set-ups
//        when(mockHistoryNodeConsensusCrypto.verifySignature(any(HistoryNodeConsensusResult.class))).thenReturn(true);
        ResponseEntity<IResponse> mockedResponse = new ResponseEntity(HttpStatus.OK);
        when(mockHistoryNodesConsensusService.validateStoreMultipleObjectsConsensus(Matchers.<Map<Hash, String>> any())).thenReturn(mockedResponse);
        when(mockHistoryNodesConsensusService.validateRetrieveMultipleObjectsConsensus(Matchers.anyList())).thenReturn(mockedResponse);
        when(mockValidationService.validateAddress(any(Hash.class))).thenReturn(Boolean.TRUE);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<AddressData> addressTxsHistories = new ArrayList<>();
        Map<Hash, String> hashToAddressTxsHistoryJsonDataMap = new HashMap<>();

        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressData addressData = new AddressData(generateRandomHash());
            addressTxsHistories.add(addressData);
            hashToAddressTxsHistoryJsonDataMap.put(addressData.getHash(), mapper.writeValueAsString(addressData));
        }

        // Store Addresses
        Hash hash0 = addressTxsHistories.get(0).getHash();
//        HistoryNodeConsensusResult consensus = new HistoryNodeConsensusResult(hash0);
        ResponseEntity<IResponse> storeResponse = addressStorageValidationService.storeMultipleObjectsToStorage(hashToAddressTxsHistoryJsonDataMap);
        Assert.assertTrue( storeResponse.getStatusCode().equals(HttpStatus.OK) );

        // Retrieve Addresses
        ResponseEntity<IResponse> retrievedAddressResponse = addressStorageValidationService.retrieveObjectFromStorage(hash0);
        Assert.assertTrue( retrievedAddressResponse.getStatusCode().equals(HttpStatus.OK) );
        AddressData retrievedAddress = mapper.readValue(String.valueOf(retrievedAddressResponse.getBody()), AddressData.class);
        Assert.assertTrue( retrievedAddress.equals(addressTxsHistories.get(0)) );

        List<Hash> addressesToGet = new ArrayList<>();
        Hash hash1 = addressTxsHistories.get(1).getHash();
        Hash hash2 = addressTxsHistories.get(2).getHash();
        addressesToGet.add(hash1);
        addressesToGet.add(hash2);
        GetEntitiesBulkResponse hashResponseEntities = addressStorageValidationService.retrieveMultipleObjectsFromStorage(addressesToGet);
        Assert.assertNotNull(hashResponseEntities.getEntitiesBulkResponses().get(hash1) );
        Assert.assertNotNull( hashResponseEntities.getEntitiesBulkResponses().get(hash2) );
        AddressData retrievedAddress1 = mapper.readValue(String.valueOf( hashResponseEntities.getEntitiesBulkResponses().get(hash1)), AddressData.class);
        AddressData retrievedAddress2 = mapper.readValue(String.valueOf( hashResponseEntities.getEntitiesBulkResponses().get(hash2)), AddressData.class);

        Assert.assertTrue( retrievedAddress1.equals(addressTxsHistories.get(1)) );
        Assert.assertTrue( retrievedAddress2.equals(addressTxsHistories.get(2)) );
    }


}