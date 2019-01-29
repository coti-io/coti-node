package io.coti.historynode.services;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static testUtils.TestUtils.createRandomTransaction;
import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class ClientServiceTest {
    private String TRANSACTION_INDEX_NAME = "transactions";
    private String TRANSACTION_OBJECT_NAME = "transactionData";
    private String ADDRESS_INDEX_NAME = "address";
    private String ADDRESS_OBJECT_NAME = "addressData";

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
    public void getClusterDetails() throws IOException{
        clientService.getClusterDetails(indexes.keySet());
    }

    @Test
    public void testTransactions() throws IOException {
        TransactionData transactionData = createRandomTransaction();
        ObjectMapper mapper = new ObjectMapper();
        String transactionAsJson = mapper.writeValueAsString(transactionData) ;
        clientService.insertObject(transactionData.getHash(), transactionAsJson, TRANSACTION_INDEX_NAME,  TRANSACTION_OBJECT_NAME );
        String transactionAsJsonFromDb = clientService.getObjectByHash(transactionData.getHash(), TRANSACTION_INDEX_NAME);
        Assert.assertNotNull(transactionAsJsonFromDb );
    }

    @Test
    public void testAddresses() throws IOException {
        AddressData addressData = new AddressData(generateRandomHash());
        ObjectMapper mapper = new ObjectMapper();
        String addressAsJson = mapper.writeValueAsString(addressData) ;
        clientService.insertObject(addressData.getHash(), addressAsJson, ADDRESS_INDEX_NAME,  ADDRESS_OBJECT_NAME );
        String addressAsJsonFromDb = clientService.getObjectByHash(addressData.getHash(), ADDRESS_INDEX_NAME);
        Assert.assertNotNull(addressAsJsonFromDb);
    }
}