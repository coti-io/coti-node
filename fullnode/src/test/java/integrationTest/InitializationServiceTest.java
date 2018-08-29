package integrationTest;

import io.coti.common.crypto.DspConsensusCrypto;
import io.coti.common.crypto.TransactionTrustScoreCrypto;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.TransactionIndexes;
import io.coti.common.model.Transactions;
import io.coti.common.services.BalanceService;
import io.coti.common.services.InitializationService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.TransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@ContextConfiguration(classes = IntegrationServiceTestsAppConfig.class)
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "../test.properties")
@Slf4j
public class InitializationServiceTest {

    @Before
    public void init() {

//        org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getRootLogger();
//        logger4j.setLevel(org.apache.log4j.Level.toLevel("ERROR"));
    }
    @Autowired
    private InitializationService initializationService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private WebSocketSender webSocketSender;

    @MockBean
    private org.springframework.messaging.simp.SimpMessagingTemplate SimpMessagingTemplate;

    @MockBean
    private io.coti.common.services.TransactionService TransactionService;

    @MockBean
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;

    @MockBean
    private TransactionIndexes transactionIndexes;

    @MockBean
    private AddressesTransactionsHistory addressesTransactionsHistory;

    @MockBean
    private Transactions transactions;

    @MockBean
    private DspConsensusCrypto dspConsensusCrypto;

    @MockBean
    private TransactionIndexService transactionIndexService;


    @Test
    public void testInit() {

    }

}
