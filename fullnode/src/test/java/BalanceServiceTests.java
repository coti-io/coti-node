import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.ConfirmationData;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.model.Transactions;
import io.coti.common.services.BalanceService;
import io.coti.common.services.InitializationService;
import io.coti.common.services.LiveView.LiveViewService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.TransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)

@ContextConfiguration(classes = BalanceServiceTestsAppConfig.class)
@TestPropertySource(locations = "../fullnode1.properties")
@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)

public class BalanceServiceTests {

    @MockBean
    TransactionHelper transactionHelper;

    @MockBean
    InitializationService initializationService;

    @MockBean
    WebSocketSender webSocketSender;

    @MockBean
    private LiveViewService liveViewService;

    @MockBean
    private Transactions transactions;



    @Autowired
    private BalanceService balanceService;

    @Test
    public void AInitTest() { // the name starts with a to check make sure it runs first
    /*

    here we can check only the snapshot
     */
        BigDecimal addressBalance1 = balanceService.getBalanceMap().
                get(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"));
        Assert.assertTrue(addressBalance1.compareTo(new BigDecimal("3941622.610838615")) == 0);

        BigDecimal addressBalance2 = balanceService.getBalanceMap().
                get(new Hash("5e6b6af708ae15c1c55641f9e87e71f5cd58fc71aa58ae55abe9d5aa88b2ad3c5295cbffcfbb3a087e8da72596d7c60eebb4c59748cc1906b2aa67be43ec3eb147c1a19a"));
        Assert.assertFalse(addressBalance2.compareTo(new BigDecimal("3941622.610838615")) == 0);

        int temp=0;
    }

    @Test
    public void checkBalancesTest() {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.
                add(new BaseTransactionData(new Hash("07ffe1f66fcfbd4adb004c0dde1414b62d604bc8a09caefe04ca085a6a0890dd9127f41238b76208b0c88b8ea6e05f4e848a4e9f431060cd53fdeebf6e6cf994a838e46e"),
                new BigDecimal(-150),
                        new Hash("BE"),
                        new SignatureData("", ""),
                        new Date()));

        boolean ans = balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
        Assert.assertFalse(ans);


        List<BaseTransactionData> baseTransactionDatas2 = new LinkedList<>();
        baseTransactionDatas2.add(new BaseTransactionData(new Hash("BE"), new BigDecimal(-20), new Hash("BE"), new SignatureData("", ""), new Date()));
        ans = balanceService.checkBalancesAndAddToPreBalance(baseTransactionDatas2);
        Assert.assertTrue(ans);

//Big decimals should be compared with compareTo and not equals
        Assert.assertTrue(balanceService.getPreBalanceMap().get(new Hash("BE"))
                .compareTo(new BigDecimal(100)) == 0);


    }

//    @Test // this method checks ConfirmationData.equals() as well
//    public void insertIntoUnconfirmedDBandAddToTccQeueueTest() {
//        ConfirmationData confirmationData1 = new ConfirmationData(new Hash("A3")); //tcc =0 , dspc =0
//        populateTransactionWithDummy(confirmationData1);
//        balanceService.insertToUnconfirmedTransactions(confirmationData1);
//        ConfirmationData confirmationData  = unconfirmedTransactions.getByHash(new Hash("A3"));
//        Assert.assertTrue(queueService.getTccQueue().contains(confirmationData.getHash()));
//
//    }

    private void populateTransactionWithDummy(ConfirmationData transaction) {
        Map<Hash, BigDecimal> addressToAmount = new HashMap<>();
        addressToAmount.put(new Hash("DD"), new BigDecimal(10.1));
//        transaction.setAddressHashToValueTransferredMapping(addressToAmount);
    }

    @After
    public void tearDown() {
    }

//    @Test
//    public void syncBalanceScheduledTest() {
//
//
//        try {
//            ConfirmationData confirmationData1 = new ConfirmationData(new Hash("A1")); //tcc =0 , dspc =0
//            populateTransactionWithDummy(confirmationData1);
//            unconfirmedTransactions.put(confirmationData1);
//            queueService.addToUpdateBalanceQueue(new Hash("A1"));
//
//
//            TimeUnit.SECONDS.sleep(5); //wait for the scheduled task to end
//            ConfirmationData confirmationData = unconfirmedTransactions.getByHash(new Hash("A1"));
//            Assert.assertTrue(confirmationData.isTrustChainConsensus());
//
//
//            ConfirmationData confirmationData2 = new ConfirmationData(new Hash("A2")); //tcc =0 , dspc =0
//            populateTransactionWithDummy(confirmationData2);
//            confirmationData2.setDoubleSpendPreventionConsensus(true);
//            unconfirmedTransactions.put(confirmationData2);
//
//            queueService.addToUpdateBalanceQueue(new Hash("A2"));
//            TimeUnit.SECONDS.sleep(5); //wait for the scheduled task to end
//            confirmationData = unconfirmedTransactions.getByHash(new Hash("A2"));
//            Assert.assertNull(confirmationData);
//            ConfirmationData confirmedTransactionData = confirmedTransactions.getByHash(new Hash("A2"));
//            populateTransactionWithDummy(confirmedTransactionData);
//            Assert.assertNotNull(confirmedTransactionData);
//
//        } catch (InterruptedException e) {
//            log.error("Error , {}", e);
//        }
//    }
}


