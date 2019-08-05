package io.coti.historynode.services;

import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.RequestedAddressHashes;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.historynode.database.RocksDBConnector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(classes = {AddressService.class, Addresses.class,RequestedAddressHashes.class, IDatabaseConnector.class, RocksDBConnector.class, StorageConnector.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceIntegrationTest {

    @Autowired
    private Addresses addresses;
    @Autowired
    private RequestedAddressHashes requestedAddressHashes;
    @Autowired
    public IDatabaseConnector databaseConnector;
    @Autowired
    private RocksDBConnector rocksDBConnector;

    // Unused but required for compilation
    @MockBean
    private InitializationService initializationService;
    @MockBean
    private HttpJacksonSerializer httpJacksonSerializer;
    @MockBean
    private BaseNodeValidationService baseNodeValidationService;
    @MockBean
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    //

    @Before
    public void setUp(){
        rocksDBConnector.setColumnFamily();
        databaseConnector.init();
    }

    // Write to local rocksdb collection for integration test with full node
    //@Test
    public void setUpForFullNodeIT(){
        addHashToRocksDBAddressesCollection("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51881");
        addHashToRocksDBAddressesCollection("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51884");
    }

    private void addHashToRocksDBAddressesCollection(String hashString){
        AddressData address = new AddressData(new Hash(hashString));
        addresses.put(address);
        Assert.assertEquals(address,addresses.getByHash(address.getHash()));
    }
}
