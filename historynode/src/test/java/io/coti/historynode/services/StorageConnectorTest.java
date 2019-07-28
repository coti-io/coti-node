package io.coti.historynode.services;


import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.AddHistoryAddressesRequest;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import utils.AddressTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ContextConfiguration(classes = {StorageConnector.class, GetHistoryAddressesRequestCrypto.class, NodeCryptoHelper.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageConnectorTest {

    @Autowired
    private StorageConnector storageConnector;

    @Autowired
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;

    @Value("${storage.server.address}")
    private String storageNodeUrl;

    @BeforeClass
    public static void setupOnce() {

    }

    //**************** Address Tests ****************

    /**
     * Retrieve addresses that aren't found in storage.
     * Should return map with hash as keys and null as values.
     */
    @Test
    public void retrieveAddresses_notStoredInStorage_shouldReturnMapWithNullValues() {
        int size = 3;
        List<AddressData> addresses = AddressTestUtils.generateListOfRandomAddressData(size);
        GetHistoryAddressesRequest request = new GetHistoryAddressesRequest(addresses.stream().map(addressData -> addressData.getHash()).collect(Collectors.toList()));
        getHistoryAddressesRequestCrypto.signMessage(request);
        ResponseEntity<GetHistoryAddressesResponse> retrieveResponse = storageConnector.retrieveFromStorage(storageNodeUrl + "/addresses", request, GetHistoryAddressesResponse.class);
        Assert.assertEquals(HttpStatus.OK, retrieveResponse.getStatusCode());
        Map<Hash, AddressData> addressHashesToAddresses = retrieveResponse.getBody().getAddressHashesToAddresses();
        Assert.assertEquals(size, addressHashesToAddresses.size());
        for (Map.Entry<Hash, AddressData> entry : addressHashesToAddresses.entrySet()) {
            Assert.assertNull(entry.getValue());
        }
        Assert.assertTrue(addressHashesToAddresses.keySet().containsAll(addresses.stream().map(a -> a.getHash()).collect(Collectors.toSet())));
    }

    @Test
    public void storeAddresses_notStoredInStorage_shouldReturnMapWithTrueValues() {
        int size = 3;
        List<AddressData> addresses = AddressTestUtils.generateListOfRandomAddressData(size);
        AddHistoryAddressesRequest request = new AddHistoryAddressesRequest(addresses);
        ResponseEntity<AddHistoryEntitiesResponse> retrieveResponse = storageConnector.storeInStorage(storageNodeUrl + "/addresses", request, AddHistoryEntitiesResponse.class);
        Assert.assertEquals(HttpStatus.OK, retrieveResponse.getStatusCode());
        Map<Hash, Boolean> addressHashesToStoreResult = retrieveResponse.getBody().getHashesToStoreResult();
        Assert.assertEquals(size, addressHashesToStoreResult.size());
        for (Map.Entry<Hash, Boolean> entry : addressHashesToStoreResult.entrySet()) {
            Assert.assertTrue(entry.getValue());
        }
    }

    @Test
    public void storeAddresses_storeExistingAddresses_shouldReturnMapWithTrueValues() {
        int size = 2;
        List<AddressData> addresses = AddressTestUtils.generateListOfRandomAddressData(size);
        AddHistoryAddressesRequest request = new AddHistoryAddressesRequest(addresses);
        ResponseEntity<AddHistoryEntitiesResponse> retrieveResponse = storageConnector.storeInStorage(storageNodeUrl + "/addresses", request, AddHistoryEntitiesResponse.class);
        Assert.assertEquals(HttpStatus.OK, retrieveResponse.getStatusCode());
        Map<Hash, Boolean> addressHashesToStoreResult = retrieveResponse.getBody().getHashesToStoreResult();
        Assert.assertEquals(size, addressHashesToStoreResult.size());
        for (Map.Entry<Hash, Boolean> entry : addressHashesToStoreResult.entrySet()) {
            Assert.assertTrue(entry.getValue());
        }

        ResponseEntity<AddHistoryEntitiesResponse> secondRetrieveResponse = storageConnector.storeInStorage(storageNodeUrl + "/addresses", request, AddHistoryEntitiesResponse.class);
        Assert.assertEquals(HttpStatus.OK, secondRetrieveResponse.getStatusCode());
        Map<Hash, Boolean> secondAddressHashesToStoreResult = secondRetrieveResponse.getBody().getHashesToStoreResult();
        Assert.assertEquals(size, secondAddressHashesToStoreResult.size());
        for (Map.Entry<Hash, Boolean> entry : secondAddressHashesToStoreResult.entrySet()) {
            Assert.assertTrue(entry.getValue());
        }
    }

    @Test
    public void storeAndRetrieveAddresses() {
        int size = 2;

        // Store in elastic and check correct size and correct result values.
        List<AddressData> addresses = AddressTestUtils.generateListOfRandomAddressData(size);
        AddHistoryAddressesRequest storeRequest = new AddHistoryAddressesRequest(addresses);
        ResponseEntity<AddHistoryEntitiesResponse> storeResponse = storageConnector.storeInStorage(storageNodeUrl + "/addresses", storeRequest, AddHistoryEntitiesResponse.class);
        Assert.assertEquals(storeResponse.getBody().getHashesToStoreResult().size(), size);
        Assert.assertEquals(HttpStatus.OK, storeResponse.getStatusCode());
        storeResponse.getBody().getHashesToStoreResult().values().forEach(storeResult -> Assert.assertTrue(storeResult));

        // get from elastic addresses that were stored above. make sure correct Http status, correct size, correct address data.
        GetHistoryAddressesRequest retrieveRequest = new GetHistoryAddressesRequest(addresses.stream().map(addressData -> addressData.getHash()).collect(Collectors.toList()));
        getHistoryAddressesRequestCrypto.signMessage(retrieveRequest);
        ResponseEntity<GetHistoryAddressesResponse> retrieveResponse = storageConnector.retrieveFromStorage(storageNodeUrl + "/addresses", retrieveRequest, GetHistoryAddressesResponse.class);
        Assert.assertEquals(retrieveResponse.getStatusCode(), HttpStatus.OK);
        Map<Hash, AddressData> addressHashesToAddresses = retrieveResponse.getBody().getAddressHashesToAddresses();
        Assert.assertEquals(addressHashesToAddresses.size(), size);
        Set<Hash> hashes = addresses.stream().map(addressData -> addressData.getHash()).collect(Collectors.toSet());
        addressHashesToAddresses.values().forEach(addressData -> Assert.assertTrue(hashes.contains(addressData.getHash())));
    }

}
