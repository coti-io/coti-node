package io.coti.fullnode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
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

import static org.junit.Assert.assertNull;
import static testUtils.TestUtils.generateRandomCount;
import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WebSocketSender.class})
public class WebSocketSenderTest {
    private static final int SIZE_OF_HASH = 64;

    @Autowired
    private WebSocketSender webSocketSender;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    public void notifyBalanceChange_noExceptionIsThrown() {
        try {
            webSocketSender.notifyBalanceChange(generateRandomHash(SIZE_OF_HASH),
                    new BigDecimal(generateRandomCount()),
                    new BigDecimal(generateRandomCount()));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void notifyTransactionHistoryChange_noExceptionIsThrown() {
        try {
            TransactionData transactionData =
                    TestUtils.createTransactionWithSpecificHash(generateRandomHash(SIZE_OF_HASH));
            webSocketSender.notifyTransactionHistoryChange(transactionData,
                    TransactionStatus.CONFIRMED);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void notifyGeneratedAddress_noExceptionIsThrown() {
        try {
            webSocketSender.notifyGeneratedAddress(generateRandomHash(SIZE_OF_HASH));
        } catch (Exception e) {
            assertNull(e);
        }
    }


}
