package io.coti.cotinode;
import io.coti.cotinode.model.Transaction;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import io.coti.cotinode.storage.RocksDBProviderI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RocksDBTests {
    private IPersistenceProvider provider;
    @Before
    public void init(){
        System.out.println("Initializing!");
        provider = new RocksDBProviderI();
        provider.init();
    }

    @After
    public void shutdown(){
        provider.shutdown();
        provider.deleteDatabaseFolder();
    }

    @Test
    public void saveAndRetrieveSingleTransaction(){
        Transaction transaction1 = new Transaction("Transaction 0");
        provider.put(transaction1);
        Transaction transaction2 = provider.getTransaction("Transaction 0".getBytes());
        Assert.assertEquals(transaction1, transaction2);
    }

    @Test
    public void saveAndRetrieveWithManyTransactions(){
        Transaction transaction1 = new Transaction("Transaction 0");
        Transaction transaction2 = new Transaction("Transaction 2");
        provider.put(transaction1);
        provider.put(transaction2);
        Transaction transaction3 = provider.getTransaction("Transaction 0".getBytes());
        Assert.assertEquals(transaction1, transaction3);
    }

    @Test
    public void saveManyAndRetrieveManyTransactions(){
        Transaction transaction1 = new Transaction("Transaction 0");
        Transaction transaction2 = new Transaction("Transaction 1");
        provider.put(transaction1);
        provider.put(transaction2);
        Transaction transaction3 = provider.getTransaction("Transaction 0".getBytes());
        Transaction transaction4 = provider.getTransaction("Transaction 1".getBytes());

        Assert.assertEquals(transaction1, transaction3);
        Assert.assertEquals(transaction2, transaction4);
    }

    @Test
    public void saveManyAndGetAll(){
        Transaction transaction1 = new Transaction("First A");
        Transaction transaction2 = new Transaction("Second B");
        provider.put(transaction1);
        provider.put(transaction2);
        List<Transaction> transactions = provider.getAllTransactions();

        Assert.assertEquals(transaction1, transactions.get(0));
        Assert.assertEquals(transaction2, transactions.get(1));
        Assert.assertEquals(2, transactions.size());
    }

    @Test
    public void saveAndDelete(){
        Transaction transaction1 = new Transaction("Transaction 0");
        provider.put(transaction1);
        Transaction transaction2 = provider.getTransaction("Transaction 0".getBytes());
        Assert.assertEquals(transaction1, transaction2);
        provider.deleteTransaction(transaction1.getKey());
        Assert.assertEquals(provider.getAllTransactions(), new ArrayList<Transaction>());
    }
}
