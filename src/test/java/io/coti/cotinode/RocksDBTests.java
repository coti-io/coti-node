package io.coti.cotinode;

import io.coti.cotinode.data.*;
import io.coti.cotinode.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class RocksDBTests {
    @Autowired
    private Transactions transactions;
    @Autowired
    private BaseTransactions baseTransactions;
    @Autowired
    private Addresses addresses;
    @Autowired
    private ConfirmedTransactionData confirmedTransactionData;
    @Autowired
    private UnconfirmedTransaction unconfirmedTransaction;

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
    public void saveAndDeleteTransactions() {
        TransactionData transactionData1 = new TransactionData(new Hash("TransactionData 0".getBytes()));
        transactions.put(transactionData1);
        TransactionData transactionData2 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData1, transactionData2);
        transactions.delete(transactionData1.getKey());
        transactionData2 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData2, null);
    }

    @Test
    public void saveAndGetBaseTransaction() {
        BaseTransactionData baseTransactionData1 = new BaseTransactionData(new Hash("TransactionData 0".getBytes()), 12.2);
        baseTransactions.put(baseTransactionData1);
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
        transaction2 = baseTransactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transaction2, null);
    }

    @Test
    public void saveAndGetBalance() {
        ConfirmedTransactionData confirmedTransactionData1 = new ConfirmedTransactionData(new Hash("ConfirmedTransactionData 0".getBytes()));
        confirmedTransactionData.put(confirmedTransactionData1);
        ConfirmedTransactionData confirmedTransactionData2 = confirmedTransactionData.getByHash(new Hash("ConfirmedTransactionData 0".getBytes()));
        Assert.assertEquals(confirmedTransactionData1, confirmedTransactionData2);
    }

    @Test
    public void saveAndGetPreBalance() {
        UnconfirmedTransactionData unconfirmedTransactionData1 = new UnconfirmedTransactionData(new Hash("ConfirmedTransactionData 0".getBytes()));
        unconfirmedTransaction.put(unconfirmedTransactionData1);
        UnconfirmedTransactionData unconfirmedTransactionData2 = unconfirmedTransaction.getByHash(new Hash("ConfirmedTransactionData 0".getBytes()));
        Assert.assertEquals(unconfirmedTransactionData1, unconfirmedTransactionData2);
    }
}