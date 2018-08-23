package integrationTest;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.model.TransactionIndexes;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

@ContextConfiguration(classes = {Transactions.class,
        TransactionIndexes.class,
        TransactionIndexService.class,
})
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "../test.properties")
@Slf4j
public class TransactionIndexServiceTest {


    TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(new Hash("AA"));
}
