package io.coti.historynode.services;

import io.coti.basenode.crypto.AddressesRequestCrypto;
import io.coti.basenode.crypto.AddressesResponseCrypto;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.historynode.http.GetAddressResponse;
import org.junit.Assert;
import org.junit.Before;
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
import utils.HashTestUtils;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AddressService.class,StorageConnector.class, BaseNodeValidationService.class, AddressesResponseCrypto.class, AddressesRequestCrypto.class, CryptoHelper.class, NodeCryptoHelper.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceTest {

    public static final int NUMBER_OF_ADDRESSES = 8;

    @Autowired
    private AddressService addressService;

    @MockBean
    private StorageConnector storageConnector;

    @MockBean
    private Addresses addresses;

    @MockBean
    private BaseNodeValidationService baseNodeValidationService;

    private Hash hash;
    private List<Hash> hashes;
    private GetHistoryAddressesRequest getHistoryAddressesRequest;

    @Before
    public void setUpOnce() throws Exception {
        hash = HashTestUtils.generateRandomAddressHash();
        hashes = new ArrayList<>();
        hashes.add(hash);
        getHistoryAddressesRequest = new GetHistoryAddressesRequest(hashes);
    }

    /** getAddress
     *  Scenarios:
     *      addressData in RocksDB - returns response entity with addressData and hash, status ok
     *      addressData not in RocksDB and in StorageNode - returns response entity with addressData and hash, status ok
     *      addressData not in RocksDB and not in StorageNode - returns response entity with null as addressData and hash, status ok
    **/

    @Test
    public void getAddress_AddressInRocksDb_returnFoundAddress() {
        AddressData addressData = new AddressData(hash);
        when(addresses.getByHash(hash)).thenReturn(addressData);
        ResponseEntity<IResponse> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressResponse(hash,addressData));
        Assert.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));
    }

    @Test
    public void getAddress_AddressNotInRocksDb_returnFoundAddress() {
        AddressData addressData = new AddressData(hash);
        when(addresses.getByHash(hash)).thenReturn(null);
        Assert.assertEquals(null, addressService.getAddresses(getHistoryAddressesRequest));
        ResponseEntity<IResponse> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressResponse(hash,addressData));
//        when(addressService.getAddressFromStorage(hash)).thenReturn(response);
        Assert.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));
    }

    @Test
    public void getAddress_AddressNotInRocksDbOrStorage_returnNotFoundAddress() {
        Hash hash = TestUtils.generateRandomHash();
        AddressData addressData = new AddressData(hash);
        when(addresses.getByHash(hash)).thenReturn(null);
        Assert.assertEquals(null, addressService.getAddresses(getHistoryAddressesRequest));
        ResponseEntity<IResponse> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressResponse(hash,null));
        //when(addressService.getAddressFromStorage(hash)).thenReturn(response);
        Assert.assertEquals(expectedResponse, addressService.getAddresses(getHistoryAddressesRequest));
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