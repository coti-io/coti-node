package integrationTest;

import io.coti.common.crypto.DspConsensusCrypto;
import io.coti.common.crypto.TransactionTrustScoreCrypto;
import io.coti.common.database.RocksDBConnector;
import io.coti.common.model.*;
import io.coti.common.services.*;
import io.coti.common.services.LiveView.LiveViewService;
import io.coti.common.services.LiveView.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@ContextConfiguration(classes = {Transactions.class,
        Addresses.class,
        RocksDBConnector.class,
        AddressesTransactionsHistory.class,
        TrustScores.class,
        TransactionIndexes.class,
        TransactionVotes.class,
        InitializationService.class,
        TransactionIndexService.class,
        BalanceService.class,
        WebSocketSender.class,
        LiveViewService.class,
        TransactionHelper.class,
        ClusterService.class,
        SourceSelector.class,
        TccConfirmationService.class,
        DspConsensusCrypto.class,
        TransactionTrustScoreCrypto.class,
        MonitorService.class,
        WebSocketSender.class
})
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

    @MockBean
    private WebSocketSender webSocketSender;

    @Test
    public void testInit() {

    }

}
