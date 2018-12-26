package io.coti.fullnode.services;

import io.coti.basenode.config.WebShutDown;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(classes = {InitializationService.class,
        BalanceService.class,
        TransactionHelper.class,
        WebSocketSender.class
})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class InitializationServiceTest {

    @MockBean
    private io.coti.basenode.services.LiveView.LiveViewService liveViewService;
    @MockBean
    private BaseNodeInitializationService baseNodeInitializationService;
    @MockBean
    private CommunicationService communicationService;

    @MockBean
    private BaseNodeConfirmationService baseNodeConfirmationService;

    @MockBean
    private InitializationService initializationService;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private TransactionHelper transactionHelper;

    @MockBean
    private WebSocketSender webSocketSender;

    @MockBean
    private org.springframework.messaging.simp.SimpMessagingTemplate SimpMessagingTemplate;

    @MockBean
    private io.coti.fullnode.services.TransactionService TransactionService;

    @MockBean
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;

    @MockBean
    private TransactionIndexes transactionIndexes;

    @MockBean
    private Transactions transactions;

    @MockBean
    private DspConsensusCrypto dspConsensusCrypto;

    @MockBean
    private TransactionIndexService transactionIndexService;

    @MockBean
    private WebShutDown webShutDown;

    @Test
    public void init() {
        initializationService.init();
    }

}
