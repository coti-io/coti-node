package io.coti.cotinode;

import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.model.UnconfirmedTransactions;
import io.coti.cotinode.service.interfaces.ICluster;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
public class ClusterServiceTest {

    private static int counter = 0;
    @Autowired
    UnconfirmedTransactions unconfirmedTransactions;
    @Autowired
    Transactions transaction;
    private Random random = new Random();
    @Autowired
    private ICluster cluster;
    private List<TransactionData> newTransactions;
    private List<Hash> notTccConfirmTransactions;

    private void setUpNewTransactions(){

        TransactionData TransactionData2 = new TransactionData(new Hash("22".getBytes()));
        TransactionData2.setSenderTrustScore(92);
        TransactionData2.setCreateTime(new Date());

        TransactionData TransactionData3 = new TransactionData(new Hash("33".getBytes()));
        TransactionData3.setSenderTrustScore(84);
        TransactionData3.setCreateTime(new Date());

        TransactionData TransactionData4 = new TransactionData(new Hash("44".getBytes()));
        TransactionData4.setSenderTrustScore(86);
        TransactionData4.setCreateTime(new Date());

        TransactionData TransactionData5 = new TransactionData(new Hash("55".getBytes()));
        TransactionData5.setSenderTrustScore(76);
        TransactionData5.setCreateTime(new Date());

        TransactionData TransactionData6 = new TransactionData(new Hash("66".getBytes()));
        TransactionData6.setSenderTrustScore(78);
        TransactionData6.setCreateTime(new Date());

        TransactionData TransactionData7 = new TransactionData(new Hash("77".getBytes()));
        TransactionData7.setSenderTrustScore(86);
        TransactionData7.setCreateTime(new Date());

        TransactionData TransactionData8 = new TransactionData(new Hash("88".getBytes()));
        TransactionData8.setSenderTrustScore(80);
        TransactionData8.setCreateTime(new Date());

        TransactionData TransactionData9 = new TransactionData(new Hash("99".getBytes()));
        TransactionData9.setSenderTrustScore(72);
        TransactionData9.setCreateTime(new Date());

        newTransactions.add(TransactionData2);
        newTransactions.add(TransactionData3);
        newTransactions.add(TransactionData4);
        newTransactions.add(TransactionData5);
        newTransactions.add(TransactionData6);
        newTransactions.add(TransactionData7);
        newTransactions.add(TransactionData8);
        newTransactions.add(TransactionData9);
    }
    private void setUpTransactionsFromSnapShot() {
        TransactionData TransactionData0 = new TransactionData(new Hash("00".getBytes()));
        TransactionData0.setSenderTrustScore(70);
        List<Hash> hashChildren = new Vector<>();
        hashChildren.add(new Hash("11".getBytes()));
        TransactionData0.setChildrenTransactions(hashChildren);
        TransactionData0.setProcessStartTime(new Date());
        TransactionData0.setAttachmentTime(new Date());

        TransactionData TransactionData1 = new TransactionData(new Hash("11".getBytes()));
        TransactionData1.setSenderTrustScore(73);
        TransactionData1.setRightParentHash(TransactionData0.getHash());
        TransactionData1.setCreateTime(new Date());
        TransactionData1.setProcessStartTime(new Date());
        TransactionData1.setAttachmentTime(new Date());
        transaction.delete(TransactionData0.getHash());
        transaction.delete(TransactionData1.getHash());

        transaction.put(TransactionData0);
        transaction.put(TransactionData1);

        notTccConfirmTransactions.add(TransactionData0.getHash());
        notTccConfirmTransactions.add(TransactionData1.getHash());
    }
    private void setunconfirmedTransactionsTable(){
        ConfirmationData confirmationData0 = new ConfirmationData(new Hash("00".getBytes()));
        ConfirmationData confirmationData1 = new ConfirmationData(new Hash("11".getBytes()));
        ConfirmationData confirmationData2 = new ConfirmationData(new Hash("22".getBytes()));
        ConfirmationData confirmationData3 = new ConfirmationData(new Hash("33".getBytes()));
        ConfirmationData confirmationData4 = new ConfirmationData(new Hash("44".getBytes()));
        ConfirmationData confirmationData5 = new ConfirmationData(new Hash("55".getBytes()));
        ConfirmationData confirmationData6 = new ConfirmationData(new Hash("66".getBytes()));
        ConfirmationData confirmationData7 = new ConfirmationData(new Hash("77".getBytes()));
        ConfirmationData confirmationData8 = new ConfirmationData(new Hash("88".getBytes()));
        ConfirmationData confirmationData9 = new ConfirmationData(new Hash("99".getBytes()));

        unconfirmedTransactions.delete(confirmationData0.getHash());
        unconfirmedTransactions.delete(confirmationData1.getHash());
        unconfirmedTransactions.delete(confirmationData2.getHash());
        unconfirmedTransactions.delete(confirmationData3.getHash());
        unconfirmedTransactions.delete(confirmationData4.getHash());
        unconfirmedTransactions.delete(confirmationData5.getHash());
        unconfirmedTransactions.delete(confirmationData6.getHash());
        unconfirmedTransactions.delete(confirmationData7.getHash());
        unconfirmedTransactions.delete(confirmationData8.getHash());
        unconfirmedTransactions.delete(confirmationData9.getHash());

        unconfirmedTransactions.put(confirmationData0);
        unconfirmedTransactions.put(confirmationData1);
        unconfirmedTransactions.put(confirmationData2);
        unconfirmedTransactions.put(confirmationData3);
        unconfirmedTransactions.put(confirmationData4);
        unconfirmedTransactions.put(confirmationData5);
        unconfirmedTransactions.put(confirmationData6);
        unconfirmedTransactions.put(confirmationData7);
        unconfirmedTransactions.put(confirmationData8);
        unconfirmedTransactions.put(confirmationData9);
    }
    @Before
    public void setUp() throws Exception {
        System.out.println("Initializing!");
        newTransactions = new Vector<>();
        notTccConfirmTransactions = new Vector<>();

        Calendar calender = Calendar.getInstance();
        setunconfirmedTransactionsTable();
        setUpTransactionsFromSnapShot();
        setUpNewTransactions();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void initCluster() throws InterruptedException {
        try {
            cluster.initCluster(notTccConfirmTransactions);
            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();


            exec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (newTransactions.size() > 0) {
                        int index = random.nextInt(newTransactions.size());
                        if (!cluster.addNewTransaction(newTransactions.get(index))
                                || newTransactions.get(index).getLeftParentHash() != null
                                || newTransactions.get(index).getRightParentHash() != null) {
                            log.info("removed hash:{}", newTransactions.get(index));
                            newTransactions.remove(index);

                        }
                    }
                }
            }, 4, 8, TimeUnit.SECONDS);

            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    //log.info("sleep");
                } catch (InterruptedException e) {
                    log.error("An error in initCluster() Test when sleep ", e);

                }
            }
        } catch (InterruptedException e) {
            log.error("An error in initCluster() Test ", e);
        }

    }
}