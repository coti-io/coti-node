package io.coti.fullnode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.data.RequestedAddressHashData;
import io.coti.fullnode.http.AddressBulkRequest;
import io.coti.fullnode.http.AddressesExistsResponse;
import io.coti.fullnode.model.RequestedAddressHashes;
import io.coti.fullnode.websocket.WebSocketSender;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import utils.AddressTestUtils;
import utils.HashTestUtils;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressService.class, Addresses.class, IValidationService.class, IDatabaseConnector.class, HttpJacksonSerializer.class, GetHistoryAddressesRequestCrypto.class, CryptoHelper.class, NodeCryptoHelper.class, GetHistoryAddressesResponseCrypto.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceTest {

    @Autowired
    private AddressService addressService;
    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    @MockBean
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @MockBean
    private NetworkService networkService;
    @MockBean
    private Addresses addresses;
    @MockBean
    private RequestedAddressHashes requestedAddressHashes;

    // Unused but required for compilation
    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private IValidationService validationService;
    //

    private AddressData addressInLocalAddressesCollection = AddressTestUtils.generateRandomAddressData();
    private RequestedAddressHashData addressInRequestedAddressesCollectionLessThenTenMinutes = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());
    private RequestedAddressHashData addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());
    private RequestedAddressHashData addressInRequestedAddressesCollectionMoreThenTenMinutesNotInHistory = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());
    private RequestedAddressHashData addressNotFoundInFullNodeAndFoundInHistory = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());
    private RequestedAddressHashData addressNotFoundInFullNodeAndNotFoundInHistory = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());

    @Before
    public void setUp(){
        // Hash that is mocked to be found in local RocksDB addresses collection
        when(addresses.getByHash(addressInLocalAddressesCollection.getHash())).thenReturn(addressInLocalAddressesCollection);

        // Hash that is mocked to be found in local RocksDB requested addresses collection inserted less then 10 minutes ago.
        setTimeAndMock(addressInRequestedAddressesCollectionLessThenTenMinutes, 300_000);

        // Hash that is mocked to be found in local RocksDB requested addresses collection inserted more then 10 minutes ago and will be returned by history node upon request.
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory, 660_000);

        // Hash that is mocked to be found in local RocksDB requested addresses collection inserted more then 10 minutes ago and will not be returned by history node upon request.
        setTimeAndMock(addressInRequestedAddressesCollectionMoreThenTenMinutesNotInHistory, 660_000);

        // Hash that is mocked to be found in local RocksDB requested addresses collection inserted more then 10 minutes ago and will not be returned by history node upon request.
        setTimeAndMock(addressNotFoundInFullNodeAndFoundInHistory, 660_000);

        // Hash that is mocked to be found in local RocksDB requested addresses collection inserted more then 10 minutes ago and will not be returned by history node upon request.
        setTimeAndMock(addressNotFoundInFullNodeAndNotFoundInHistory, 660_000);

        mockNetworkService();

    }

    private void setTimeAndMock(RequestedAddressHashData requestedAddressHashData, long insertionTime){
        requestedAddressHashData.setLastUpdateTime(Instant.now().minusMillis(insertionTime));
        when(requestedAddressHashes.getByHash(requestedAddressHashData.getHash())).thenReturn(requestedAddressHashData);
    }

    @Test
    public void addressesExist_hashInLocalDB_shouldReturnHashAndTrue() {
        AddressBulkRequest addressBulkRequest = generateAddressBulkRequest(addressInLocalAddressesCollection.getHash());
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);
        AddressesExistsResponse expectedResponse = generateExpectedResponse(initMapWithHashes(addressInLocalAddressesCollection.getHash()), Boolean.TRUE);
        Assert.assertTrue(equals(expectedResponse, response));
    }

    @Test
    public void addressesExist_hashInLocalDbAndUpdatedFromHistoryLessThenTenMinutes_shouldReturnTrueAndFalseResults() {
        AddressBulkRequest addressBulkRequest = generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressInRequestedAddressesCollectionLessThenTenMinutes.getHash());

        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = generateExpectedResponse(
                initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                addressInRequestedAddressesCollectionLessThenTenMinutes.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assert.assertTrue(equals(expectedResponse, response));
    }

    // History and Storage should be running for this.
    @Test
    public void addressesExist_hashInLocalDbAndUpdatedFromHistoryMoreThenTenMinutes_shouldGetResponseFromHistoryNode_IT() {
        AddressBulkRequest addressBulkRequest = generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),
                addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory.getHash());
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = generateExpectedResponse(
                initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionMoreThenTenMinutesInHistory.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assert.assertTrue(equals(expectedResponse, response));
    }

    @Test
    public void addressesExist_hashInLocalDbAndUpdatedFromHistoryMoreThenTenMinutesAndInStorageNode_shouldGetResponseFromHistoryNode_IT() {
        Hash addressHash = new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51885");
        RequestedAddressHashData requestedAddressHashData = new RequestedAddressHashData(addressHash); //same hash as in storage node elastic search

        List<Hash> addressHashes = new ArrayList<>();
        addressHashes.add(addressInLocalAddressesCollection.getHash());
        addressHashes.add(requestedAddressHashData.getHash());

        AddressBulkRequest addressBulkRequest = new AddressBulkRequest();
        addressBulkRequest.setAddresses(addressHashes);
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        LinkedHashMap<String, Boolean> responseMap = new LinkedHashMap<>();
        responseMap.put(addressInLocalAddressesCollection.getHash().toHexString(), Boolean.TRUE);
        responseMap.put(requestedAddressHashData.getHash().toHexString(), Boolean.TRUE);
        AddressesExistsResponse expectedResponse = new AddressesExistsResponse(responseMap);

        Assert.assertTrue(equals(expectedResponse, response));
    }

    private void mockNetworkService() {
        NetworkNodeData networkNodeData = new NetworkNodeData();
        networkNodeData.setAddress("localhost");
        networkNodeData.setHttpPort("7031");
        Map<Hash, NetworkNodeData> networkNodeDataMap = new HashMap<>();
        networkNodeDataMap.put(new Hash("aaaa"), networkNodeData);

        when(networkService.getMapFromFactory(NodeType.HistoryNode)).thenReturn(networkNodeDataMap);
    }

    private boolean equals(AddressesExistsResponse expected, Object actual) {
        if (expected == actual) return true;
        if (!(actual instanceof AddressesExistsResponse)) return false;
        AddressesExistsResponse actualCasted = (AddressesExistsResponse) actual;
        Iterator<Map.Entry<String, Boolean>> thisItr = expected.getAddresses().entrySet().iterator();
        Iterator<Map.Entry<String, Boolean>> otherItr = actualCasted.getAddresses().entrySet().iterator();
        while (thisItr.hasNext() && otherItr.hasNext()) {
            Map.Entry<String, Boolean> thisEntry = thisItr.next();
            Map.Entry<String, Boolean> otherEntry = otherItr.next();
            if (!thisEntry.equals(otherEntry))
                return false;
        }
        return !(thisItr.hasNext() || otherItr.hasNext());
    }

    private LinkedHashMap<String, Boolean> initMapWithHashes(Hash... addressHashes){
        LinkedHashMap<String, Boolean> responseMap = new LinkedHashMap<>();
        Arrays.stream(addressHashes).forEach(addreassHash -> responseMap.put(addreassHash.toHexString(),null));
        return responseMap;
    }

    private AddressesExistsResponse generateExpectedResponse(LinkedHashMap<String, Boolean> responseMapHashInitiated, Boolean... addressUsageStatuses) {
        int i = 0;
        for(Map.Entry<String,Boolean> entry: responseMapHashInitiated.entrySet()){
            entry.setValue(addressUsageStatuses[i]);
            i++;
        }
        return new AddressesExistsResponse(responseMapHashInitiated);
    }

    private AddressBulkRequest generateAddressBulkRequest(Hash... addressHashes){
        List<Hash> addressHashesList = new ArrayList<>();
        Arrays.stream(addressHashes).forEach(addressHash -> addressHashesList.add(addressHash));

        AddressBulkRequest addressBulkRequest = new AddressBulkRequest();
        addressBulkRequest.setAddresses(addressHashesList);
        return addressBulkRequest;
    }

}
