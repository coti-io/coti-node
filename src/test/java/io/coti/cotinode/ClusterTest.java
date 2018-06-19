package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.Cluster;
import io.coti.cotinode.service.interfaces.ICluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Vector;

import static org.junit.Assert.*;

public class ClusterTest {

    ICluster cluster;
    List<TransactionData> allClusterTransactions;

    @Before
    public void setUp() throws Exception {
        System.out.println("Initializing!");
        allClusterTransactions = new Vector<>();

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

        TransactionData TransactionData1 = new TransactionData(new Hash("1".getBytes()));
        TransactionData1.setSenderTrustScore(70);

        TransactionData TransactionData2 = new TransactionData(new Hash("2".getBytes()));
        TransactionData2.setSenderTrustScore(100);

        TransactionData TransactionData3 = new TransactionData(new Hash("3".getBytes()));
        TransactionData3.setSenderTrustScore(90);

        TransactionData TransactionData4 = new TransactionData(new Hash("4".getBytes()));
        TransactionData4.setSenderTrustScore(100);

        TransactionData TransactionData5 = new TransactionData(new Hash("5".getBytes()));
        TransactionData5.setSenderTrustScore(90);

        TransactionData TransactionData6 = new TransactionData(new Hash("6".getBytes()));
        TransactionData6.setSenderTrustScore(90);
        TransactionData6.setTransactionConsensus(true);

        allClusterTransactions.add( TransactionData0);
        allClusterTransactions.add( TransactionData1);
        allClusterTransactions.add( TransactionData2);
        allClusterTransactions.add( TransactionData3);
        allClusterTransactions.add( TransactionData4);
        allClusterTransactions.add( TransactionData5);
        allClusterTransactions.add( TransactionData6);
        cluster = new Cluster();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void initCluster() throws InterruptedException {
        cluster.initCluster(allClusterTransactions);
    }
}