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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

    //TODO 7/30/2019 astolia: maybe mock this?
    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    //TODO 7/30/2019 astolia: maybe mock this?
    @Autowired
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    //TODO 7/30/2019 astolia: maybe mock this?
    @MockBean
    private NetworkService networkService;
    @MockBean
    private Addresses addresses;
    @MockBean
    private RequestedAddressHashes requestedAddressHashes;

    //
    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private IValidationService validationService;
    //

    private AddressData addressInLocalAddressesCollection;
    private RequestedAddressHashData addressInRequestedAddressesCollectionLessThenTenMinutes;
    private RequestedAddressHashData addressInRequestedAddressesCollectionMoreThenTenMinutes;

    @Before
    public void setUpBeforeEachTest() {
        addressInLocalAddressesCollection = AddressTestUtils.generateRandomAddressData();
        when(addresses.getByHash(addressInLocalAddressesCollection.getHash())).thenReturn(addressInLocalAddressesCollection);

        addressInRequestedAddressesCollectionLessThenTenMinutes = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());
        addressInRequestedAddressesCollectionLessThenTenMinutes.setLastUpdateTime(Instant.now().minusMillis(300_000)); //Mock insertion 5 minutes ago
        when(requestedAddressHashes.getByHash(addressInRequestedAddressesCollectionLessThenTenMinutes.getHash())).thenReturn(addressInRequestedAddressesCollectionLessThenTenMinutes);

        addressInRequestedAddressesCollectionMoreThenTenMinutes = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());
        addressInRequestedAddressesCollectionMoreThenTenMinutes.setLastUpdateTime(Instant.now().minusMillis(660_000)); //Mock insertion 11 minutes ago
        when(requestedAddressHashes.getByHash(addressInRequestedAddressesCollectionMoreThenTenMinutes.getHash())).thenReturn(addressInRequestedAddressesCollectionMoreThenTenMinutes);

        mockNetworkService();
    }

    @After
    public void clearUpAfterEachTest() {
        addresses.delete(addressInLocalAddressesCollection);
    }

    @Test
    public void addressesExist_hashInLocalDB_shouldReturnHashAndTrue() {
        List<Hash> addressHashes = new ArrayList<>();
        addressHashes.add(addressInLocalAddressesCollection.getHash());

        AddressBulkRequest addressBulkRequest = new AddressBulkRequest();
        addressBulkRequest.setAddresses(addressHashes);
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        LinkedHashMap<String,Boolean> responseMap = new LinkedHashMap<>();
        responseMap.put(addressInLocalAddressesCollection.getHash().toHexString(),Boolean.TRUE);
        AddressesExistsResponse expectedResponse = new AddressesExistsResponse(responseMap);

        Assert.assertTrue(equals(expectedResponse,response));
    }


    @Test
    public void addressesExist_hashInLocalDbAndUpdatedFromHistoryLessThenTenMinutes_shouldReturnTrueAndFalseResults() {
        List<Hash> addressHashes = new ArrayList<>();
        addressHashes.add(addressInLocalAddressesCollection.getHash());
        addressHashes.add(addressInRequestedAddressesCollectionLessThenTenMinutes.getHash());

        AddressBulkRequest addressBulkRequest = new AddressBulkRequest();
        addressBulkRequest.setAddresses(addressHashes);
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        LinkedHashMap<String,Boolean> responseMap = new LinkedHashMap<>();
        responseMap.put(addressInLocalAddressesCollection.getHash().toHexString(),Boolean.TRUE);
        responseMap.put(addressInRequestedAddressesCollectionLessThenTenMinutes.getHash().toHexString(),Boolean.FALSE);
        AddressesExistsResponse expectedResponse = new AddressesExistsResponse(responseMap);

        Assert.assertTrue(equals(expectedResponse,response));
    }

    // History and Storage should be running for this.
    @Test
    public void addressesExist_hashInLocalDbAndUpdatedFromHistoryMoreThenTenMinutes_shouldGetResponseFromHistoryNode_IT() {
        List<Hash> addressHashes = new ArrayList<>();
        addressHashes.add(addressInLocalAddressesCollection.getHash());
        addressHashes.add(addressInRequestedAddressesCollectionMoreThenTenMinutes.getHash());

        AddressBulkRequest addressBulkRequest = new AddressBulkRequest();
        addressBulkRequest.setAddresses(addressHashes);
        AddressesExistsResponse response = addressService.addressesExist(addressBulkRequest);

        LinkedHashMap<String,Boolean> responseMap = new LinkedHashMap<>();
        responseMap.put(addressInLocalAddressesCollection.getHash().toHexString(),Boolean.TRUE);
        responseMap.put(addressInRequestedAddressesCollectionMoreThenTenMinutes.getHash().toHexString(),Boolean.FALSE); // should depend on response from history
        AddressesExistsResponse expectedResponse = new AddressesExistsResponse(responseMap);

        Assert.assertTrue(equals(expectedResponse,response));
    }

    private void mockNetworkService(){
        NetworkNodeData networkNodeData = new NetworkNodeData();
        networkNodeData.setAddress("localhost");
        networkNodeData.setHttpPort("7031");
        Map<Hash,NetworkNodeData> networkNodeDataMap = new HashMap<>();
        networkNodeDataMap.put(new Hash("fake"),networkNodeData);

        when(networkService.getMapFromFactory(NodeType.HistoryNode)).thenReturn(networkNodeDataMap);
    }

    public boolean equals(AddressesExistsResponse expected, Object actual) {
        if (expected == actual) return true;
        if (!(actual instanceof AddressesExistsResponse)) return false;
        AddressesExistsResponse actualCasted = (AddressesExistsResponse) actual;
        Iterator<Map.Entry<String, Boolean>> thisItr = expected.getAddresses().entrySet().iterator();
        Iterator<Map.Entry<String, Boolean>> otherItr = actualCasted.getAddresses().entrySet().iterator();
        while ( thisItr.hasNext() && otherItr.hasNext()) {
            Map.Entry<String, Boolean> thisEntry = thisItr.next();
            Map.Entry<String, Boolean> otherEntry = otherItr.next();
            if (! thisEntry.equals(otherEntry))
                return false;
        }
        return !(thisItr.hasNext() || otherItr.hasNext());
    }

}
