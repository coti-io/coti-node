package io.coti.storagenode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.database.DbConnectorService;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.AddressAsObjectAndJsonString;
import testUtils.TransactionAsObjectAndJsonString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static testUtils.TestUtils.*;

@ContextConfiguration(classes = DbConnectorService.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class DbConnectorServiceITest {

    private final static int NUMBER_OF_OBJECTS = 6;

    private Map<String, String> indexes;

    @Autowired
    private DbConnectorService dbConnectorService;

    @Before
    public void setUp() throws Exception {
        indexes = new HashMap<>();
        indexes.put(TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME);
        indexes.put(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        dbConnectorService.addIndexes(false);
    }

    //    @Test
    public void getClusterDetails_noException() throws IOException {
        ClusterGetSettingsResponse clusterDetails = dbConnectorService.getClusterDetails(indexes.keySet());
        Assert.assertNotNull(clusterDetails);
    }

    //    @Test
    public void testTransactions() throws IOException {
        TransactionAsObjectAndJsonString transactionAsObjectAndJsonString = getRandomTransactionAsObjectAndJsonString();
        IndexResponse indexResponse = dbConnectorService.insertObjectToDb(transactionAsObjectAndJsonString.getHash(),
                transactionAsObjectAndJsonString.getTransactionAsJsonString(),
                TRANSACTION_INDEX_NAME,
                TRANSACTION_OBJECT_NAME, false);
        Assert.assertTrue(indexResponse.getResult().getLowercase().equals("created"));
        GetResponse objectFromDbByHashResponse = dbConnectorService.getObjectFromDbByHash(transactionAsObjectAndJsonString.getHash(), TRANSACTION_INDEX_NAME, false);
        Object transactionAsJsonFromDb = objectFromDbByHashResponse.getSourceAsMap().get(TRANSACTION_OBJECT_NAME);
        Assert.assertNotNull(transactionAsJsonFromDb);
    }

    //    @Test
    public void testAddresses() throws IOException {
        AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
        dbConnectorService.insertObjectToDb(addressAsObjectAndJsonString.getHash(),
                addressAsObjectAndJsonString.getAddressAsJsonString(),
                ADDRESS_INDEX_NAME,
                ADDRESS_OBJECT_NAME, false);
        GetResponse objectFromDbByHashResponse = dbConnectorService.getObjectFromDbByHash(addressAsObjectAndJsonString.getHash(), ADDRESS_INDEX_NAME, false);
        Object addressAsJsonFromDb = objectFromDbByHashResponse.getSourceAsMap().get(ADDRESS_OBJECT_NAME);
        Assert.assertNotNull(addressAsJsonFromDb);
    }

    //    @Test
    public void insertAndGetMultiObjects() throws Exception {
        Map<Hash, String> hashToObjectJsonDataMap = insertAddressBulk();
        Map<Hash, String> hashToObjectsFromDbMap = getMultiObjectsFromDb(ElasticSearchData.ADDRESSES.getIndex(), new ArrayList<>(hashToObjectJsonDataMap.keySet()), ElasticSearchData.ADDRESSES.getObjectName());
        Assert.assertTrue(insertedObjectsEqualToObjectsFromDb(hashToObjectJsonDataMap, hashToObjectsFromDbMap));
    }

    //    @Test
    public void deleteAddressByHash_hashNotExist() {
        DeleteResponse deleteResponse = dbConnectorService.deleteObject(generateRandomHash(), ElasticSearchData.ADDRESSES.getIndex(), false);
        Assert.assertTrue(deleteResponse.status().equals(RestStatus.NOT_FOUND));
    }

    //    @Test
    public void deleteAddressByHash_hashExist() throws IOException {
        AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
        dbConnectorService.insertObjectToDb(addressAsObjectAndJsonString.getHash(),
                addressAsObjectAndJsonString.getAddressAsJsonString(),
                ADDRESS_INDEX_NAME,
                ElasticSearchData.ADDRESSES.getObjectName(), false);
        DeleteResponse deleteResponse = dbConnectorService.deleteObject(addressAsObjectAndJsonString.getHash(), ElasticSearchData.ADDRESSES.getIndex(), false);
        Assert.assertTrue(deleteResponse.status().equals(RestStatus.OK));
    }

    private boolean insertedObjectsEqualToObjectsFromDb(Map<Hash, String> hashToObjectJsonDataMap, Map<Hash, String> hashToObjectsFromDbMap) {
        return hashToObjectJsonDataMap.size() == hashToObjectsFromDbMap.size();
    }

    private Map<Hash, String> getMultiObjectsFromDb(String indexName, List<Hash> hashes, String fieldName) {
        return dbConnectorService.getMultiObjects(hashes, indexName, false, fieldName);
    }

    private Map<Hash, String> insertAddressBulk() throws Exception {
        Map<Hash, String> hashToObjectJsonDataMap = new HashMap<>();
        for (int i = 0; i < NUMBER_OF_OBJECTS; i++) {
            AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
            hashToObjectJsonDataMap.put(addressAsObjectAndJsonString.getHash(), addressAsObjectAndJsonString.getAddressAsJsonString());
        }
        dbConnectorService.insertMultiObjectsToDb(ADDRESS_INDEX_NAME, ElasticSearchData.ADDRESSES.getObjectName(), hashToObjectJsonDataMap, false);
        return hashToObjectJsonDataMap;
    }

    private TransactionAsObjectAndJsonString getRandomTransactionAsObjectAndJsonString() throws JsonProcessingException {
        TransactionData transactionData = createRandomTransaction();
        ObjectMapper mapper = new ObjectMapper();
        return new TransactionAsObjectAndJsonString(transactionData.getHash(), transactionData, mapper.writeValueAsString(transactionData));
    }

    private AddressAsObjectAndJsonString getRandomAddressAsObjectAndJsonString() throws JsonProcessingException {
        AddressTransactionsHistory addressTransactionsHistory = new AddressTransactionsHistory(generateRandomHash());
        ObjectMapper mapper = new ObjectMapper();
        return new AddressAsObjectAndJsonString(addressTransactionsHistory.getHash(), addressTransactionsHistory, mapper.writeValueAsString(addressTransactionsHistory));
    }

}