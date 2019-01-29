package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.TransactionData;
import javafx.util.Pair;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static testUtils.TestUtils.createRandomTransaction;

//@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class ClientServiceTest {
    private String TRANSACTIONS_INDEX_NAME = "transactions";
    private String TRANSACTIONS_OBJECT_NAME = "transactionData";
    private String ADDRESSES_INDEX_NAME = "addresesData";
    private String ADDRESSES_OBJECT_NAME = "transactionData";

    private Map<String, String> indexes;

    @Autowired
    private ClientService clientService;

    @Before
    public void setUp() throws Exception {
        indexes = new HashMap<>();
        indexes.put(TRANSACTIONS_INDEX_NAME, TRANSACTIONS_OBJECT_NAME);
        clientService.init(indexes);
    }

    @Test
    public void test() throws IOException {
        //clientService.getClusterDetails(indexes.);
        TransactionData transactionData = createRandomTransaction();
        ObjectMapper mapper = new ObjectMapper();
        String transactionAsJson = mapper.writeValueAsString(transactionData) ;
        clientService.insertTransaction(transactionData.getHash(), transactionAsJson, TRANSACTIONS_INDEX_NAME,  TRANSACTIONS_OBJECT_NAME );
        String transactionAsJson2 = clientService.getTransactionByHash(transactionData.getHash(), TRANSACTIONS_INDEX_NAME);
    }
}