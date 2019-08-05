package io.coti.storagenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.AddressTestUtils;
import testUtils.HashTestUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressStorageService.class, ObjectService.class, DbConnectorService.class,
        JacksonSerializer.class, GetHistoryAddressesResponseCrypto.class, GetHistoryAddressesRequestCrypto.class, BaseNodeValidationService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressStorageServiceTest {

    private final int NUMBER_OF_ADDRESSES = 4;

    @Autowired
    private AddressStorageService addressStorageService;
    @Autowired
    private BaseNodeValidationService validationService;
    @MockBean
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    @Autowired
    protected JacksonSerializer jacksonSerializer;
    @MockBean
    private ObjectService objectService;
    @MockBean
    private DbConnectorService dbConnectorService;
    //
    @MockBean
    private NodeCryptoHelper nodeCryptoHelper;

    @MockBean
    private Transactions transactions;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private TransactionSenderCrypto transactionSenderCrypto;
    @MockBean
    private IPotService potService;
    //

    @Before
    public void init() {

    }

    AddressData addressDataInStorage1 = AddressTestUtils.generateRandomAddressData();
    AddressData addressDataInStorage2 = AddressTestUtils.generateRandomAddressData();

    AddressData addressDataNotInStorage1 = AddressTestUtils.generateRandomAddressData();
    AddressData addressDataNotInStorage2 = AddressTestUtils.generateRandomAddressData();

    /**
     * Test request received with bad signature.
     */
    @Test
    public void retrieveMultipleObjectsFromStorage_badSignature_errorReturned() {
        GetHistoryAddressesRequest getHistoryAddressesRequest = AddressTestUtils.generateGetHistoryAddressesRequest(
                null, null,
                addressDataInStorage1);

        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(false);

        ResponseEntity<IResponse> actualResponse = addressStorageService.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest);
        SerializableResponse expectedResponse = new SerializableResponse(INVALID_SIGNATURE, STATUS_ERROR);

        Assert.assertEquals(expectedResponse, actualResponse.getBody());
    }

    /**
     * retrieve 2 address from storage.
     * both address are found in elastic and will be returned.
     */
    @Test
    public void retrieveMultipleObjectsFromStorage_addressesInStorage_returnsAddresses() {
        GetHistoryAddressesRequest getHistoryAddressesRequest = AddressTestUtils.generateGetHistoryAddressesRequest(
                null, null,
                addressDataInStorage1,
                addressDataInStorage2);
        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(true);

        mockGetMultiObjectsFromDb(true, ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.TRUE, Boolean.TRUE),
                addressDataInStorage1,
                addressDataInStorage2);

        mockGetMultiObjectsFromDb(false, ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.TRUE, Boolean.TRUE),
                addressDataInStorage1,
                addressDataInStorage2);

        ResponseEntity<IResponse> actualResponse = addressStorageService.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest);
        ResponseEntity<IResponse> expectedResponse = generateExpectedResponse(generateInStorageList(Boolean.TRUE, Boolean.TRUE),
                addressDataInStorage1, addressDataInStorage2);
        checkResponseEquality(expectedResponse, actualResponse);
    }

    /**
     * retrieve 2 address from storage.
     * both address are not found in elastic and null will be returned.
     */
    @Test
    public void retrieveMultipleObjectsFromStorage_addressesNotInStorage_returnsNulls() {
        GetHistoryAddressesRequest getHistoryAddressesRequest = AddressTestUtils.generateGetHistoryAddressesRequest(
                null, null,
                addressDataNotInStorage1,
                addressDataNotInStorage2);
        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(true);

        mockGetMultiObjectsFromDb(true, ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.FALSE, Boolean.FALSE),
                addressDataNotInStorage1,
                addressDataNotInStorage2);

        mockGetMultiObjectsFromDb(false, ElasticSearchData.ADDRESSES,
                generateInStorageList(Boolean.FALSE, Boolean.FALSE),
                addressDataNotInStorage1,
                addressDataNotInStorage2);

        ResponseEntity<IResponse> actualResponse = addressStorageService.retrieveMultipleObjectsFromStorage(getHistoryAddressesRequest);
        ResponseEntity<IResponse> expectedResponse = generateExpectedResponse(generateInStorageList(Boolean.FALSE, Boolean.FALSE),
                addressDataNotInStorage1, addressDataNotInStorage2);
        checkResponseEquality(expectedResponse, actualResponse);
    }


   // @Test
    public void insertAndGetAddressTest() {
        AddressData addressData1 = new AddressData(HashTestUtils.generateRandomAddressHash());
        String addressAsJson = jacksonSerializer.serializeAsString(addressData1);
        RestStatus insertRestStatus = objectService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(insertRestStatus.equals(RestStatus.CREATED));
        String returnedAddressAsJson = objectService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(addressAsJson.equals(returnedAddressAsJson));
    }


    //@Test
    public void addressTest() {
        AddressData addressData1 = new AddressData(HashTestUtils.generateRandomAddressHash());
        AddressData addressData2 = new AddressData(HashTestUtils.generateRandomAddressHash());

        String addressAsJson = jacksonSerializer.serializeAsString(addressData1);
        Assert.assertTrue(addressData1.equals(jacksonSerializer.deserialize(addressAsJson)));

        RestStatus insertRestStatus1 = objectService.insertObjectJson(addressData1.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(insertRestStatus1.equals(RestStatus.CREATED));
        RestStatus insertRestStatus2 = objectService.insertObjectJson(addressData2.getHash(), addressAsJson, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(insertRestStatus2.equals(RestStatus.CREATED));

        RestStatus deleteRestStatus = objectService.deleteObjectByHash(addressData2.getHash(), true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(deleteRestStatus.equals(RestStatus.OK));

        String returnedAddressAsJson = objectService.getObjectByHash(addressData1.getHash(), true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(addressData1.equals(jacksonSerializer.deserialize(returnedAddressAsJson)));
    }

    //@Test
    public void multiAddressTest() {
        List<AddressData> AddressDataList = new ArrayList<>();
        Map<Hash, String> hashToAddressJsonDataMap = new HashMap<>();

        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressData addressData = new AddressData(HashTestUtils.generateRandomAddressHash());
            AddressDataList.add(addressData);
            hashToAddressJsonDataMap.put(addressData.getHash(), jacksonSerializer.serializeAsString(addressData));
        }
        Map<Hash, RestStatus> hashToRestStatusInsertResponseMap = objectService.insertMultiObjects(hashToAddressJsonDataMap, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(hashToRestStatusInsertResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.CREATED)));

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(AddressDataList.get(0).getHash());
        deleteHashes.add(AddressDataList.get(1).getHash());

        Map<Hash, RestStatus> hashToRestStatusDeleteResponseMap = objectService.deleteMultiObjectsFromDb(deleteHashes, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(hashToRestStatusDeleteResponseMap.values().stream().allMatch(entry -> entry.equals(RestStatus.OK)));

        List<Hash> getHashes = new ArrayList<>();
        getHashes.add(AddressDataList.get(2).getHash());
        getHashes.add(AddressDataList.get(3).getHash());

        Map<Hash, String> hashToRestStatusGetResponseMap = objectService.getMultiObjectsFromDb(getHashes, true, ElasticSearchData.ADDRESSES);
        Assert.assertTrue(hashToRestStatusGetResponseMap.size() == getHashes.size());
        Assert.assertNotNull(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(0))));
        Assert.assertTrue(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(0))).equals(AddressDataList.get(2)));
        Assert.assertTrue(jacksonSerializer.deserialize(hashToRestStatusGetResponseMap.get(getHashes.get(1))).equals(AddressDataList.get(3)));
    }

    private ResponseEntity<IResponse> generateExpectedResponse(List<Boolean> inStorage, AddressData... addresses) {
        Map<Hash, AddressData> hashToAddressDataMap = new HashMap<>();
        AtomicInteger index = new AtomicInteger(0);
        Arrays.stream(addresses).forEach(addressData -> hashToAddressDataMap.put(addressData.getHash(), inStorage.get(index.getAndIncrement()) == Boolean.TRUE ? addressData : null));
        GetHistoryAddressesResponse getHistoryAddressesResponse = new GetHistoryAddressesResponse(hashToAddressDataMap);
        getHistoryAddressesResponseCrypto.signMessage(getHistoryAddressesResponse);

        return ResponseEntity.ok(getHistoryAddressesResponse);
    }

    private void checkResponseEquality(ResponseEntity<IResponse> expectedResponse, ResponseEntity<IResponse> actualResponse) {
        Assert.assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());

        Map<Hash, AddressData> expectedAddressHashesToAddresses = ((GetHistoryAddressesResponse) expectedResponse.getBody()).getAddressHashesToAddresses();
        Map<Hash, AddressData> actualAddressHashesToAddresses = ((GetHistoryAddressesResponse) actualResponse.getBody()).getAddressHashesToAddresses();
        Assert.assertEquals(expectedAddressHashesToAddresses.size(), actualAddressHashesToAddresses.size());

        Iterator iterExpected = expectedAddressHashesToAddresses.entrySet().iterator();
        Iterator iterActual = actualAddressHashesToAddresses.entrySet().iterator();
        while (iterExpected.hasNext()) {
            Map.Entry<Hash, AddressData> expectedEntry = (Map.Entry<Hash, AddressData>) iterExpected.next();
            Map.Entry<Hash, AddressData> actualEntry = (Map.Entry<Hash, AddressData>) iterActual.next();

            Assert.assertEquals(expectedEntry.getValue(), actualEntry.getValue());
            Assert.assertEquals(expectedEntry.getKey(), actualEntry.getKey());
        }
    }

    private List<Boolean> generateInStorageList(Boolean... inStorageList) {
        List<Boolean> inStorageListReturn = new ArrayList<>();
        Arrays.stream(inStorageList).forEach(bool -> inStorageListReturn.add(bool));
        return inStorageListReturn;

    }

    private void mockGetMultiObjectsFromDb(boolean fromColdStorage, ElasticSearchData objectType, List<Boolean> inStorage, AddressData... addresses) {
        List<Hash> hashes = new ArrayList<>();
        Map<Hash, String> objectsFromDBMap = new HashMap<>();
        AtomicInteger index = new AtomicInteger(0);
        Arrays.stream(addresses).forEach(addressData -> {

            hashes.add(addressData.getHash());
            objectsFromDBMap.put(addressData.getHash(), inStorage.get(index.getAndIncrement()) == Boolean.TRUE ? jacksonSerializer.serializeAsString(addressData) : null);
        });
        when(objectService.getMultiObjectsFromDb(hashes, false, objectType)).thenReturn(objectsFromDBMap);
    }

}