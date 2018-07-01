package io.coti.cotinode;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.ConfirmedTransactions;
import io.coti.cotinode.model.UnconfirmedTransactions;
import io.coti.cotinode.service.BalanceService;
import io.coti.cotinode.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
public class BalanceServiceTests {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UnconfirmedTransactions unconfirmedTransactions;

    @Autowired
    private ConfirmedTransactions confirmedTransactions;

    @Autowired
    private QueueService queueService;


    @Test
    public void AInitTest() { // the name starts with a to check make sure it runs first
    /*

    here we can check only the snapshot
     */

        Assert.assertTrue(balanceService.getBalanceMap().get(new Hash("BE")) == new BigDecimal(120.0));
        Assert.assertTrue(balanceService.getPreBalanceMap().get(new Hash("BE")) == new BigDecimal(120.0));


    }

    @Test
    public void checkBalancesTest() {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(new BaseTransactionData("BE", new BigDecimal(-150)));

        boolean ans = balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
        Assert.assertFalse(ans);


        List<BaseTransactionData> baseTransactionDatas2 = new LinkedList<>();
        baseTransactionDatas2.add(new BaseTransactionData("BE", new BigDecimal(-20)));
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
        transaction.setAddressHashToValueTransferredMapping(addressToAmount);
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


