package unitTest;

import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.services.BaseNodeBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseNodeBalanceService.class,
        RocksDBConnector.class}
)
@Slf4j
public class BaseNodeBalanceServiceTest {
    private static boolean setUpIsDone = false;

    @Autowired
    private BaseNodeBalanceService baseNodeBalanceService;

    @Autowired
    private IDatabaseConnector rocksDBConnector;

    @Before
    public void setUp() throws Exception {
        if (setUpIsDone) {
            return;
        }
        rocksDBConnector.init();
        baseNodeBalanceService.init();
        setUpIsDone = true;
    }


    @Test
    public void checkBalancesAndAddToPreBalance() {
    }

    @Test
    public void continueHandleBalanceChanges() {
    }

    @Test
    public void getBalances() {
    }

    @Test
    public void rollbackBaseTransactions() {
    }

    @Test
    public void validateBalances() {
    }

    @Test
    public void updateBalance() {
    }

    @Test
    public void updatePreBalance() {
    }
}
