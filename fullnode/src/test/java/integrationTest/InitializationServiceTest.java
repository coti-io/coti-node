package integrationTest;

import io.coti.common.database.RocksDBConnector;
import io.coti.common.model.Transactions;
import io.coti.common.services.BalanceService;
import io.coti.common.services.ClusterService;
import io.coti.common.services.InitializationService;
import io.coti.common.services.LiveView.LiveViewService;
import io.coti.common.services.LiveView.WebSocketSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@ContextConfiguration()
@TestPropertySource(locations = "../fullnode1.properties")
@SpringBootTest (classes = {InitializationService.class,
        Transactions.class,
        BalanceService.class,
        ClusterService.class,
        LiveViewService.class,
        RocksDBConnector.class,
        WebSocketSender.class})
public class InitializationServiceTest {

    @Autowired
    private InitializationService initializationService;

    @Test
    public void testInit() {
        InitializationService initializationService2 = initializationService;
    }

}
