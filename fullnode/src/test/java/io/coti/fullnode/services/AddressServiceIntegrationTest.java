package io.coti.fullnode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.RequestedAddressHashes;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.database.RocksDBConnector;
import io.coti.fullnode.http.AddressBulkRequest;
import io.coti.fullnode.http.AddressesExistsResponse;
import io.coti.fullnode.websocket.WebSocketSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import utils.AddressTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressService.class, IDatabaseConnector.class,
        HttpJacksonSerializer.class, GetHistoryAddressesRequestCrypto.class, CryptoHelper.class, NodeCryptoHelper.class,
        GetHistoryAddressesResponseCrypto.class, IDatabaseConnector.class, RocksDBConnector.class,
        /*IPropagationPublisher.class, ZeroMQPropagationPublisher.class, ISerializer.class, JacksonSerializer.class, CommunicationService.class,
        IReceiver.class*/})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceIntegrationTest {

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
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    /*
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ISerializer serializer;
    @Autowired
    private IReceiver iReceiver;
    @Autowired
    private CommunicationService communicationService;
    */
    @MockBean
    private NetworkService networkService;

    @MockBean
    private Addresses addresses;
    @MockBean
    private RequestedAddressHashes requestedAddressHashes;
//    @Autowired
//    public IDatabaseConnector databaseConnector;
//    @Autowired
//    private RocksDBConnector rocksDBConnector;

    // Unused but required for compilation
    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private IValidationService validationService;
    //

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

    //TODO 8/4/2019 astolia: change to @BeforeClass
    @Before
    public void setUp() {
//        rocksDBConnector.setColumnFamily();
//        databaseConnector.init();

        addresses.put(AddressTestUtils.generateRandomAddressData());

        when(addresses.getByHash(addressInLocalAddressesCollection.getHash())).thenReturn(addressInLocalAddressesCollection);
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory, 660_000);
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesInStorage, 660_000);
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound, 660_000);
        mockNotFound(addressNotFoundInFullNodeAndFoundInHistory);
        mockNotFound(addressNotFoundInFullNodeAndFoundInStorage);
        mockNotFound(addressNotFoundInFullNodeAndNotFound);
        mockNetworkService();
        //TODO 8/1/2019 astolia: todo mock crypto?

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
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse, response));
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
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesInStorage.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse, response));
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
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponseFirstTime = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponseFirstTime, response));

        AddressesExistsResponse expectedResponseSecondTime = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponseSecondTime, response));
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
        addressService.addressesExist(prepareForIntegrationRequest);

        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressNotFoundInFullNodeAndFoundInHistory.getHash());
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndFoundInHistory.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse, response));
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
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndFoundInStorage.getHash()),
                Boolean.TRUE,
                Boolean.TRUE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse, response));
    }

    /**
     * Get 2 addresses. one in address collection and the other is not found in RequestedAddress collection.
     * the second address is not found in history node and is not found in storage node.
     * the address is expected to be returned by history node and the response will be TRUE, FALSE(Ordered).
     */
//    @Test
    public void addressesExist_addressInLocalDbAndNotFound_shouldGetResponseFromStorageNode_IT() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressNotFoundInFullNodeAndNotFound.getHash());
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse1 = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse1, response));

        AddressesExistsResponse expectedResponse2 = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressNotFoundInFullNodeAndNotFound.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse2, response));
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
