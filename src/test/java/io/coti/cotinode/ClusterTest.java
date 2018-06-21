package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.Cluster;
import io.coti.cotinode.service.interfaces.ICluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ClusterTest {

    private static int counter = 0;

    @Autowired
    private ICluster cluster;
    private List<TransactionData> newTransactions;
    private List<TransactionData> notTccConfirmTransactions;

    @Before
    public void setUp() throws Exception {
        System.out.println("Initializing!");
        newTransactions = new Vector<>();
        notTccConfirmTransactions = new Vector<>();

//        TransactionData transaction1 = new TransactionData(new Hash("1".getBytes()));
//        TransactionData transaction2 = new TransactionData(new Hash("2".getBytes()));
//        TransactionData transaction3 = new TransactionData(new Hash("3".getBytes()));
//        TransactionData transaction4 = new TransactionData(new Hash("4".getBytes()));
//        allClusterTransactions.add(transaction1);
//        allClusterTransactions.add(transaction2);
//        allClusterTransactions.add(transaction3);
//        allClusterTransactions.add(transaction4);
        TransactionData TransactionData0 = new TransactionData(new Hash("0".getBytes()));
        TransactionData0.setSenderTrustScore(80);
        TransactionData0.setCreateTime(new Date());

        TransactionData TransactionData1 = new TransactionData(new Hash("1".getBytes()));
        TransactionData1.setSenderTrustScore(78);
        TransactionData1.setCreateTime(new Date());

        TransactionData TransactionData2 = new TransactionData(new Hash("2".getBytes()));
        TransactionData2.setSenderTrustScore(92);
        TransactionData2.setCreateTime(new Date());

        TransactionData TransactionData3 = new TransactionData(new Hash("3".getBytes()));
        TransactionData3.setSenderTrustScore(84);
        TransactionData3.setCreateTime(new Date());

        TransactionData TransactionData4 = new TransactionData(new Hash("4".getBytes()));
        TransactionData4.setSenderTrustScore(86);
        TransactionData4.setCreateTime(new Date());

        newTransactions.add(TransactionData0);
        newTransactions.add(TransactionData1);
        newTransactions.add(TransactionData2);
        newTransactions.add(TransactionData3);
        newTransactions.add(TransactionData4);

        TransactionData TransactionData5 = new TransactionData(new Hash("5".getBytes()));
        TransactionData5.setSenderTrustScore(78);
        TransactionData5.setCreateTime(new Date());

        TransactionData TransactionData6 = new TransactionData(new Hash("6".getBytes()));
        TransactionData6.setSenderTrustScore(73);
        TransactionData6.setRightParent(new Hash("7"));
        TransactionData6.setCreateTime(new Date());

        TransactionData TransactionData7 = new TransactionData(new Hash("7".getBytes()));
        TransactionData7.setSenderTrustScore(70);
        List<Hash> hashChildrens = new Vector<>();
        hashChildrens.add(new Hash("6"));
        TransactionData7.setChildrenTransactions(hashChildrens);
        TransactionData7.setCreateTime(new Date());

        notTccConfirmTransactions.add(TransactionData6);
        notTccConfirmTransactions.add(TransactionData7);

        cluster = new Cluster();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void initCluster() throws InterruptedException {
        cluster.initCluster(notTccConfirmTransactions);
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        //allClusterTransactions.forEach(transaction -> cluster.addNewTransaction(transaction));


        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                TransactionData TransactionData5 = new TransactionData(new Hash("5".getBytes()));
                TransactionData5.setSenderTrustScore(86);
                TransactionData5.setCreateTime(new Date());

                if (counter < newTransactions.size()) {
                    cluster.addNewTransaction(newTransactions.get(counter));
                    counter++;
                } else if (counter == newTransactions.size()) {
                    cluster.trustScoreConsensusProcess();
                    counter++;
                } else if (counter == newTransactions.size() + 1){
                    cluster.addNewTransaction(TransactionData5);
                    counter++;
                }
            }
        }, 15, 3, TimeUnit.SECONDS);

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}