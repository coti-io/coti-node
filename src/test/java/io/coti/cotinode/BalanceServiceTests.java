package io.coti.cotinode;

import io.coti.cotinode.data.ConfirmedTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.UnconfirmedTransactionData;
import io.coti.cotinode.database.RocksDBConnector;
import io.coti.cotinode.model.ConfirmedTransactions;
import io.coti.cotinode.model.UnconfirmedTransactions;
import io.coti.cotinode.service.BalanceService;
import io.coti.cotinode.service.QueueService;
import io.coti.cotinode.service.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.data.MapEntry;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.text.html.parser.Entity;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
public class BalanceServiceTests {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private RocksDBConnector dbConnector;

    @Autowired
    private UnconfirmedTransactions unconfirmedTransactions;

    @Autowired
    private ConfirmedTransactions confirmedTransactions;

    @Autowired
    private QueueService queueService;

        @Test
        public  void AInitTest(){ // the name starts with a to check make sure it runs first
        /*

        here we can check only the snapshot
         */


//        UnconfirmedTransactionData unconfirmedTransactionData = new UnconfirmedTransactionData(new Hash("T1"));
//        Map<Hash,Double> hashToDouble = new HashMap<>();
//        hashToDouble.put(new Hash("BEN"),20.0);
//        unconfirmedTransactionData.setAddressHashToValueTransferredMapping(hashToDouble);
//        unconfirmedTransactions.put(unconfirmedTransactionData);

            Assert.assertTrue(balanceService.getBalanceMap().get(new Hash("BEN")) == 120.0);
            Assert.assertTrue(balanceService.getPreBalanceMap().get(new Hash("BEN")) == 120.0);


        }
//    @Test
//    public void getBalances_EmptyBalancesList_ReturnsEmptyList() {
//        List<BaseTransactionData> balances = balanceService.getBalances(new ArrayList<>());
//        Assert.assertTrue(balances.equals(new ArrayList<>()));
//    }
/*
      we know that the snapshot consists :
        BEN,120
        MOSES,200


 */
    @Test
    public void checkBalancesTest(){
        List<Entry<Hash,Double>> pairList = new LinkedList<>();
        pairList.add(new AbstractMap.SimpleEntry<Hash, Double>(new Hash("BEN"), -150.0));

        boolean ans = balanceService.checkBalancesAndAddToPreBalance(pairList);
        Assert.assertFalse(ans);

        List<Entry<Hash,Double>> pairList2 = new LinkedList<>();
        pairList2.add(new AbstractMap.SimpleEntry<Hash, Double>(new Hash("BEN"), -20.0));
        ans = balanceService.checkBalancesAndAddToPreBalance(pairList2);
        Assert.assertTrue(ans);


        Assert.assertTrue(balanceService.getPreBalanceMap().get(new Hash("BEN")) == 100.0);


    }


   // @Test
    public void syncBalanceScheduledTest(){


        try {
            UnconfirmedTransactionData unconfirmedTransactionData1 = new UnconfirmedTransactionData(new Hash("T1")); //tcc =0 , dspc =0
            unconfirmedTransactions.put(unconfirmedTransactionData1);
            queueService.addToUpdateBalanceQueue(new Hash("T1"));


            TimeUnit.SECONDS.sleep(3); //wait for the scheduled task to end
            UnconfirmedTransactionData unconfirmedTransactionData = unconfirmedTransactions.getByHash(new Hash("T1"));
            Assert.assertTrue( unconfirmedTransactionData.isTrustChainConsensus());


            UnconfirmedTransactionData unconfirmedTransactionData2 = new UnconfirmedTransactionData(new Hash("T2")); //tcc =0 , dspc =0
            unconfirmedTransactionData2.setDoubleSpendPreventionConsensus(true);
            unconfirmedTransactions.put(unconfirmedTransactionData2);

            queueService.addToUpdateBalanceQueue(new Hash("T2"));
            TimeUnit.SECONDS.sleep(3); //wait for the scheduled task to end
            unconfirmedTransactionData = unconfirmedTransactions.getByHash(new Hash("T2"));
            Assert.assertNull( unconfirmedTransactionData);
            ConfirmedTransactionData confirmedTransactionData = confirmedTransactions.getByHash(new Hash("T2"));
            Assert.assertNotNull(confirmedTransactionData);

        } catch (InterruptedException e) {
            log.error("Error , {}",e);
        }


    }


}
