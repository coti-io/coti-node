package io.coti.historynode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeVote;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetAddressBatchResponse;
import io.coti.historynode.http.storageConnector.StorageConnector;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@ContextConfiguration(classes = {HistoryAddressService.class, StorageConnector.class})
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
    private StorageConnector storageConnector;

    @Autowired
    private IStorageConnector iStorageConnector;

    @Before
    public void setUp() throws Exception {
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

}