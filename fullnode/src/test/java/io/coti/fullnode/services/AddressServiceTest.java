package io.coti.fullnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.http.AddressBulkRequest;
import io.coti.basenode.http.AddressesExistsResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.RequestedAddressHashes;
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
import utils.AddressTestUtils;
import utils.HashTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressService.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AddressServiceTest {

    @Autowired
    private AddressService addressService;
    @MockBean
    private NetworkService networkServiceLocal;
    @MockBean
    private Addresses addressesLocal;
    @MockBean
    private RequestedAddressHashes requestedAddressHashesLocal;

    private AddressData addressInLocalAddressesCollection = AddressTestUtils.generateRandomAddressData();
    private RequestedAddressHashData addressInRequestedAddressesCollectionLessThenTenMinutes = new RequestedAddressHashData(HashTestUtils.generateRandomAddressHash());


    @BeforeEach
    void setUpBeforeEachTest() {
        networkService = networkServiceLocal;
        addresses = addressesLocal;
        requestedAddressHashes = requestedAddressHashesLocal;
        when(addresses.getByHash(addressInLocalAddressesCollection.getHash())).thenReturn(addressInLocalAddressesCollection);
        setTimeAndMock(addressInRequestedAddressesCollectionLessThenTenMinutes, 300_000);

        mockNetworkService();
    }

    /**
     * Get 1 address which is available in address collection.
     * the expected response is Hash, TRUE.
     */
    @Test
    void addressesExist_addressInLocalDB_shouldReturnHashAndTrue() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash());

        AddressesExistsResponse actualResponse = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash()),
                Boolean.TRUE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse, actualResponse));
    }

    /**
     * Get 2 addresses. 1 is available in local addresses collection. The second is available in RequestedAddresses,
     * inserted less then 10 minutes ago.
     * the expected response is Hash, TRUE,TRUE.
     */
    @Test
    void addressesExist_addressInLocalDbAndUpdatedFromHistoryLessThenTenMinutes_shouldReturnTrueAndFalse() {
        AddressBulkRequest addressBulkRequest = AddressTestUtils.generateAddressBulkRequest(
                addressInLocalAddressesCollection.getHash(), addressInRequestedAddressesCollectionLessThenTenMinutes.getHash());
        AddressesExistsResponse actualResponse = addressService.addressesCheckExistenceAndRequestHistoryNode(addressBulkRequest);

        AddressesExistsResponse expectedResponse = AddressTestUtils.generateExpectedResponse(
                AddressTestUtils.initMapWithHashes(
                        addressInLocalAddressesCollection.getHash(),
                        addressInRequestedAddressesCollectionLessThenTenMinutes.getHash()),
                Boolean.TRUE,
                Boolean.FALSE);

        Assertions.assertTrue(AddressTestUtils.equals(expectedResponse, actualResponse));
    }


    private void setTimeAndMock(RequestedAddressHashData requestedAddressHashData, long insertionTime) {
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
