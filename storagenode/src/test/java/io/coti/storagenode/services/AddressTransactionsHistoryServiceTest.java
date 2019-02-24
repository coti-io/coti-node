package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.http.GetEntitiesBulkJsonResponse;
import io.coti.storagenode.http.GetEntityJsonResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static io.coti.storagenode.http.HttpStringConstants.STATUS_OK;
import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {AddressTransactionsHistoryService.class, DbConnectorService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AddressTransactionsHistoryServiceTest {

    private static final int NUMBER_OF_ADDRESSES = 4;
    @Autowired
    private AddressTransactionsHistoryService addressTransactionsHistoryService;

    @Autowired
    private DbConnectorService dbConnectorService;

    private ObjectMapper mapper;


    @Before
    public void init() {
        mapper = new ObjectMapper();
    }

    @Test
    public void AddressTransactionsHistoryTest() throws IOException {
        AddressTransactionsHistory addressTransactionsHistory1 = new AddressTransactionsHistory(generateRandomHash());
        AddressTransactionsHistory addressTransactionsHistory2 = new AddressTransactionsHistory(generateRandomHash());

        String addressTransactionsHistoryAsJson = mapper.writeValueAsString(addressTransactionsHistory1);
        addressTransactionsHistoryService.insertObjectJson(addressTransactionsHistory1.getHash(), addressTransactionsHistoryAsJson, false);
        addressTransactionsHistoryService.insertObjectJson(addressTransactionsHistory2.getHash(), addressTransactionsHistoryAsJson, false);

        IResponse deleteResponse = addressTransactionsHistoryService.deleteObjectByHash(addressTransactionsHistory2.getHash(), false).getBody();

        IResponse getResponse = addressTransactionsHistoryService.getObjectByHash(addressTransactionsHistory1.getHash(), false).getBody();
        Assert.assertTrue(((BaseResponse) (getResponse)).getStatus().equals(STATUS_SUCCESS) &&
                ((GetEntityJsonResponse) deleteResponse).status.equals(STATUS_SUCCESS));
    }

    @Test
    public void multiAddressTransactionsHistoryTest() throws IOException {
        Map<Hash, String> hashToAddressTransactionsHistoryJsonDataMap = new HashMap<>();
        List<AddressTransactionsHistory> addressTransactionsHistories = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ADDRESSES; i++) {
            AddressTransactionsHistory addressTransactionsHistory = new AddressTransactionsHistory(generateRandomHash());
            addressTransactionsHistories.add(addressTransactionsHistory);
            hashToAddressTransactionsHistoryJsonDataMap.put(addressTransactionsHistory.getHash(), mapper.writeValueAsString(addressTransactionsHistory));
        }
        addressTransactionsHistoryService.insertMultiObjects(hashToAddressTransactionsHistoryJsonDataMap, false);

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(addressTransactionsHistories.get(0).getHash());
        deleteHashes.add(addressTransactionsHistories.get(1).getHash());

        IResponse deleteResponse = addressTransactionsHistoryService.deleteMultiObjectsFromDb(deleteHashes, false).getBody();

        List<Hash> GetHashes = new ArrayList<>();
        GetHashes.add(addressTransactionsHistories.get(2).getHash());
        GetHashes.add(addressTransactionsHistories.get(3).getHash());

        IResponse response = addressTransactionsHistoryService.getMultiObjectsFromDb(GetHashes, false).getBody();

        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(addressTransactionsHistories.get(0).getHash()).equals(STATUS_OK)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(addressTransactionsHistories.get(1).getHash()).equals(STATUS_OK));
    }
}