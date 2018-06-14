package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.Transaction;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;

public class TransactionTests {

    @Test
    public void isSource_NullChildrenList_True(){
        Transaction transaction = new Transaction(new Hash("Transaction 0".getBytes()));
        Assert.assertTrue(transaction.isSource());
    }

    @Test
    public void isSource_EmptyChildrenList_True(){
        Transaction transaction = new Transaction(new Hash("Transaction 0".getBytes()));
        transaction.setChildrenTransactions(new LinkedList<>());
        Assert.assertTrue(transaction.isSource());
    }

    @Test
    public void isSource_NonEmptyChildrenList_False(){
        Transaction transaction = new Transaction(new Hash("Transaction 0".getBytes()));
        transaction.setChildrenTransactions(
                Collections.singletonList(new Hash("Transaction 1".getBytes())));
        Assert.assertFalse(transaction.isSource());
    }
}
