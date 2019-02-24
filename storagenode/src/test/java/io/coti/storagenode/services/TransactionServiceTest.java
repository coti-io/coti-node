package io.coti.storagenode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
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
import static testUtils.TestUtils.createRandomTransaction;

@ContextConfiguration(classes = {TransactionService.class, DbConnectorService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    private static final int NUMBER_OF_TRANSACTIONS = 4;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DbConnectorService dbConnectorService;

    private ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper();
    }

    @Test
    public void transactionTest() throws IOException {
        TransactionData transactionData1 = createRandomTransaction();
        TransactionData transactionData2 = createRandomTransaction();

        String transactionAsJson = mapper.writeValueAsString(transactionData1);
        transactionService.insertObjectJson(transactionData1.getHash(), transactionAsJson, false);

        IResponse deleteResponse = transactionService.deleteObjectByHash(transactionData2.getHash(), false).getBody();

        GetEntityJsonResponse response = (GetEntityJsonResponse) transactionService.getObjectByHash(transactionData1.getHash(), false).getBody();
        Assert.assertTrue(response.getStatus().equals(STATUS_SUCCESS) &&
                ((GetEntityJsonResponse) deleteResponse).status.equals(STATUS_SUCCESS));
    }

    @Test
    public void multiTransactionTest() throws IOException {
        Map<Hash, String> hashToTransactionJsonDataMap = new HashMap<>();
        List<TransactionData> TransactionDataList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            TransactionData transactionData = createRandomTransaction();
            TransactionDataList.add(transactionData);
            hashToTransactionJsonDataMap.put(transactionData.getHash(), mapper.writeValueAsString(transactionData));
        }
        transactionService.insertMultiObjects(hashToTransactionJsonDataMap, false);

        List<Hash> deleteHashes = new ArrayList<>();
        deleteHashes.add(TransactionDataList.get(0).getHash());
        deleteHashes.add(TransactionDataList.get(1).getHash());

        IResponse deleteResponse = transactionService.deleteMultiObjectsFromDb(deleteHashes, false).getBody();

        List<Hash> GetHashes = new ArrayList<>();
        GetHashes.add(TransactionDataList.get(2).getHash());
        GetHashes.add(TransactionDataList.get(3).getHash());

        IResponse response = transactionService.getMultiObjectsFromDb(GetHashes, false).getBody();

        Assert.assertTrue(((BaseResponse) (response)).getStatus().equals(STATUS_SUCCESS)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(TransactionDataList.get(0).getHash()).equals(STATUS_OK)
                && ((GetEntitiesBulkJsonResponse) deleteResponse).getHashToEntitiesFromDbMap().get(TransactionDataList.get(1).getHash()).equals(STATUS_OK));
    }


}