package io.coti.historynode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.historynode.data.ObjectDocument;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
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

import static testUtils.TestUtils.createRandomTransaction;
import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = ClientService.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class ClientServiceTest {
    private final static String TRANSACTION_INDEX_NAME = "transactions";
    private final static String TRANSACTION_OBJECT_NAME = "transactionData";
    private final static String ADDRESS_INDEX_NAME = "address";
    private final static String ADDRESS_OBJECT_NAME = "addressData";
    private final static int NUMBER_OF_OBJECTS = 6;

    private Map<String, String> indexes;

    @Autowired
    private ClientService clientService;

    @Before
    public void setUp() throws Exception {
        indexes = new HashMap<>();
        indexes.put(TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME);
        indexes.put(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        clientService.addIndexes(indexes);
    }

    @Test
    public void getClusterDetails() throws IOException {
        clientService.getClusterDetails(indexes.keySet());
    }

    @Test
    public void testTransactions() throws IOException {
        TransactionAsObjectAndJsonString transactionAsObjectAndJsonString = getRandomTransactionAsObjectAndJsonString();
        clientService.insertObjectToDb(transactionAsObjectAndJsonString.getHash(),
                transactionAsObjectAndJsonString.getTransactionAsJsonString(),
                TRANSACTION_INDEX_NAME,
                TRANSACTION_OBJECT_NAME);
        String transactionAsJsonFromDb = clientService.getObjectFromDbByHash(transactionAsObjectAndJsonString.getHash(), TRANSACTION_INDEX_NAME);
        Assert.assertNotNull(transactionAsJsonFromDb);
    }

    @Test
    public void testAddresses() throws IOException {
        AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
        clientService.insertObjectToDb(addressAsObjectAndJsonString.getHash(),
                addressAsObjectAndJsonString.getAddressAsJsonString(),
                ADDRESS_INDEX_NAME,
                ADDRESS_OBJECT_NAME);
        String addressAsJsonFromDb = clientService.getObjectFromDbByHash(addressAsObjectAndJsonString.getHash(), ADDRESS_INDEX_NAME);
        Assert.assertNotNull(addressAsJsonFromDb);
    }

    @Test
    public void insertAndGetMultiObjects() throws IOException {
        List<ObjectDocument> insertedObjectDocuments = insertBulk();
        Map<Hash, String> hashToObjectsFromDbMap = getMultiObjects(insertedObjectDocuments);
        Assert.assertTrue(insertedObjectsEqualToObjectsFromDb(insertedObjectDocuments, hashToObjectsFromDbMap));
    }

    private boolean insertedObjectsEqualToObjectsFromDb(List<ObjectDocument> insertedObjectDocuments, Map<Hash, String> hashToObjectsFromDbMap) {
        if (insertedObjectDocuments.size() != hashToObjectsFromDbMap.size()) {
            return false;
        }
        return true;
    }

    private Map<Hash, String> getMultiObjects(List<ObjectDocument> insertedObjectDocuments) {
        Map<Hash, String> hashToObjectsFromDbMap = null;
        Map<Hash, String> hashAndIndexNameMap = new HashMap<>();
        for (ObjectDocument objectDocument : insertedObjectDocuments) {
            hashAndIndexNameMap.put(objectDocument.getHash(), objectDocument.getIndexName());
        }
        MultiGetResponse multiGetResponse = clientService.getMultiObjectsFromDb(hashAndIndexNameMap);
        hashToObjectsFromDbMap = new HashMap<>();
        for (MultiGetItemResponse multiGetItemResponse : multiGetResponse.getResponses()) {
            hashToObjectsFromDbMap.put(new Hash(multiGetItemResponse.getId()),
                    new String(multiGetItemResponse.getResponse().getSourceAsBytes()));
        }
        return hashToObjectsFromDbMap;
    }

    private List<ObjectDocument> insertBulk() throws IOException {
        List<ObjectDocument> objectDocuments = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_OBJECTS; i++) {
            if (i % 2 == 0) {
                TransactionAsObjectAndJsonString transactionAsObjectAndJsonString = getRandomTransactionAsObjectAndJsonString();
                objectDocuments.add(new ObjectDocument(TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME, transactionAsObjectAndJsonString.getHash(), transactionAsObjectAndJsonString.getTransactionAsJsonString()));
            } else {
                AddressAsObjectAndJsonString addressAsObjectAndJsonString = getRandomAddressAsObjectAndJsonString();
                objectDocuments.add(new ObjectDocument(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME, addressAsObjectAndJsonString.getHash(), addressAsObjectAndJsonString.getAddressAsJsonString()));
            }
        }
        clientService.insertMultiObjectsToDb(objectDocuments);
        return objectDocuments;
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