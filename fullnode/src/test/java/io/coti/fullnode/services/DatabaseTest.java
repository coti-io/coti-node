package io.coti.fullnode.services;


import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.basenode.crypto.GetHistoryAddressesResponseCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.*;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.database.RocksDBConnector;
import io.coti.fullnode.model.ExplorerIndexes;
import io.coti.fullnode.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.List;

import static utils.AddressTestUtils.generateListOfRandomAddressData;

@Slf4j
@ContextConfiguration(classes = {RocksDBConnector.class, InitializationService.class, Addresses.class, AddressData.class, Transactions.class, AddressService.class,
        IValidationService.class, RocksDBConnector.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest()
public class DatabaseTest {


    @Autowired
    private Addresses addresses;
    @Autowired
    private Transactions transactions;
    @Autowired
    private AddressService addressService;
    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private NetworkService networkService;
    //    @Autowired
//    private Addresses addresses;
    @MockBean
    private RequestedAddressHashes requestedAddressHashes;
    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    @MockBean
    private GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    @MockBean
    private HttpJacksonSerializer jacksonSerializer;
    @MockBean
    private InitializationService initializationService;
    @Autowired
    private AddressData addressData;
//    @Autowired
//    private BaseNodeValidationService validationService;
    @MockBean
    private IValidationService validationService;
    @Autowired
    private RocksDBConnector rocksDBConnector;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private TransactionVotes transactionVotes;
    @MockBean
    private NodeRegistrations nodeRegistrations;
    @MockBean
    private ExplorerIndexes explorerIndexes;


    @Test
    public void storeAddresses() {
        rocksDBConnector.init();

        Instant preStart = Instant.now();
        log.info("Test about to start {}", preStart);

        int numOfAddresses = 10;
        List<AddressData> addressDataList = generateListOfRandomAddressData(numOfAddresses);
        addressDataList.stream().forEach(addressData -> addresses.put(addressData));

        Instant postEnd = Instant.now();
        log.info("Test ended {} after {} ms", postEnd, postEnd.toEpochMilli() - preStart.toEpochMilli());
    }

}
