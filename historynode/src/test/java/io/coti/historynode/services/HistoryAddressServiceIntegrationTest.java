package io.coti.historynode.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.historynode.crypto.AddressesRequestCrypto;
import io.coti.historynode.crypto.AddressesResponseCrypto;
import io.coti.historynode.crypto.HistoryAddressCrypto;
import io.coti.historynode.database.HistoryRocksDBConnector;
import io.coti.historynode.http.GetAddressBatchResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@ContextConfiguration(classes = {HistoryAddressService.class, Addresses.class, HistoryRocksDBConnector.class, AddressesRequestCrypto.class, NodeCryptoHelper.class
, HistoryAddressCrypto.class, AddressesRequestCrypto.class, AddressesResponseCrypto.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class HistoryAddressServiceIntegrationTest {

    public static final int NUMBER_OF_ADDRESSES = 8;

    @Autowired
    private HistoryAddressService historyAddressService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private HistoryRocksDBConnector historyRocksDBConnector;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private Addresses addressesCollection;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;

    private GetAddressesBulkRequest getAddressesBulkRequest;

    private Hash insertedHash;

    @Before
    public void setUp() throws Exception {
        databaseConnector.init();
        getAddressesBulkRequest = TestUtils.generateGetAddressesRequest();
        addressesRequestCrypto.signMessage(getAddressesBulkRequest);
        insertedHash = getAddressesBulkRequest.getAddressesHash().iterator().next();
        insertAddressDataToRocksDB(insertedHash);
    }

    @After
    public void finishUp() throws Exception {
        addressesCollection.deleteByHash(insertedHash);
    }

    @Test
    public void getAddressesTestTemp(){
        historyAddressService.getAddresses(getAddressesBulkRequest);
    }

    @Test
    public void storePlusRetrieveAddress_AddressMatch()
    {
        // This is an integration test, requiring Storage Node to be up as well.

        List<Hash> addresses = new ArrayList<>();
        List<AddressData> addressesData = new ArrayList<>();
        IntStream.range(0, NUMBER_OF_ADDRESSES).forEachOrdered(n -> {
            Hash hash = TestUtils.generateRandomHash();
            addresses.add(hash);
            addressesData.add(new AddressData(hash));
        });

        ResponseEntity<IResponse> response = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressBatchResponse(addressesData));

        //TODO: not finished
// This currently fails
    }

    private void insertAddressDataToRocksDB(Hash addressHash){
        AddressData data = new AddressData(addressHash);
        addressesCollection.put(data);
    }

}