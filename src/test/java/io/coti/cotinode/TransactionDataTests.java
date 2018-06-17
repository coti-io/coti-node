package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;

public class TransactionDataTests {

    @Test
    public void isSource_NullChildrenList_True(){
        TransactionData transactionData = new TransactionData(new Hash("TransactionData 0".getBytes()));
        Assert.assertTrue(transactionData.isSource());
    }

    @Test
    public void isSource_EmptyChildrenList_True(){
        TransactionData transactionData = new TransactionData(new Hash("TransactionData 0".getBytes()));
        transactionData.setChildrenTransactions(new LinkedList<>());
        Assert.assertTrue(transactionData.isSource());
    }

    @Test
    public void isSource_NonEmptyChildrenList_False(){
        TransactionData transactionData = new TransactionData(new Hash("TransactionData 0".getBytes()));
        transactionData.setChildrenTransactions(
                Collections.singletonList(new Hash("TransactionData 1".getBytes())));
        Assert.assertFalse(transactionData.isSource());
    }
}
