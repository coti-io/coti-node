package unitTest;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.services.LiveView.WebSocketSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.math.BigDecimal;

import static org.junit.Assert.assertNull;

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
            webSocketSender.notifyBalanceChange(new Hash("AB"),
                    new BigDecimal("56.345"),
                    new BigDecimal("60.12345"));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void notifyTransactionHistoryChange_noExceptionIsThrown() {
        try {
            TransactionData transactionData =
                    TestUtils.createTransactionWithSpecificHash(new Hash("CD"));
            webSocketSender.notifyTransactionHistoryChange(transactionData,
                    TransactionStatus.CONFIRMED);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void notifyGeneratedAddress_noExceptionIsThrown() {
        try {
            webSocketSender.notifyGeneratedAddress(new Hash("EF"));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void sendNode_noExceptionIsThrown() {
        try {
            webSocketSender.sendNode(new NodeData());
        } catch (Exception e) {
            assertNull(e);
        }
    }
}
