package unitTest;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.fullnode.services.WebSocketSender;
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

@TestPropertySource(locations = "../test.properties")
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
            webSocketSender.notifyBalanceChange(generateRandomHash(64),
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
                    TestUtils.createTransactionWithSpecificHash(generateRandomHash(64));
            webSocketSender.notifyTransactionHistoryChange(transactionData,
                    TransactionStatus.CONFIRMED);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void notifyGeneratedAddress_noExceptionIsThrown() {
        try {
            webSocketSender.notifyGeneratedAddress(generateRandomHash(64));
        } catch (Exception e) {
            assertNull(e);
        }
    }


}
