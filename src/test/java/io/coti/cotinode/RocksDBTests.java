package io.coti.cotinode;
import io.coti.cotinode.model.BaseTransaction;
import io.coti.cotinode.model.Interfaces.IBaseTransaction;
import io.coti.cotinode.model.Interfaces.ITransaction;
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
    public void saveAndRetrieveSingleITransaction(){
        ITransaction transaction1 = new Transaction("ITransaction 0".getBytes());
        provider.put(transaction1);
        ITransaction transaction2 = provider.getTransaction(transaction1.getKey());
        Assert.assertEquals(transaction1, transaction2);
    }

    @Test
    public void saveAndRetrieveWithManyITransactions(){
        ITransaction transaction1 = new Transaction("ITransaction 0".getBytes());
        ITransaction transaction2 = new Transaction("ITransaction 2".getBytes());
        provider.put(transaction1);
        provider.put(transaction2);
        ITransaction transaction3 = provider.getTransaction("ITransaction 0".getBytes());
        Assert.assertEquals(transaction1, transaction3);
    }

    @Test
    public void saveManyAndRetrieveManyITransactions(){
        ITransaction transaction1 = new Transaction("ITransaction 0".getBytes());
        ITransaction transaction2 = new Transaction("ITransaction 1".getBytes());
        provider.put(transaction1);
        provider.put(transaction2);
        ITransaction transaction3 = provider.getTransaction("ITransaction 0".getBytes());
        ITransaction transaction4 = provider.getTransaction("ITransaction 1".getBytes());

        Assert.assertEquals(transaction1, transaction3);
        Assert.assertEquals(transaction2, transaction4);
    }

    @Test
    public void saveManyAndGetAll(){
        ITransaction transaction1 = new Transaction("First A".getBytes());
        ITransaction transaction2 = new Transaction("Second B".getBytes());
        provider.put(transaction1);
        provider.put(transaction2);
        List<ITransaction> transactions = provider.getAllTransactions();

        Assert.assertEquals(transaction1, transactions.get(0));
        Assert.assertEquals(transaction2, transactions.get(1));
        Assert.assertEquals(2, transactions.size());
    }

    @Test
    public void saveAndDeleteTransactions(){
        ITransaction transaction1 = new Transaction("ITransaction 0".getBytes());
        provider.put(transaction1);
        ITransaction transaction2 = provider.getTransaction("ITransaction 0".getBytes());
        Assert.assertEquals(transaction1, transaction2);
        provider.deleteTransaction(transaction1.getKey());
        Assert.assertEquals(provider.getAllTransactions(), new ArrayList<ITransaction>());
    }

    @Test
    public void saveAndGetBaseTransaction(){
        IBaseTransaction baseTransaction1 = new BaseTransaction("ITransaction 0".getBytes());
        provider.put(baseTransaction1);
        IBaseTransaction baseTransaction2 = provider.getBaseTransaction("ITransaction 0".getBytes());
        Assert.assertEquals(baseTransaction1, baseTransaction2);
    }
}