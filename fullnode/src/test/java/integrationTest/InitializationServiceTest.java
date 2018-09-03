package integrationTest;

import io.coti.basenode.communication.ZeroMQSender;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.AddressesTransactionsHistory;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.*;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.fullnode.services.AddressService;
import io.coti.fullnode.services.BalanceService;
import io.coti.fullnode.services.InitializationService;
import io.coti.fullnode.services.WebSocketSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {InitializationService.class,
        Addresses.class,
        AddressService.class,
        io.coti.basenode.services.BaseNodeInitializationService.class,
        Transactions.class,
        RocksDBConnector.class,
        TransactionIndexService.class,
        TransactionHelper.class,
        AddressesTransactionsHistory.class,
        BalanceService.class,
        LiveViewService.class,
        ClusterService.class,
        WebSocketSender.class,
        SourceSelector.class,
        TccConfirmationService.class,
        TransactionIndexes.class,
        DspConsensusCrypto.class,
        TransactionTrustScoreCrypto.class,
        BaseNodeTransactionService.class,
        ZeroMQSender.class,
        IDspVoteService.class,
        BaseNodeTransactionService.class

})
public class InitializationServiceTest {

    @Autowired
    private InitializationService initializationService;

    @MockBean
    private LiveViewService liveViewService;

    @MockBean
    private io.coti.fullnode.services.WebSocketSender WebSocketSender;

    @MockBean
    private MonitorService monitorService;

    @MockBean
    private BaseNodeTransactionService baseNodeTransactionService;

    @MockBean
    private ZeroMQSender ZeroMQSender;

    @MockBean
    private IDspVoteService dspVoteService;

    @MockBean
    private CommunicationService communicationService;

    @Test
    public void init() {

    }
}