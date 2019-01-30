package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.TransactionData;
import io.coti.historynode.http.GetObjectJsonResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static testUtils.TestUtils.createRandomTransaction;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionServiceTest {
    @Autowired
    private TransactionService transactionService;

    @Test
    public void transactionTest() throws IOException {
        TransactionData transactionData = createRandomTransaction();
        ObjectMapper mapper = new ObjectMapper();
        String transactionAsJson = mapper.writeValueAsString(transactionData);
        transactionService.insertTransactionJson(transactionData.getHash(), transactionAsJson);
        GetObjectJsonResponse response = (GetObjectJsonResponse) transactionService.getTransactionByHash(transactionData.getHash()).getBody();
        Assert.assertTrue(response.getStatus().equals(STATUS_SUCCESS));
    }
}