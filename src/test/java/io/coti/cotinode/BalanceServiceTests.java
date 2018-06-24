package io.coti.cotinode;

import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.interfaces.IEntity;
import io.coti.cotinode.database.RocksDBConnector;
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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

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


    @BeforeClass
    public static void deleteRocksDBfolder() {

        File index = new File("rocksDB");
        if (!index.exists()) {
            return;
        }
        String[] entries = index.list();
        for (String s : entries) {
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
        index.delete();


    }

    @Test
    public void AInitTest() { // the name starts with a to check make sure it runs first
    /*

    here we can check only the snapshot
     */

        Assert.assertTrue(balanceService.getBalanceMap().get(new Hash("BEN")) == 120.0);
        Assert.assertTrue(balanceService.getPreBalanceMap().get(new Hash("BEN")) == 120.0);


    }

    @Test
    public void checkBalancesTest() {
        List<Entry<Hash, Double>> pairList = new LinkedList<>();
        pairList.add(new AbstractMap.SimpleEntry<Hash, Double>(new Hash("BEN"), -150.0));

        boolean ans = balanceService.checkBalancesAndAddToPreBalance(pairList);
        Assert.assertFalse(ans);

        List<Entry<Hash, Double>> pairList2 = new LinkedList<>();
        pairList2.add(new AbstractMap.SimpleEntry<Hash, Double>(new Hash("BEN"), -20.0));
        ans = balanceService.checkBalancesAndAddToPreBalance(pairList2);
        Assert.assertTrue(ans);


        Assert.assertTrue(balanceService.getPreBalanceMap().get(new Hash("BEN")) == 100.0);

    }

    @Test // this method checks ConfirmationData.equals() as well
    public void insertIntoUnconfirmedDBandAddToTccQeueueTest() {
        ConfirmationData confirmationData1 = new ConfirmationData(new Hash("T3")); //tcc =0 , dspc =0
        populateTransactionWithDummy(confirmationData1);
        balanceService.insertIntoUnconfirmedDBandAddToTccQeueue(confirmationData1);
        ConfirmationData confirmationData  = unconfirmedTransactions.getByHash(new Hash("T3"));
        Assert.assertTrue(queueService.getTccQueue().contains(confirmationData.getHash()));

    }

    private void populateTransactionWithDummy(ConfirmationData transaction) {
        Map<Hash, Double> addressToAmount = new HashMap<>();
        addressToAmount.put(new Hash("Dummy"), 10.10);
        transaction.setAddressHashToValueTransferredMapping(addressToAmount);
    }

    @Test
    public void syncBalanceScheduledTest() {


        try {
            ConfirmationData confirmationData1 = new ConfirmationData(new Hash("T1")); //tcc =0 , dspc =0
            populateTransactionWithDummy(confirmationData1);
            unconfirmedTransactions.put(confirmationData1);
            queueService.addToUpdateBalanceQueue(new Hash("T1"));


            TimeUnit.SECONDS.sleep(5); //wait for the scheduled task to end
            ConfirmationData confirmationData = unconfirmedTransactions.getByHash(new Hash("T1"));
            Assert.assertTrue(confirmationData.isTrustChainConsensus());


            ConfirmationData confirmationData2 = new ConfirmationData(new Hash("T2")); //tcc =0 , dspc =0
            populateTransactionWithDummy(confirmationData2);
            confirmationData2.setDoubleSpendPreventionConsensus(true);
            unconfirmedTransactions.put(confirmationData2);

            queueService.addToUpdateBalanceQueue(new Hash("T2"));
            TimeUnit.SECONDS.sleep(5); //wait for the scheduled task to end
            confirmationData = unconfirmedTransactions.getByHash(new Hash("T2"));
            Assert.assertNull(confirmationData);
            ConfirmationData confirmedTransactionData = confirmedTransactions.getByHash(new Hash("T2"));
            populateTransactionWithDummy(confirmedTransactionData);
            Assert.assertNotNull(confirmedTransactionData);

        } catch (InterruptedException e) {
            log.error("Error , {}", e);
        }
    }

}
