package io.coti.fullnode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.math.BigDecimal;

import static testUtils.TestUtils.generateRandomHash;
import static testUtils.TestUtils.generateRandomPositiveAmount;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WebSocketSender.class})
public class WebSocketSenderTest {

    @Autowired
    private WebSocketSender webSocketSender;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    public void notifyBalanceChange_noExceptionIsThrown() {
        try {
            webSocketSender.notifyBalanceChange(generateRandomHash(),
                    new BigDecimal(generateRandomPositiveAmount()),
                    new BigDecimal(generateRandomPositiveAmount()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void notifyTransactionHistoryChange_noExceptionIsThrown() {
        try {
            TransactionData transactionData =
                    TestUtils.createRandomTransaction(generateRandomHash());
            webSocketSender.notifyTransactionHistoryChange(transactionData,
                    TransactionStatus.CONFIRMED);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void notifyGeneratedAddress_noExceptionIsThrown() {
        try {
            webSocketSender.notifyGeneratedAddress(generateRandomHash());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


}
