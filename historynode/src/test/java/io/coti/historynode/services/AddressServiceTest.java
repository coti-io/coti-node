//package io.coti.historynode.services;
//
//import io.coti.basenode.crypto.*;
//import io.coti.basenode.data.AddressData;
//import io.coti.basenode.data.Hash;
//import io.coti.basenode.data.SignatureData;
//import io.coti.basenode.database.interfaces.IDatabaseConnector;
//import io.coti.basenode.http.GetHistoryAddressesRequest;
//import io.coti.basenode.http.GetHistoryAddressesResponse;
//import io.coti.basenode.http.HttpJacksonSerializer;
//import io.coti.basenode.http.SerializableResponse;
//import io.coti.basenode.http.interfaces.IResponse;
//import io.coti.basenode.model.Addresses;
//import io.coti.basenode.model.RequestedAddressHashes;
//import io.coti.basenode.model.Transactions;
//import io.coti.basenode.services.BaseNodeValidationService;
//import io.coti.basenode.services.interfaces.IPotService;
//import io.coti.basenode.services.interfaces.ITransactionHelper;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//import utils.HashTestUtils;
//
//import java.util.*;
//
//import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
//import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
//import static io.coti.historynode.http.HttpStringConstants.STORAGE_INVALID_SIGNATURE;
//import static io.coti.historynode.http.HttpStringConstants.STORAGE_RESPONSE_VALIDATION_ERROR;
//import static org.mockito.Mockito.when;
//
//@ContextConfiguration(classes = {AddressService.class, BaseNodeValidationService.class,
//        CryptoHelper.class, NodeCryptoHelper.class, Transactions.class, HttpJacksonSerializer.class})
//@TestPropertySource(locations = "classpath:test.properties")
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class AddressServiceTest {
//
//    @Autowired
//    private AddressService addressService;
//    @MockBean
//    private StorageConnector storageConnector;
//    @MockBean
//    private Addresses addresses;
//    @MockBean
//    private RequestedAddressHashes requestedAddressHashes;
//    @MockBean
//    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
//    @MockBean
//    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
//    @Autowired
//    private BaseNodeValidationService validationService;
//
//    // Unused, Just for mocking
//    @MockBean
//    private ITransactionHelper transactionHelper;
//    @MockBean
//    private TransactionCrypto transactionCrypto;
//    @MockBean
//    private TransactionSenderCrypto transactionSenderCrypto;
//    @MockBean
//    private IPotService potService;
//    @MockBean
//    private IDatabaseConnector iDatabaseConnector;
//    //
//
//    @Value("${storage.server.address}")
//    private String storageNodeUrl;
//
//    private Hash hashInRocks;
//    private AddressData addressDataFromRocks;
//    private Hash hashInStorage;
//    private AddressData addressDataFromStorage;
//    private List<Hash> addressHashesFromFullNodeRequest;
//    private GetHistoryAddressesRequest getHistoryAddressesRequestFromFullNode;
//    private List<Hash> addressHashesToStorageHashList;
//    private GetHistoryAddressesRequest getHistoryAddressesRequestToStorageNode;
//
//    @Before
//    public void setUpBeforeEachTest() {
//        hashInRocks = HashTestUtils.generateRandomAddressHash();
//        addressDataFromRocks = new AddressData(hashInRocks);
//        addressDataFromStorage = new AddressData(hashInStorage);
//        when(addresses.getByHash(hashInRocks)).thenReturn(addressDataFromRocks);
//        hashInStorage = HashTestUtils.generateRandomAddressHash();
//        addressHashesFromFullNodeRequest = new ArrayList<>(Arrays.asList(hashInRocks, hashInStorage));
//        getHistoryAddressesRequestFromFullNode = new GetHistoryAddressesRequest(addressHashesFromFullNodeRequest);
//        setRequestDummySignerHashAndSignature(getHistoryAddressesRequestFromFullNode);
//
//        addressHashesToStorageHashList = new ArrayList<>(Collections.singletonList(hashInStorage));
//        getHistoryAddressesRequestToStorageNode = new GetHistoryAddressesRequest(addressHashesToStorageHashList);
//    }
//
//    @Test
//    public void getAddress_testUnsignedRequestFromFullNode_shouldFailInValidation() {
//        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(new ArrayList<>());
//        //create request without signer hash and signature
//        ResponseEntity<IResponse> expectedResponse = ResponseEntity
//                .status(HttpStatus.UNAUTHORIZED)
//                .body(new SerializableResponse(INVALID_SIGNATURE, STATUS_ERROR));
//        Assert.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));
//
//        //create request without signer hash
//        getHistoryAddressesRequest.setSignature(new SignatureData("a", "b"));
//        Assert.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));
//
//        //create request wit signer hash and signature but with bad signature
//        getHistoryAddressesRequest.setSignerHash(new Hash(1));
//        Assert.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));
//    }
//
//    @Test
//    public void getAddress_testUnsignedResponseFromStorage_shouldFailInValidation() {
//        Map<Hash, AddressData> responseMap = new HashMap<>();
//        responseMap.put(hashInStorage, addressDataFromStorage);
//        when(storageConnector.retrieveFromStorage(storageNodeUrl + "/addresses", getHistoryAddressesRequestToStorageNode, GetHistoryAddressesResponse.class)).
//                thenReturn(ResponseEntity.status(HttpStatus.OK).body(new GetHistoryAddressesResponse(responseMap)));
//
//        ResponseEntity<IResponse> responseFromGetAddresses = addressService.getAddresses(getHistoryAddressesRequestFromFullNode);
//        ResponseEntity<IResponse> expectedUnsignedResponseFromStorage = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SerializableResponse(STORAGE_INVALID_SIGNATURE, STATUS_ERROR));
//
//        Assert.assertEquals(expectedUnsignedResponseFromStorage.getStatusCode(), responseFromGetAddresses.getStatusCode());
//        Assert.assertEquals((expectedUnsignedResponseFromStorage.getBody()), (responseFromGetAddresses.getBody()));
//    }
//
//
//    @Test
//    public void getAddress_testResponseFromStorageHashKeyDoesntMatchHashInValue_shouldFailInValidation() {
//        Map<Hash, AddressData> responseMap = new HashMap<>();
//        responseMap.put(hashInStorage, addressDataFromRocks);
//        GetHistoryAddressesResponse responseFromStorage = new GetHistoryAddressesResponse(responseMap);
//        responseFromStorage.setSignature(new SignatureData("a", "b"));
//        responseFromStorage.setSignerHash(new Hash(1));
//
//        when(storageConnector.retrieveFromStorage(storageNodeUrl + "/addresses", getHistoryAddressesRequestToStorageNode, GetHistoryAddressesResponse.class)).
//                thenReturn(ResponseEntity.status(HttpStatus.OK).body(responseFromStorage));
//        when(getHistoryAddressesResponseCrypto.verifySignature(responseFromStorage)).thenReturn(true);
//
//        ResponseEntity<IResponse> responseFromGetAddresses = addressService.getAddresses(getHistoryAddressesRequestFromFullNode);
//        ResponseEntity<IResponse> expectedUnsignedResponseFromStorage = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(STORAGE_RESPONSE_VALIDATION_ERROR, STATUS_ERROR));
//
//        Assert.assertEquals(expectedUnsignedResponseFromStorage.getStatusCode(), responseFromGetAddresses.getStatusCode());
//        Assert.assertEquals((expectedUnsignedResponseFromStorage.getBody()), (responseFromGetAddresses.getBody()));
//    }
//
//    @Test
//    public void getAddress_testResponseFromStorageOrderNotCorrect_shouldReturnOrderedResponse() {
//        List<Hash> addressesHashFromStorage1 = HashTestUtils.generateListOfRandomAddressHashes(15);
//        List<Hash> addressesHashFromStorage2 = HashTestUtils.generateListOfRandomAddressHashes(15);
//        List<Hash> addressesHashFromStorage = new ArrayList<>();
//        addressesHashFromStorage.addAll(addressesHashFromStorage1);
//        addressesHashFromStorage.add(hashInRocks);
//        addressesHashFromStorage.addAll(addressesHashFromStorage2);
//
//        List<Hash> orderedList = new ArrayList<>();
//        orderedList.addAll(addressesHashFromStorage);
//
//
//        GetHistoryAddressesRequest getHistoryAddressesRequestFromFullNode = new GetHistoryAddressesRequest(addressesHashFromStorage);
//        setRequestDummySignerHashAndSignature(getHistoryAddressesRequestFromFullNode);
//
//        List<Hash> addressesDataFromHistoryList = new ArrayList<>();
//        addressesDataFromHistoryList.addAll(addressesHashFromStorage1);
//        addressesDataFromHistoryList.addAll(addressesHashFromStorage2);
//        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressesDataFromHistoryList);
//
//        Map<Hash, AddressData> responseMap = new LinkedHashMap<>();
//        addressesHashFromStorage2.stream().forEach(hash -> responseMap.put(hash, new AddressData(hash)));
//        addressesHashFromStorage1.stream().forEach(hash -> responseMap.put(hash, new AddressData(hash)));
//        GetHistoryAddressesResponse unOrderedResponseFromStorage = new GetHistoryAddressesResponse(responseMap);
//        setResponseDummySignerHashAndSignature(unOrderedResponseFromStorage);
//
//        when(storageConnector.retrieveFromStorage(storageNodeUrl + "/addresses", getHistoryAddressesRequest, GetHistoryAddressesResponse.class)).
//                thenReturn(ResponseEntity.status(HttpStatus.OK).body(unOrderedResponseFromStorage));
//
//        ResponseEntity<IResponse> responseFromGetAddresses = addressService.getAddresses(getHistoryAddressesRequestFromFullNode);
//
//        Map<Hash, AddressData> orderedExpectedResponseMap = new LinkedHashMap<>();
//        addressesHashFromStorage1.stream().forEach(hash -> orderedExpectedResponseMap.put(hash, new AddressData(hash)));
//        orderedExpectedResponseMap.put(hashInRocks, new AddressData(hashInRocks));
//        addressesHashFromStorage2.stream().forEach(hash -> orderedExpectedResponseMap.put(hash, new AddressData(hash)));
//        GetHistoryAddressesResponse expectedResponse = new GetHistoryAddressesResponse(orderedExpectedResponseMap);
//        ResponseEntity<IResponse> expectedOrderedResponseFromStorage = ResponseEntity.status(HttpStatus.OK).body(expectedResponse);
//
//        Iterator<Map.Entry<Hash, AddressData>> responseIterator = ((GetHistoryAddressesResponse) expectedOrderedResponseFromStorage.getBody()).getAddressHashesToAddresses().entrySet().iterator();
//        int i = 0;
//        while (responseIterator.hasNext()) {
//            Map.Entry<Hash, AddressData> entry = responseIterator.next();
//            Assert.assertEquals(orderedList.get(i), entry.getKey());
//            i++;
//        }
//        Assert.assertEquals(HttpStatus.OK, responseFromGetAddresses.getStatusCode());
//        Assert.assertEquals(expectedResponse.getStatus(), ((GetHistoryAddressesResponse) responseFromGetAddresses.getBody()).getStatus());
//    }
//
//    @Test
//    public void getAddresses_retrieveAddressNotInStorage() {
//        Hash addressHashInHistory = HashTestUtils.generateRandomAddressHash();
//        Hash addressHashNotFound = HashTestUtils.generateRandomAddressHash();
//        when(addresses.getByHash(addressHashInHistory)).thenReturn(new AddressData(addressHashInHistory));
//        when(requestedAddressHashes.getByHash(addressHashInHistory)).thenReturn(null);
//        retrieveAddressNotInStorage(addressHashInHistory, addressHashNotFound);
//
//
//    }
//
//    private void retrieveAddressNotInStorage(Hash addressHashInHistory, Hash addressHashNotFound) {
//
//        List<Hash> addressHashes = new ArrayList<>();
//        addressHashes.add(addressHashInHistory);
//        addressHashes.add(addressHashNotFound);
//
//        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashes);
//        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequest);
//
//        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(true);
//
//        Map<Hash, AddressData> addressHashesToAddresses = new HashMap<>();
//        addressHashesToAddresses.put(addressHashNotFound, null);
//        GetHistoryAddressesResponse getHistoryAddressesResponseFromStorageNode = new GetHistoryAddressesResponse(addressHashesToAddresses);
//
//        GetHistoryAddressesRequest getHistoryAddressesRequestToStorage = new GetHistoryAddressesRequest(Arrays.asList(addressHashNotFound));
//        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequestToStorage);
//        when(storageConnector.retrieveFromStorage(storageNodeUrl + "/addresses", getHistoryAddressesRequestToStorage, GetHistoryAddressesResponse.class)).thenReturn(ResponseEntity.status(HttpStatus.OK).body(getHistoryAddressesResponseFromStorageNode));
//        when(getHistoryAddressesResponseCrypto.verifySignature(getHistoryAddressesResponseFromStorageNode)).thenReturn(true);
//
//        GetHistoryAddressesResponse actualResponse = (GetHistoryAddressesResponse) addressService.getAddresses(getHistoryAddressesRequest).getBody();
//
//        LinkedHashMap<Hash, AddressData> expectedResponseMap = new LinkedHashMap<>();
//        expectedResponseMap.put(addressHashInHistory, new AddressData(addressHashInHistory));
//        expectedResponseMap.put(addressHashNotFound, null);
//
//        assertResponsesMapsEqual(expectedResponseMap, actualResponse.getAddressHashesToAddresses());
//    }
//
//    private void assertResponsesMapsEqual(Map<Hash, AddressData> expectedResponseMap, Map<Hash, AddressData> actualResponseMap) {
//        Assert.assertEquals(expectedResponseMap.size(), actualResponseMap.size());
//        Iterator expectedIterator = expectedResponseMap.entrySet().iterator();
//        Iterator actualIterator = actualResponseMap.entrySet().iterator();
//        while (expectedIterator.hasNext()) {
//            Map.Entry<Hash, AddressData> expectedEntry = (Map.Entry<Hash, AddressData>) expectedIterator.next();
//            Map.Entry<Hash, AddressData> actualEntry = (Map.Entry<Hash, AddressData>) actualIterator.next();
//            Assert.assertEquals(expectedEntry.getKey(), actualEntry.getKey());
//            Assert.assertEquals(expectedEntry.getKey(), actualEntry.getKey());
//            if (expectedEntry.getValue() == null) {
//                Assert.assertNull(actualEntry.getValue());
//            } else if (actualEntry.getValue() == null) {
//                Assert.assertNull(expectedEntry.getValue());
//            } else {
//                Assert.assertEquals(expectedEntry.getValue().getHash(), actualEntry.getValue().getHash());
//            }
//        }
//
//    }
//
//    private void setRequestDummySignerHashAndSignature(GetHistoryAddressesRequest getHistoryAddressesRequestFromFullNode) {
//        getHistoryAddressesRequestFromFullNode.setSignature(new SignatureData("a", "b"));
//        getHistoryAddressesRequestFromFullNode.setSignerHash(new Hash(1));
//        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequestFromFullNode)).thenReturn(true);
//    }
//
//    private void setResponseDummySignerHashAndSignature(GetHistoryAddressesResponse response) {
//        response.setSignature(new SignatureData("a", "b"));
//        response.setSignerHash(new Hash(1));
//        when(getHistoryAddressesResponseCrypto.verifySignature(response)).thenReturn(true);
//    }
//
//}
