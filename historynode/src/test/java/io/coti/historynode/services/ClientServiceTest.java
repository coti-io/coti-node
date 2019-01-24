package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.TransactionData;
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

import static testUtils.TestUtils.createRandomTransaction;

//@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class ClientServiceTest {

    @Autowired
    private ClientService clientService;

    @Before
    public void setUp() throws Exception {
        clientService.init();
    }

    @Test
    public void test() throws IOException {
        clientService.getClusterDetails();
        TransactionData transactionData = createRandomTransaction();
        ObjectMapper mapper = new ObjectMapper();
        String transactionAsJson = mapper.writeValueAsString(transactionData );
        clientService.insertTransaction(transactionData.getHash(), transactionAsJson );
        String transactionAsJson2 = clientService.getTransactionByHash(transactionData.getHash());
    }
}