package io.coti.historynode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.historynode.http.GetAddressResponse;
import io.coti.historynode.services.interfaces.IStorageConnector;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import utils.TestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = HistoryAddressService.class)
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class HistoryAddressServiceTest {

    public static final int NUMBER_OF_ADDRESSES = 8;

    @Autowired
    private HistoryAddressService addressService;

    @MockBean
    private IStorageConnector storageConnector;

    @MockBean
    private Addresses addresses;

    private Hash hash;
    private Set<Hash> hashes;
    private GetAddressesBulkRequest getAddressesBulkRequest;

    @BeforeClass
    public void setUpOnce() throws Exception {
        hash = TestUtils.generateRandomHash();
        hashes = new HashSet<>();
        hashes.add(hash);
        getAddressesBulkRequest = new GetAddressesBulkRequest(hashes);
    }

    /** getAddress
     *  Scenarios:
     *      addressData in RocksDB - returns response entity with addressData and hash, status ok
     *      addressData not in RocksDB and in StorageNode - returns response entity with addressData and hash, status ok
     *      addressData not in RocksDB and not in StorageNode - returns response entity with null as addressData and hash, status ok
    **/
    @Test
    public void getAddress(){
        // test signature validation
        // test correctly splits set of hashes to found and not found
        getAddress_AddressInRocksDb_returnFoundAddress();
        getAddress_AddressNotInRocksDb_returnFoundAddress();
        getAddress_AddressNotInRocksDbOrStorage_returnNotFoundAddress();

    }

    public void getAddress_AddressInRocksDb_returnFoundAddress() {
        AddressData addressData = new AddressData(hash);
        when(addresses.getByHash(hash)).thenReturn(addressData);
        ResponseEntity<IResponse> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressResponse(hash,addressData));
        Assert.assertEquals(expectedResponse, addressService.getAddresses(getAddressesBulkRequest));
    }

    public void getAddress_AddressNotInRocksDb_returnFoundAddress() {
        AddressData addressData = new AddressData(hash);
        when(addresses.getByHash(hash)).thenReturn(null);
        Assert.assertEquals(null, addressService.getAddresses(getAddressesBulkRequest));
        ResponseEntity<IResponse> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressResponse(hash,addressData));
//        when(addressService.getAddressFromStorage(hash)).thenReturn(response);
        Assert.assertEquals(expectedResponse, addressService.getAddresses(getAddressesBulkRequest));
    }

    public void getAddress_AddressNotInRocksDbOrStorage_returnNotFoundAddress() {
        Hash hash = TestUtils.generateRandomHash();
        AddressData addressData = new AddressData(hash);
        when(addresses.getByHash(hash)).thenReturn(null);
        Assert.assertEquals(null, addressService.getAddresses(getAddressesBulkRequest));
        ResponseEntity<IResponse> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressResponse(hash,null));
        //when(addressService.getAddressFromStorage(hash)).thenReturn(response);
        Assert.assertEquals(expectedResponse, addressService.getAddresses(getAddressesBulkRequest));
    }

//    @Test
//    public void getAddress_noExceptionIsThrown() {
//        try {
//            Hash address;
//            AddressData addressData;
//            Hash hash = TestUtils.generateRandomHash();
//            address = hash;
//            addressData = new AddressData(hash);
//
//            ResponseEntity<IResponse> response = ResponseEntity
//                    .status(HttpStatus.OK)
//                    .body(new GetAddressResponse(hash, addressData));
//            when(storageConnector.getForObject(any(String.class), any(Class.class), any(GetEntityRequest.class)))
//                    .thenReturn(response);
//            addressService.getAddressFromStorage(address);
//        } catch (Exception e) {
//            Assert.fail(e.getMessage());
//        }
//    }
}