import io.coti.common.data.*;
import io.coti.common.model.*;
import io.coti.fullnode.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

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

    @Test
    public void saveAndRetrieveSingleTransaction() {
        TransactionData transactionData1 = new TransactionData(new ArrayList<>(),new Hash("TransactionData 0".getBytes()),"test",5, new Date());
        transactions.put(transactionData1);
        TransactionData transactionData2 = transactions.getByHash(transactionData1.getHash());
        Assert.assertEquals(transactionData1, transactionData2);
    }

    @Test
    public void saveAndRetrieveWithManyTransactions() {
        TransactionData transactionData1 = new TransactionData(new ArrayList<>(),new Hash("TransactionData 0".getBytes()),"test",5, new Date());
        TransactionData transactionData2 = new TransactionData(new ArrayList<>(),new Hash("TransactionData 2".getBytes()),"test",5, new Date());
        transactions.put(transactionData1);
        transactions.put(transactionData2);
        TransactionData transactionData3 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData1, transactionData3);
    }

    @Test
    public void saveManyAndRetrieveManyTransactions() {
        TransactionData transactionData1 = new TransactionData(new ArrayList<>(),new Hash("TransactionData 0".getBytes()),"test",5, new Date());
        TransactionData transactionData2 = new TransactionData(new ArrayList<>(),new Hash("TransactionData 1".getBytes()),"test",5, new Date());
        transactions.put(transactionData1);
        transactions.put(transactionData2);
        TransactionData transactionData3 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        TransactionData transactionData4 = transactions.getByHash(new Hash("TransactionData 1".getBytes()));

        Assert.assertEquals(transactionData1, transactionData3);
        Assert.assertEquals(transactionData2, transactionData4);
    }

    @Test
    public void saveAndDeleteTransactions() {
        TransactionData transactionData1 = new TransactionData(null,new Hash("TransactionData 0".getBytes()),"test",5, new Date());
        transactions.put(transactionData1);
        TransactionData transactionData2 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData1, transactionData2);
        transactions.delete(transactionData1.getHash());
        transactionData2 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData2, null);
    }

    @Test
    public void saveAndGetBaseTransaction() {
        BaseTransactionData baseTransactionData1 = new BaseTransactionData(new Hash("ABCDEF"),new BigDecimal(12.2),new Hash("ABCDEF"),new SignatureData("",""),new Date());
        baseTransactions.put(baseTransactionData1);
        BaseTransactionData baseTransactionData2 = baseTransactions.getByHash(new Hash("ABCDEF"));
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
        BaseTransactionData transaction1 = new BaseTransactionData(new Hash("ABCDEF"),new BigDecimal(12.2),new Hash("ABCDEF"),new SignatureData("",""),new Date());
        baseTransactions.put(transaction1);
        BaseTransactionData transaction2 = baseTransactions.getByHash("ABCDEF");
        Assert.assertEquals(transaction1, transaction2);
        baseTransactions.delete(transaction1.getHash());
        transaction2 = baseTransactions.getByHash("ABCDEF");
        Assert.assertEquals(transaction2, null);
    }

}