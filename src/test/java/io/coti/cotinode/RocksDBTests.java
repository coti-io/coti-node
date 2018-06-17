package io.coti.cotinode;

import io.coti.cotinode.data.*;
import io.coti.cotinode.model.*;
import io.coti.cotinode.storage.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RocksDBTests {
    private IDatabaseConnector provider;

    @Autowired
    private Transactions transactions;
    private BaseTransactions baseTransactions;
    private Addresses addresses;
    private Balances balances;
    private PreBalances preBalances;

    @Before
    public void init() {
        transactions = new Transactions();
        baseTransactions = new BaseTransactions();
        addresses = new Addresses();
        balances = new Balances();
        preBalances = new PreBalances();
    }

    @Test
    public void saveAndRetrieveSingleTransaction() {
        TransactionData transactionData1 = new TransactionData(new Hash("TransactionData 0".getBytes()));
        transactions.put(transactionData1);
        TransactionData transactionData2 = transactions.getByHash(transactionData1.getKey());
        Assert.assertEquals(transactionData1, transactionData2);
    }

    @Test
    public void saveAndRetrieveWithManyTransactions() {
        TransactionData transactionData1 = new TransactionData(new Hash("TransactionData 0".getBytes()));
        TransactionData transactionData2 = new TransactionData(new Hash("TransactionData 2".getBytes()));
        transactions.put(transactionData1);
        transactions.put(transactionData2);
        TransactionData transactionData3 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData1, transactionData3);
    }

    @Test
    public void saveManyAndRetrieveManyTransactions() {
        TransactionData transactionData1 = new TransactionData(new Hash("TransactionData 0".getBytes()));
        TransactionData transactionData2 = new TransactionData(new Hash("TransactionData 1".getBytes()));
        transactions.put(transactionData1);
        transactions.put(transactionData2);
        TransactionData transactionData3 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        TransactionData transactionData4 = transactions.getByHash(new Hash("TransactionData 1".getBytes()));

        Assert.assertEquals(transactionData1, transactionData3);
        Assert.assertEquals(transactionData2, transactionData4);
    }

    @Test
    public void saveManyAndGetAll() {
        TransactionData transactionData1 = new TransactionData(new Hash("TransactionData A".getBytes()));
        TransactionData transactionData2 = new TransactionData(new Hash("TransactionData B".getBytes()));
        transactions.put(transactionData1);
        transactions.put(transactionData2);
        List<TransactionData> transactionData = transactions.getAll();

        Assert.assertEquals(transactionData1, transactionData.get(0));
        Assert.assertEquals(transactionData2, transactionData.get(1));
        Assert.assertEquals(2, transactionData.size());
    }

    @Test
    public void saveAndDeleteTransactions() {
        TransactionData transactionData1 = new TransactionData(new Hash("TransactionData 0".getBytes()));
        transactions.put(transactionData1);
        TransactionData transactionData2 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData1, transactionData2);
        transactions.delete(transactionData1.getKey());
        Assert.assertEquals(transactions.getAll(), new ArrayList<TransactionData>());
    }

    @Test
    public void saveAndGetBaseTransaction() {
        BaseTransactionData baseTransactionData1 = new BaseTransactionData(new Hash("TransactionData 0".getBytes()), 12.2);
        provider.put(baseTransactionData1);
        BaseTransactionData baseTransactionData2 = baseTransactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(baseTransactionData1, baseTransactionData2);
    }

    @Test
    public void saveAndGetAddress() {
        AddressData addressData1 = new AddressData(new Hash("AddressData 0".getBytes()));
        addresses.put(addressData1);
        AddressData addressData2 = addresses.getByHash(new Hash("AddressData 0".getBytes()));
        Assert.assertEquals(addressData1, addressData2);
    }

    @Test
    public void saveAndDeleteBaseTransactions() {
        BaseTransactionData transaction1 = new BaseTransactionData(new Hash("TransactionData 0".getBytes()), 12.2);
        baseTransactions.put(transaction1);
        BaseTransactionData transaction2 = baseTransactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transaction1, transaction2);
        baseTransactions.delete(transaction1.getKey());
        Assert.assertEquals(baseTransactions.getAll(), new ArrayList<BaseTransactionData>());
    }

    @Test
    public void saveAndGetBalance() {
        BalanceData balanceData1 = new BalanceData(new Hash("BalanceData 0".getBytes()));
        balances.put(balanceData1);
        BalanceData balanceData2 = balances.getByHash(new Hash("BalanceData 0".getBytes()));
        Assert.assertEquals(balanceData1, balanceData2);
    }

    @Test
    public void saveAndGetPreBalance() {
        PreBalance preBalance1 = new PreBalance(new Hash("BalanceData 0".getBytes()));
        preBalances.put(preBalance1);
        PreBalance preBalance2 = preBalances.getByHash(new Hash("BalanceData 0".getBytes()));
        Assert.assertEquals(preBalance1, preBalance2);
    }
}