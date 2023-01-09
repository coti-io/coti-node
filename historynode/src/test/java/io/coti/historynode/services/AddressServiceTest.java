package io.coti.historynode.services;

import io.coti.basenode.config.NodeConfig;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.RequestedAddressHashes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.basenode.services.FileService;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import utils.HashTestUtils;
import utils.TransactionTestUtils;

import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {AddressService.class, BaseNodeValidationService.class, NodeConfig.class, CryptoHelper.class, NodeCryptoHelper.class, Transactions.class, HttpJacksonSerializer.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AddressServiceTest {

    @Autowired
    private AddressService addressService;
    @MockBean
    private StorageConnector storageConnector;
    @MockBean
    private Addresses addresses;
    @MockBean
    private RequestedAddressHashes requestedAddressHashes;
    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    @MockBean
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @MockBean
    private BaseNodeValidationService validationService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private TransactionSenderCrypto transactionSenderCrypto;
    @MockBean
    private IPotService potService;
    @MockBean
    private IDatabaseConnector iDatabaseConnector;
    @MockBean
    private FileService fileService;
    private Hash hashInRocks;
    private AddressData addressDataFromRocks;
    private Hash hashInStorage;
    private AddressData addressDataFromStorage;
    private List<Hash> addressHashesFromFullNodeRequest;
    private GetHistoryAddressesRequest getHistoryAddressesRequestFromFullNode;
    private List<Hash> addressHashesToStorageHashList;
    private GetHistoryAddressesRequest getHistoryAddressesRequestToStorageNode;

    @BeforeEach
    public void setUpBeforeEachTest() {
        hashInRocks = HashTestUtils.generateRandomAddressHash();
        addressDataFromRocks = new AddressData(hashInRocks);
        addressDataFromStorage = new AddressData(hashInStorage);
        when(addresses.getByHash(hashInRocks)).thenReturn(addressDataFromRocks);
        hashInStorage = HashTestUtils.generateRandomAddressHash();
        addressHashesFromFullNodeRequest = new ArrayList<>(Arrays.asList(hashInRocks, hashInStorage));
        getHistoryAddressesRequestFromFullNode = new GetHistoryAddressesRequest(addressHashesFromFullNodeRequest);
        setRequestDummySignerHashAndSignature(getHistoryAddressesRequestFromFullNode);

        addressHashesToStorageHashList = new ArrayList<>(Collections.singletonList(hashInStorage));
        getHistoryAddressesRequestToStorageNode = new GetHistoryAddressesRequest(addressHashesToStorageHashList);
    }

    @Test
    void getAddress_testUnsignedRequestFromFullNode_shouldFailInValidation() {
        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(new ArrayList<>());
        //create request without signer hash and signature
        ResponseEntity<IResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SerializableResponse(INVALID_SIGNATURE, STATUS_ERROR));
        Assertions.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));

        //create request without signer hash
        getHistoryAddressesRequest.setSignature(new SignatureData("a", "b"));
        Assertions.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));

        //create request wit signer hash and signature but with bad signature
        getHistoryAddressesRequest.setSignerHash(new Hash(1));
        Assertions.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));
    }

    @Test
    void getAddress_testUnsignedResponseFromStorage_shouldFailInValidation() {
        Map<Hash, AddressData> responseMap = new HashMap<>();
        responseMap.put(hashInStorage, addressDataFromStorage);
        GetHistoryAddressesResponse response = new GetHistoryAddressesResponse(responseMap);
        response.setSignature(new SignatureData("b", "a"));
        response.setSignerHash(TransactionTestUtils.generateRandomHash());
        when(storageConnector.retrieveFromStorage(any(String.class), any(GetHistoryAddressesRequest.class), any(Class.class))).thenReturn(ResponseEntity.status(HttpStatus.OK).body(response));
        doCallRealMethod().when(getHistoryAddressesResponseCrypto).verifySignature(any(GetHistoryAddressesResponse.class));
        doCallRealMethod().when(getHistoryAddressesResponseCrypto).getSignatureMessage(any(GetHistoryAddressesResponse.class));
        doCallRealMethod().when(getHistoryAddressesResponseCrypto).getSignature(any(GetHistoryAddressesResponse.class));
        doCallRealMethod().when(getHistoryAddressesResponseCrypto).getSignerHash(any(GetHistoryAddressesResponse.class));

        verify(validationService, never()).validateGetAddressesResponse(any(GetHistoryAddressesResponse.class));
    }

    @Test
    void getAddress_testResponseFromStorageOrderNotCorrect_shouldReturnOrderedResponse() {
        List<Hash> addressesHashFromStorage1 = HashTestUtils.generateListOfRandomAddressHashes(15);
        List<Hash> addressesHashFromStorage2 = HashTestUtils.generateListOfRandomAddressHashes(15);
        List<Hash> addressesHashFromStorage = new ArrayList<>();
        addressesHashFromStorage.addAll(addressesHashFromStorage1);
        addressesHashFromStorage.add(hashInRocks);
        addressesHashFromStorage.addAll(addressesHashFromStorage2);

        List<Hash> orderedList = new ArrayList<>();
        orderedList.addAll(addressesHashFromStorage);


        GetHistoryAddressesRequest getHistoryAddressesRequestFromFullNode = new GetHistoryAddressesRequest(addressesHashFromStorage);
        setRequestDummySignerHashAndSignature(getHistoryAddressesRequestFromFullNode);

        List<Hash> addressesDataFromHistoryList = new ArrayList<>();
        addressesDataFromHistoryList.addAll(addressesHashFromStorage1);
        addressesDataFromHistoryList.addAll(addressesHashFromStorage2);

        Map<Hash, AddressData> responseMap = new LinkedHashMap<>();
        addressesHashFromStorage2.stream().forEach(hash -> responseMap.put(hash, new AddressData(hash)));
        addressesHashFromStorage1.stream().forEach(hash -> responseMap.put(hash, new AddressData(hash)));
        GetHistoryAddressesResponse unOrderedResponseFromStorage = new GetHistoryAddressesResponse(responseMap);
        setResponseDummySignerHashAndSignature(unOrderedResponseFromStorage);

        when(storageConnector.retrieveFromStorage(any(String.class), any(GetHistoryAddressesRequest.class), any(Class.class))).thenReturn(ResponseEntity.status(HttpStatus.OK).body(unOrderedResponseFromStorage));

        ResponseEntity<IResponse> responseFromGetAddresses = addressService.getAddresses(getHistoryAddressesRequestFromFullNode);

        Map<Hash, AddressData> orderedExpectedResponseMap = new LinkedHashMap<>();
        addressesHashFromStorage1.stream().forEach(hash -> orderedExpectedResponseMap.put(hash, new AddressData(hash)));
        orderedExpectedResponseMap.put(hashInRocks, new AddressData(hashInRocks));
        addressesHashFromStorage2.stream().forEach(hash -> orderedExpectedResponseMap.put(hash, new AddressData(hash)));
        GetHistoryAddressesResponse expectedResponse = new GetHistoryAddressesResponse(orderedExpectedResponseMap);
        ResponseEntity<IResponse> expectedOrderedResponseFromStorage = ResponseEntity.status(HttpStatus.OK).body(expectedResponse);

        Iterator<Map.Entry<Hash, AddressData>> responseIterator = ((GetHistoryAddressesResponse) expectedOrderedResponseFromStorage.getBody()).getAddressHashesToAddresses().entrySet().iterator();
        int i = 0;
        while (responseIterator.hasNext()) {
            Map.Entry<Hash, AddressData> entry = responseIterator.next();
            Assertions.assertEquals(orderedList.get(i), entry.getKey());
            i++;
        }
        Assertions.assertEquals(HttpStatus.OK, responseFromGetAddresses.getStatusCode());
        Assertions.assertEquals(expectedResponse.getStatus(), ((GetHistoryAddressesResponse) responseFromGetAddresses.getBody()).getStatus());
    }

    @Test
    void getAddresses_retrieveAddressNotInStorage() {
        Hash addressHashInHistory = HashTestUtils.generateRandomAddressHash();
        Hash addressHashNotFound = HashTestUtils.generateRandomAddressHash();
        when(addresses.getByHash(any(Hash.class))).thenReturn(new AddressData(addressHashInHistory)).thenReturn(null);
        when(requestedAddressHashes.getByHash(any(Hash.class))).thenReturn(null);
        retrieveAddressNotInStorage(addressHashInHistory, addressHashNotFound);
    }

    private void retrieveAddressNotInStorage(Hash addressHashInHistory, Hash addressHashNotFound) {

        List<Hash> addressHashes = new ArrayList<>();
        addressHashes.add(addressHashInHistory);
        addressHashes.add(addressHashNotFound);

        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest(addressHashes);
        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequest);

        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequest)).thenReturn(true);

        Map<Hash, AddressData> addressHashesToAddresses = new HashMap<>();
        addressHashesToAddresses.put(addressHashNotFound, null);
        GetHistoryAddressesResponse getHistoryAddressesResponseFromStorageNode = new GetHistoryAddressesResponse(addressHashesToAddresses);

        GetHistoryAddressesRequest getHistoryAddressesRequestToStorage = new GetHistoryAddressesRequest(Arrays.asList(addressHashNotFound));
        getHistoryAddressesRequestCrypto.signMessage(getHistoryAddressesRequestToStorage);
        when(storageConnector.retrieveFromStorage(any(String.class), any(GetHistoryAddressesRequest.class), any(Class.class))).thenReturn(ResponseEntity.status(HttpStatus.OK).body(getHistoryAddressesResponseFromStorageNode));
        when(getHistoryAddressesResponseCrypto.verifySignature(any(GetHistoryAddressesResponse.class))).thenReturn(true);
        when(validationService.validateGetAddressesResponse(any(GetHistoryAddressesResponse.class))).thenReturn(true);
        GetHistoryAddressesResponse actualResponse = (GetHistoryAddressesResponse) addressService.getAddresses(getHistoryAddressesRequest).getBody();

        LinkedHashMap<Hash, AddressData> expectedResponseMap = new LinkedHashMap<>();
        expectedResponseMap.put(addressHashInHistory, new AddressData(addressHashInHistory));
        expectedResponseMap.put(addressHashNotFound, null);

        assertResponsesMapsEqual(expectedResponseMap, actualResponse.getAddressHashesToAddresses());
    }

    private void assertResponsesMapsEqual(Map<Hash, AddressData> expectedResponseMap, Map<Hash, AddressData> actualResponseMap) {
        Assertions.assertEquals(expectedResponseMap.size(), actualResponseMap.size());
        Iterator expectedIterator = expectedResponseMap.entrySet().iterator();
        Iterator actualIterator = actualResponseMap.entrySet().iterator();
        while (expectedIterator.hasNext()) {
            Map.Entry<Hash, AddressData> expectedEntry = (Map.Entry<Hash, AddressData>) expectedIterator.next();
            Map.Entry<Hash, AddressData> actualEntry = (Map.Entry<Hash, AddressData>) actualIterator.next();
            Assertions.assertEquals(expectedEntry.getKey(), actualEntry.getKey());
            Assertions.assertEquals(expectedEntry.getKey(), actualEntry.getKey());
            if (expectedEntry.getValue() == null) {
                Assertions.assertNull(actualEntry.getValue());
            } else if (actualEntry.getValue() == null) {
                Assertions.assertNull(expectedEntry.getValue());
            } else {
                Assertions.assertEquals(expectedEntry.getValue().getHash(), actualEntry.getValue().getHash());
            }
        }

    }

    private void setRequestDummySignerHashAndSignature(GetHistoryAddressesRequest getHistoryAddressesRequestFromFullNode) {
        getHistoryAddressesRequestFromFullNode.setSignature(new SignatureData("a", "b"));
        getHistoryAddressesRequestFromFullNode.setSignerHash(new Hash(1));
        when(getHistoryAddressesRequestCrypto.verifySignature(getHistoryAddressesRequestFromFullNode)).thenReturn(true);
    }

    private void setResponseDummySignerHashAndSignature(GetHistoryAddressesResponse response) {
        response.setSignature(new SignatureData("a", "b"));
        response.setSignerHash(new Hash(1));
        when(getHistoryAddressesResponseCrypto.verifySignature(response)).thenReturn(true);
    }

}
