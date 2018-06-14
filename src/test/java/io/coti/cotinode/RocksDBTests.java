package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.*;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import io.coti.cotinode.storage.RocksDBProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RocksDBTests {
    private IPersistenceProvider provider;
    @Before
    public void init(){
        provider = new RocksDBProvider();
        provider.init();
    }

    @After
    public void shutdown(){
        provider.shutdown();
        provider.deleteDatabaseFolder();
    }

    @Test
    public void saveAndRetrieveSingleTransaction(){
        Transaction transaction1 = new Transaction(new Hash("Transaction 0".getBytes()));
        provider.put(transaction1);
        Transaction transaction2 = provider.getTransaction(transaction1.getKey());
        Assert.assertEquals(transaction1, transaction2);
    }

    @Test
    public void saveAndRetrieveWithManyTransactions(){
        Transaction transaction1 = new Transaction(new Hash("Transaction 0".getBytes()));
        Transaction transaction2 = new Transaction(new Hash("Transaction 2".getBytes()));
        provider.put(transaction1);
        provider.put(transaction2);
        Transaction transaction3 = provider.getTransaction(new Hash("Transaction 0".getBytes()));
        Assert.assertEquals(transaction1, transaction3);
    }

    @Test
    public void saveManyAndRetrieveManyTransactions(){
        Transaction transaction1 = new Transaction(new Hash("Transaction 0".getBytes()));
        Transaction transaction2 = new Transaction(new Hash("Transaction 1".getBytes()));
        provider.put(transaction1);
        provider.put(transaction2);
        Transaction transaction3 = provider.getTransaction(new Hash("Transaction 0".getBytes()));
        Transaction transaction4 = provider.getTransaction(new Hash("Transaction 1".getBytes()));

        Assert.assertEquals(transaction1, transaction3);
        Assert.assertEquals(transaction2, transaction4);
    }

    @Test
    public void saveManyAndGetAll(){
        Transaction transaction1 = new Transaction(new Hash("Transaction A".getBytes()));
        Transaction transaction2 = new Transaction(new Hash("Transaction B".getBytes()));
        provider.put(transaction1);
        provider.put(transaction2);
        List<Transaction> transactions = provider.getAllTransactions();

        Assert.assertEquals(transaction1, transactions.get(0));
        Assert.assertEquals(transaction2, transactions.get(1));
        Assert.assertEquals(2, transactions.size());
    }

    @Test
    public void saveAndDeleteTransactions(){
        Transaction transaction1 = new Transaction(new Hash("Transaction 0".getBytes()));
        provider.put(transaction1);
        Transaction transaction2 = provider.getTransaction(new Hash("Transaction 0".getBytes()));
        Assert.assertEquals(transaction1, transaction2);
        provider.deleteTransaction(transaction1.getKey());
        Assert.assertEquals(provider.getAllTransactions(), new ArrayList<Transaction>());
    }

    @Test
    public void saveAndGetBaseTransaction(){
        BaseTransaction baseTransaction1 = new BaseTransaction(new Hash("Transaction 0".getBytes()));
        provider.put(baseTransaction1);
        BaseTransaction baseTransaction2 = provider.getBaseTransaction(new Hash("Transaction 0".getBytes()));
        Assert.assertEquals(baseTransaction1, baseTransaction2);
    }

    @Test
    public void saveAndGetAddress(){
        Address address1 = new Address(new Hash("Address 0".getBytes()));
        provider.put(address1);
        Address address2 = provider.getAddress(new Hash("Address 0".getBytes()));
        Assert.assertEquals(address1, address2);
    }

    @Test
    public void saveAndDeleteBaseTransactions(){
        BaseTransaction transaction1 = new BaseTransaction(new Hash("Transaction 0".getBytes()));
        provider.put(transaction1);
        BaseTransaction transaction2 = provider.getBaseTransaction(new Hash("Transaction 0".getBytes()));
        Assert.assertEquals(transaction1, transaction2);
        provider.deleteBaseTransaction(transaction1.getKey());
        Assert.assertEquals(provider.getAllTransactions(), new ArrayList<BaseTransaction>());
    }

    @Test
    public void saveAndGetBalance(){
        Balance balance1 = new Balance(new Hash("Balance 0".getBytes()));
        provider.put(balance1);
        Balance balance2 = provider.getBalance(new Hash("Balance 0".getBytes()));
        Assert.assertEquals(balance1, balance2);
    }

    @Test
    public void saveAndGetPreBalance(){
        PreBalance preBalance1 = new PreBalance(new Hash("Balance 0".getBytes()));
        provider.put(preBalance1);
        PreBalance preBalance2 = provider.getPreBalance(new Hash("Balance 0".getBytes()));
        Assert.assertEquals(preBalance1, preBalance2);
    }
}