package io.coti.cotinode;

import io.coti.cotinode.model.Transaction;
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
    List<Transaction> allClusterTransactions;
    @Before
    public void setUp() throws Exception {
        System.out.println("Initializing!");
        allClusterTransactions = new Vector<>();
        allClusterTransactions.add(new Transaction("1".getBytes()));
        allClusterTransactions.add(new Transaction("2".getBytes()));
        allClusterTransactions.add(new Transaction("3".getBytes()));
        cluster = new Cluster();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void initCluster() {
        cluster.initCluster(allClusterTransactions);
    }
}