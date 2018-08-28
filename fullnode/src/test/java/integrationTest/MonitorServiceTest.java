package integrationTest;

import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.TransactionIndexes;
import io.coti.common.model.Transactions;
import io.coti.common.services.InitializationService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.MonitorService;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.TransactionIndexService;
import lombok.extern.slf4j.Slf4j;
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

public class MonitorServiceTest {
    @Autowired
    private InitializationService initializationService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private TransactionIndexService transactionIndexService;

    @MockBean
    private WebSocketSender webSocketSender;

    @MockBean
    private AddressesTransactionsHistory addressesTransactionsHistory;

    @MockBean
    private TransactionHelper transactionHelper;

    @MockBean
    private TransactionIndexes transactionIndexes;

    @MockBean
    private Transactions transactions;

    @Test
    public void lastState() {
        //uncomment it, when columnFamilyClassNames will contain RocksDBConnector.TransactionIndexes
        //monitorService.lastState();
    }

}
