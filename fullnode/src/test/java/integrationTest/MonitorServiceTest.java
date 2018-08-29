package integrationTest;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionIndexData;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.TransactionIndexes;
import io.coti.common.model.Transactions;
import io.coti.common.services.*;
import io.coti.common.services.LiveView.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = IntegrationServiceTestsAppConfig.class)
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "../test.properties")
@Slf4j

public class MonitorServiceTest {
    @Autowired
    private InitializationService initializationService;

    @Autowired
    private MonitorService monitorService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
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
                when(transactionIndexService.getLastTransactionIndexData())
                .thenReturn(new TransactionIndexData(new Hash("aa"), 0, null));
        monitorService.lastState();
    }

}
