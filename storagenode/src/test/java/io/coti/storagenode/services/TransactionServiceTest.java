package io.coti.storagenode.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@ContextConfiguration(classes = {TransactionService.class, DbConnectorService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionServiceTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DbConnectorService dbConnectorService;

    @Test
    public void transactionTest() throws IOException {
//        TransactionData transactionData = createRandomTransaction();
//        ObjectMapper mapper = new ObjectMapper();
//        String transactionAsJson = mapper.writeValueAsString(transactionData);
//        transactionService.insertTransactionJson(transactionData.getHash(), transactionAsJson);
//        GetObjectJsonResponse response = (GetObjectJsonResponse) transactionService.getTransactionByHash(transactionData.getHash()).getBody();
//        Assert.assertTrue(response.getStatus().equals(STATUS_SUCCESS));
    }
}