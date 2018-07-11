import io.coti.zero_spend.ZeroSpendConfiguration;
import io.coti.zero_spend.controllers.GetGenesisTransactions;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ZeroSpendConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ZeroSpendControllersTests {


    @Autowired
    private GetGenesisTransactions genesisTransactions;
/*
    @Test
    public void aTestZeroSpendGetGenessisController() {
        ResponseEntity<List<TransactionData>> genesisTransactionsResponse = genesisTransactions.getGenesisTransactions();
        List<TransactionData> zeroSpendTransactions = genesisTransactionsResponse.getBody();
        Assert.assertTrue(genesisTransactionsResponse.getStatusCode() == HttpStatus.OK);
        Assert.assertFalse(zeroSpendTransactions.isEmpty());
        Assert.assertTrue(zeroSpendTransactions.size() == 11);

    }
*/
//    public void bTest


}
