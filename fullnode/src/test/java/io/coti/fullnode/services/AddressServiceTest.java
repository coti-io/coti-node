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
import io.coti.fullnode.http.AddressBulkRequest;
import io.coti.fullnode.http.AddressesExistsResponse;
import io.coti.fullnode.websocket.WebSocketSender;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressService.class, Addresses.class, IValidationService.class, IDatabaseConnector.class, HttpJacksonSerializer.class, GetHistoryAddressesRequestCrypto.class, CryptoHelper.class, NodeCryptoHelper.class, GetHistoryAddressesResponseCrypto.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceTest {

    private final String HISTORY_PORT = "7031";

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


    @Before
    public void setUpBeforeEachTest() {
        when(addresses.getByHash(addressInLocalAddressesCollection.getHash())).thenReturn(addressInLocalAddressesCollection);
        setTimeAndMock(addressInRequestedAddressesCollectionLessThenTenMinutes, 300_000);

        mockNetworkService();
    }

    /**
     * Get 1 address which is available in address collection.
     * the expected response is Hash, TRUE.
     */
    @Test
    public void addressesExist_addressInLocalDB_shouldReturnHashAndTrue() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash());

        AddressesExistsResponse actualResponse = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash()),
                Boolean.TRUE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse, actualResponse));
    }

    /**
     * Get 2 addresses. 1 is available in local addresses collection. The second is available in RequestedAddresses,
     * inserted less then 10 minutes ago.
     * the expected response is Hash, TRUE,TRUE.
     */
    @Test
    public void addressesExist_addressInLocalDbAndUpdatedFromHistoryLessThenTenMinutes_shouldReturnTrueAndFalse() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(),addressInRequestedAddressesCollectionLessThenTenMinutes.getHash());
        AddressesExistsResponse actualResponse = addressService.addressesExist(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionLessThenTenMinutes.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assert.assertTrue(AddressTestUtils.equals(expectedResponse, actualResponse));
    }


    private void setTimeAndMock(RequestedAddressHashData requestedAddressHashData, long insertionTime){
        requestedAddressHashData.setLastUpdateTime(Instant.now().minusMillis(insertionTime));
        when(requestedAddressHashes.getByHash(requestedAddressHashData.getHash())).thenReturn(requestedAddressHashData);
    }

    private void mockNetworkService() {
        NetworkNodeData networkNodeData = new NetworkNodeData();
        networkNodeData.setAddress("localhost");
        networkNodeData.setHttpPort("HISTORY_PORT");
        Map<Hash, NetworkNodeData> networkNodeDataMap = new HashMap<>();
        networkNodeDataMap.put(new Hash("aaaa"), networkNodeData);

        when(networkService.getMapFromFactory(NodeType.HistoryNode)).thenReturn(networkNodeDataMap);
    }

}
