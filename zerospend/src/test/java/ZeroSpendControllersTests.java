import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetZeroSpendTransactionsRequest;
import io.coti.zero_spend.ZeroSpendConfiguration;
import io.coti.zero_spend.controllers.AddTransactionController;
import io.coti.zero_spend.controllers.GetZeroSpendTransactionController;
import io.coti.zero_spend.http.AddTransactionRequest;
import io.coti.zero_spend.services.AddTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ZeroSpendConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ZeroSpendControllersTests {


    @Autowired
    private GetZeroSpendTransactionController getZeroSpendTransactionController;

    @Value("${zerospend.request.limit}")
    private int requestLimit;

    @Autowired
    private AddTransactionController addTransactionController;

    @Autowired
    private AddTransactionService addTransactionService;


    @Test
    public void aAddTransactionTest() {

        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
//    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription,double senderTrustScore) {

        Hash transactionHash = new Hash("AAAA");
        TransactionData transactionData = new TransactionData(new LinkedList<>(), transactionHash, "testTransaction", 60);
        addTransactionRequest.setTransactionData(transactionData);
        addTransactionController.addTransaction(addTransactionRequest);
        Assert.assertTrue(addTransactionService.getTransactionDataMap().containsKey(transactionHash));

    }

    @Test
    public void bGetZeroSpendTransactionTest() {
        Hash cotiNodeHash = new Hash("BBBB");
        GetZeroSpendTransactionsRequest getZeroSpendTransactionsRequest = new GetZeroSpendTransactionsRequest();
        getZeroSpendTransactionsRequest.setFullNodeHash(cotiNodeHash);
        TransactionData transactionData = new TransactionData(new LinkedList<>(), cotiNodeHash, "testTransaction", 60);
        getZeroSpendTransactionsRequest.setTransactionData(transactionData);
        ResponseEntity<TransactionData> getZeroSpendTransactionResponse = null;
        for (int i = 0; i <= requestLimit + 1; i++) {
            getZeroSpendTransactionResponse =
                    getZeroSpendTransactionController.getZeroSpendTransaction(getZeroSpendTransactionsRequest);

        }
        Assert.assertTrue(getZeroSpendTransactionResponse.getStatusCode() == HttpStatus.METHOD_NOT_ALLOWED);

    }

//    public void bTest


}
