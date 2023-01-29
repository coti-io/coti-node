package io.coti.fullnode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.AddressBulkRequest;
import io.coti.basenode.http.AddressesExistsResponse;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.RequestedAddressHashes;
import io.coti.basenode.services.BaseNodeIdentityService;
import io.coti.basenode.services.BaseNodeSecretManagerService;
import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.basenode.services.FileService;
import io.coti.basenode.services.interfaces.ISecretManagerService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.database.RocksDBConnector;
import io.coti.fullnode.websocket.WebSocketSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import utils.AddressTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressService.class, IDatabaseConnector.class,
        HttpJacksonSerializer.class, GetHistoryAddressesRequestCrypto.class, CryptoHelper.class,
        GetHistoryAddressesResponseCrypto.class, IDatabaseConnector.class, RocksDBConnector.class, AnnotationConfigContextLoader.class,
        BaseNodeIdentityService.class, ISecretManagerService.class, BaseNodeSecretManagerService.class
})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AddressServiceIntegrationTest {

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // need to add a tests in Storage node and in history node that will prepare for tests in this class.
    //
    //Temporary workaround for setting up the tests. clear collections before running the tests:
    //
    //@Autowired
    //private Addresses addresses;
    //
    //add this in the history controller:
    //    @RequestMapping(value = "/prepareintegration", method = GET)
    //    public boolean getAddresses() {
    //        addresses.put(new AddressData(new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51881")));
    //        addresses.put(new AddressData(new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51884")));
    //        return true;
    //    }
    //
    //---------------------------
    //add this in the storage node address controller:
    //
    //@Autowired
    //private AddressStorageService addressStorageService;
    //@Autowired
    //private JacksonSerializer jacksonSerializer;
    //
    //@RequestMapping(value = "/prepareintegration", method = GET)
    //public boolean getAddresses() {
    //    Hash hash1 = new Hash("b3c1ce55ef49d7ec0dc8555a9b8e0fc309905a5d2e318ec02c574a4dcee81aa529c7ee42bc06c17f2ccf17ecbfcbf4cd4cb7420435ff593c7efe2cad9439c207ee1b6290");
    //    Hash hash2 = new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51885");
    //    Map<Hash, String> hashToAddressDataJsonMap = new HashMap<>();
    //    hashToAddressDataJsonMap.put(hash1,jacksonSerializer.serializeAsString(new AddressData(hash1)));
    //    hashToAddressDataJsonMap.put(hash2,jacksonSerializer.serializeAsString(new AddressData(hash2)));
    //    addressStorageService.storeMultipleObjectsToStorage(hashToAddressDataJsonMap);
    //    return true;
    //}
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    private final String HISTORY_PORT = "7031";

    @Autowired
    private AddressService addressService;
    @Autowired
    private BaseNodeIdentityService nodeIdentityService;
    @Autowired
    ISecretManagerService secretManagerService;
    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCryptoLocal;
    @MockBean
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @MockBean
    private NetworkService networkServiceLocal;
    @MockBean
    private Addresses addressesLocal;
    @MockBean
    private RequestedAddressHashes requestedAddressHashesLocal;

    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private IValidationService validationService;
    @MockBean
    private FileService fileService;

    private AddressData addressInLocalAddressesCollection = AddressTestUtils.generateRandomAddressData();

    private RequestedAddressHashData addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory = new RequestedAddressHashData(
            new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51881")); // todo add to history to addresses collection

    private RequestedAddressHashData addressInRequestedAddressesCollectionMoreThenTenMinutesInStorage = new RequestedAddressHashData(
            new Hash("b3c1ce55ef49d7ec0dc8555a9b8e0fc309905a5d2e318ec02c574a4dcee81aa529c7ee42bc06c17f2ccf17ecbfcbf4cd4cb7420435ff593c7efe2cad9439c207ee1b6290")); // todo add to elasticsearch

    private RequestedAddressHashData addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound = new RequestedAddressHashData(
            new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51883"));

    private RequestedAddressHashData addressNotFoundInFullNodeAndFoundInHistory = new RequestedAddressHashData(
            new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51884"));  // todo add to history to addresses collection

    private RequestedAddressHashData addressNotFoundInFullNodeAndFoundInStorage = new RequestedAddressHashData(
            new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51885")); // todo add to elasticsearch

    private RequestedAddressHashData addressNotFoundInFullNodeAndNotFound = new RequestedAddressHashData(
            new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51886"));


    @BeforeEach
    void setUp() {
        BaseNodeServiceManager.secretManagerService = secretManagerService;
        nodeIdentityService.init();
        networkService = networkServiceLocal;
        addresses = addressesLocal;
        requestedAddressHashes = requestedAddressHashesLocal;
        getHistoryAddressesRequestCrypto = getHistoryAddressesRequestCryptoLocal;

        when(addresses.getByHash(addressInLocalAddressesCollection.getHash())).thenReturn(addressInLocalAddressesCollection);
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            ((ISignable) arg0).setSignerHash(new Hash("794b3ee5e769179dc9df079d674506bdd6f9ea05aca27858e235868933235a71b9a156b72a563b5b36ca8912d8ddbcee7aad7e7353087d7caa950a1ee545748e"));
            GetHistoryAddressesRequestCrypto historyAddressesRequestCrypto = new GetHistoryAddressesRequestCrypto();
            ((ISignable) arg0).setSignature(CryptoHelper.signBytes((historyAddressesRequestCrypto.getSignatureMessage((GetHistoryAddressesRequest) arg0)), "e1c307364685ef6126de71b079aef12a00dfa4504afa4ea2714b1281f9d6b461"));
            return null;
        }).when(getHistoryAddressesRequestCrypto).signMessage(any(GetHistoryAddressesRequest.class));
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory, 660_000);
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesInStorage, 660_000);
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound, 660_000);
        mockNotFound(addressNotFoundInFullNodeAndFoundInHistory);
        mockNotFound(addressNotFoundInFullNodeAndFoundInStorage);
        mockNotFound(addressNotFoundInFullNodeAndNotFound);
        mockNetworkService();
    }

    private void setTimeAndMock(RequestedAddressHashData requestedAddressHashData, long insertionTime) {
        requestedAddressHashData.setLastUpdateTime(Instant.now().minusMillis(insertionTime));
        when(requestedAddressHashes.getByHash(requestedAddressHashData.getHash())).thenReturn(requestedAddressHashData);
    }

    private void mockNotFound(RequestedAddressHashData requestedAddressHashData) {
        when(requestedAddressHashes.getByHash(requestedAddressHashData.getHash())).thenReturn(null);
    }

    /**
     * Get 2 addresses. one in address collection and the other in local RequestedAddress collection, inserted more then 10 minutes ago.
     * the second address is found in history node in addresses collection.
     * the address is expected to be returned by history node and the response will be TRUE, TRUE(Ordered).
     */
//    @Test
    public void addressesExist_addressInLocalDbAndUpdatedFromHistoryMoreThenTenMinutesInHistory_shouldGetResponseFromHistoryNode_IT() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory.getHash());
        AddressesExistsResponse response = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse, response));
    }

    /**
     * Get 2 addresses. one in address collection and the other in local RequestedAddress collection, inserted more then 10 minutes ago.
     * the second address is not found in history node but is found in storage node.
     * the address is expected to be returned by history node and the response will be TRUE, TRUE(Ordered).
     */
//    @Test
    public void addressesExist_addressInLocalDbAndUpdatedFromHistoryMoreThenTenMinutesInStorage_shouldGetResponseFromHistoryNode_IT() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressInRequestedAddressesCollectionMoreThenTenMinutesInStorage.getHash());
        AddressesExistsResponse response = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesInStorage.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse, response));
    }

    /**
     * Get 2 addresses. one in address collection and the other in local RequestedAddress collection, inserted more then 10 minutes ago.
     * the second address is not found in history node or in storage node.
     * the address is expected to be returned by history node and the response will be TRUE, FALSE(Ordered).
     * Note that first time it will be retrieved from storage and added to the RequestedAddresses collection in History node.
     * The second time will be retrieved from history node RequestedAddresses collection.
     */
//    @Test
    public void addressesExist_addressInLocalDbAndUpdatedFromHistoryMoreThenTenMinutesNotFound_shouldGetResponseFromHistoryNode_IT() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound.getHash());
        AddressesExistsResponse response = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponseFirstTime = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponseFirstTime, response));

        AddressesExistsResponse expectedResponseSecondTime = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponseSecondTime, response));
    }

    /**
     * Get 2 addresses. one in address collection and the other is not found in local RequestedAddress collection.
     * the second address is found in history node in addresses collection.
     * the address is expected to be returned by history node and the response will be TRUE, TRUE(Ordered).
     */
//    @Test
    public void addressesExist_addressInLocalDbAndInHistory_shouldGetResponseFromHistoryNode_IT() {
        //Prepare History for this integration test
        AddressBulkRequest prepareForIntegrationRequest = AddressTestUtils.generateAddressBulkRequest(
                addressNotFoundInFullNodeAndFoundInHistory.getHash());
        addressService.addressesCheckExistenceAndRequestHistoryNode(prepareForIntegrationRequest);

        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressNotFoundInFullNodeAndFoundInHistory.getHash());
        AddressesExistsResponse response = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndFoundInHistory.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse, response));
    }

    /**
     * Get 2 addresses. one in address collection and the other is not found in RequestedAddress collection.
     * the second address is not found in history node and is found in storage node.
     * the address is expected to be returned by history node and the response will be TRUE, TRUE(Ordered).
     */
//    @Test
    public void addressesExist_addressInLocalDbAndInStorage_shouldGetResponseFromStorageNode_IT() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressNotFoundInFullNodeAndFoundInStorage.getHash());
        AddressesExistsResponse response = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndFoundInStorage.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse, response));
    }

    /**
     * Get 2 addresses. one in address collection and the other is not found in RequestedAddress collection.
     * the second address is not found in history node and is not found in storage node.
     * the address is expected to be returned by history node and the response will be TRUE, FALSE(Ordered).
     */
    @Test
    void addressesExist_addressInLocalDbAndNotFound_shouldGetResponseFromStorageNode_IT() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressNotFoundInFullNodeAndNotFound.getHash());
        AddressesExistsResponse response = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponse1 = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse1, response));

        AddressesExistsResponse expectedResponse2 = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse2, response));
    }

    private void mockNetworkService() {
        NetworkNodeData networkNodeData = new NetworkNodeData();
        networkNodeData.setAddress("localhost");
        networkNodeData.setHttpPort(HISTORY_PORT);
        Map<Hash, NetworkNodeData> networkNodeDataMap = new HashMap<>();
        networkNodeDataMap.put(new Hash("aaaa"), networkNodeData);

        when(networkService.getMapFromFactory(NodeType.HistoryNode)).thenReturn(networkNodeDataMap);
    }

}
