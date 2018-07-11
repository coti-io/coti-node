package io.coti.fullnode.controllers;

import io.coti.common.data.Hash;
import io.coti.common.http.GetTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = io.coti.fullnode.AppConfig.class)
@Slf4j
public class PropagationControllerTest {
    @Autowired
    private PropagationController propagationController;

//    @Autowired
////    private TransactionController transactionController;

    @Test
    public void getTransactionFromCurrentNode() {
        GetTransactionRequest getTransactionRequest = new GetTransactionRequest();
        getTransactionRequest.transactionHash = new Hash("01");
        // propagationController.getTransactionFromCurrentNode(getTransactionRequest);
        String url = "https://localhost:8080/propagatedTransaction";
        url = "https://localhost:8080//transaction";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForLocation(url, getTransactionRequest);
    }
}