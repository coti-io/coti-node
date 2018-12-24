package io.coti.fullnode.services;

import io.coti.basenode.config.WebShutDown;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.*;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.fullnode.services.BalanceService;
import io.coti.fullnode.services.InitializationService;
import io.coti.fullnode.services.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

//
@ContextConfiguration(classes = {InitializationService.class,
        BalanceService.class,
        TransactionHelper.class,
        WebSocketSender.class
})
//@RunWith(SpringRunner.class)
//@TestPropertySource(locations = "classpath:test.properties")
//@Slf4j
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
    //@Autowired
    @MockBean
    private InitializationService initializationService;

    //@Autowired
    @MockBean
    private BalanceService balanceService;

    //@Autowired
    @MockBean
    private TransactionHelper transactionHelper;

    //@Autowired
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
    public void testInit() {
        initializationService.init();
    }

}
