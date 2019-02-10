package io.coti.storagenode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

import static io.coti.storagenode.http.HttpStringConstants.STATUS_NOT_FOUND;
import static io.coti.storagenode.http.HttpStringConstants.STATUS_OK;
import static testUtils.TestUtils.createRandomTransaction;
import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = DbConnectorService.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class DbConnectorServiceTest {
    private final static String TRANSACTION_INDEX_NAME = "transactions";
    private final static String TRANSACTION_OBJECT_NAME = "transactionData";
    private final static String ADDRESS_INDEX_NAME = "address";
    private final static String ADDRESS_OBJECT_NAME = "addressData";
    private final static int NUMBER_OF_OBJECTS = 6;

    private Map<String, String> indexes;

    @Autowired
    private DbConnectorService dbConnectorService;

    @Before
    public void setUp() throws Exception {
        indexes = new HashMap<>();
        indexes.put(TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME);
        indexes.put(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        dbConnectorService.addIndexes(indexes, true);
    }

    @Test
    public void getClusterDetails() throws IOException {
        dbConnectorService.getClusterDetails(indexes.keySet(), true);
    }

    @Test
    public void testTransactions() throws IOException {
        TransactionAsObjectAndJsonString transactionAsObjectAndJsonString = getRandomTransactionAsObjectAndJsonString();
        dbConnectorService.insertObjectToDb(transactionAsObjectAndJsonString.getHash(),
                transactionAsObjectAndJsonString.getTransactionAsJsonString(),
                TRANSACTION_INDEX_NAME,
                TRANSACTION_OBJECT_NAME,true);
        String transactionAsJsonFromDb =
                dbConnectorService.getObjectFromDbByHash(transactionAsObjectAndJsonString.getHash(), TRANSACTION_INDEX_NAME,true);
        Assert.assertNotNull(transactionAsJsonFromDb);
    }

    @Test
    public void testAddresses() throws IOException {
        AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
        dbConnectorService.insertObjectToDb(addressAsObjectAndJsonString.getHash(),
                addressAsObjectAndJsonString.getAddressAsJsonString(),
                ADDRESS_INDEX_NAME,
                ADDRESS_OBJECT_NAME,true);
        String addressAsJsonFromDb = dbConnectorService.getObjectFromDbByHash(addressAsObjectAndJsonString.getHash(), ADDRESS_INDEX_NAME,true);
        Assert.assertNotNull(addressAsJsonFromDb);
    }

    @Test
    public void insertAndGetMultiObjects() throws Exception {
        Map<Hash, String> hashToObjectJsonDataMap = insertAddressBulk();
        Map<Hash, String> hashToObjectsFromDbMap = getMultiObjectsFromDb(ADDRESS_INDEX_NAME, new ArrayList<>(hashToObjectJsonDataMap.keySet()));
        Assert.assertTrue(insertedObjectsEqualToObjectsFromDb(hashToObjectJsonDataMap, hashToObjectsFromDbMap));
    }

    @Test
    public void deleteAddressByHash_hashNotExist() {
        String status = dbConnectorService.deleteObject(generateRandomHash(), ADDRESS_INDEX_NAME, true);
        Assert.assertTrue(status.equals(STATUS_NOT_FOUND));
    }

    @Test
    public void deleteAddressByHash_hashExist() throws IOException {
        AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
        dbConnectorService.insertObjectToDb(addressAsObjectAndJsonString.getHash(),
                addressAsObjectAndJsonString.getAddressAsJsonString(),
                ADDRESS_INDEX_NAME,
                ADDRESS_OBJECT_NAME, true);
        String status = dbConnectorService.deleteObject(addressAsObjectAndJsonString.getHash(), ADDRESS_INDEX_NAME, true);
        Assert.assertTrue(status.equals(STATUS_OK));
    }

    private boolean insertedObjectsEqualToObjectsFromDb(Map<Hash, String> hashToObjectJsonDataMap, Map<Hash, String> hashToObjectsFromDbMap) {
        return hashToObjectJsonDataMap.size() == hashToObjectsFromDbMap.size();
    }

    private Map<Hash, String> getMultiObjectsFromDb(String indexName, List<Hash> hashes) throws Exception {
        return dbConnectorService.getMultiObjects(hashes, indexName,true);
    }

    private Map<Hash, String> insertAddressBulk() throws Exception {
        Map<Hash, String> hashToObjectJsonDataMap = new HashMap<>();
        for (int i = 0; i < NUMBER_OF_OBJECTS; i++) {
            AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
            hashToObjectJsonDataMap.put(addressAsObjectAndJsonString.getHash(), addressAsObjectAndJsonString.getAddressAsJsonString());
        }
        dbConnectorService.insertMultiObjectsToDb(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME, hashToObjectJsonDataMap,true);
        return hashToObjectJsonDataMap;
    }

    private TransactionAsObjectAndJsonString getRandomTransactionAsObjectAndJsonString() throws JsonProcessingException {
        TransactionData transactionData = createRandomTransaction();
        ObjectMapper mapper = new ObjectMapper();
        return new TransactionAsObjectAndJsonString(transactionData.getHash(), transactionData, mapper.writeValueAsString(transactionData));
    }

    private AddressAsObjectAndJsonString getRandomAddressAsObjectAndJsonString() throws JsonProcessingException {
        AddressData addressData = new AddressData(generateRandomHash());
        ObjectMapper mapper = new ObjectMapper();
        return new AddressAsObjectAndJsonString(addressData.getHash(), addressData, mapper.writeValueAsString(addressData));
    }

}