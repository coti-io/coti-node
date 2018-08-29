package integrationTest;

import io.coti.common.data.*;
import io.coti.common.database.RocksDBConnector;
import io.coti.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;

@ContextConfiguration(classes = {Transactions.class,
        Addresses.class,
        RocksDBConnector.class,
        AddressesTransactionsHistory.class,
        TrustScores.class,
        TransactionIndexes.class,
        TransactionVotes.class
})
@RunWith(SpringRunner.class)

@TestPropertySource(locations = "../test.properties")
@Slf4j
public class RocksDBTests {

    @Autowired
    private Transactions transactions;

    @Autowired
    private Addresses addresses;

    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;

    @Autowired
    private TrustScores trustScores;

    @Autowired
    private TransactionIndexes transactionIndexes;

    @Autowired
    private TransactionVotes transactionVotes;

    @Test
    public void saveAndRetrieveSingleTransaction() {
        TransactionData transactionData1 = new TransactionData(new ArrayList<>(), new Hash("TransactionData 0".getBytes()), "test", 5, new Date());
        transactions.put(transactionData1);
        TransactionData transactionData2 = transactions.getByHash(transactionData1.getHash());
        Assert.assertEquals(transactionData1, transactionData2);
    }

    @Test
    public void saveAndRetrieveWithManyTransactions() {
        TransactionData transactionData1 = new TransactionData(new ArrayList<>(), new Hash("TransactionData 0".getBytes()), "test", 5, new Date());
        TransactionData transactionData2 = new TransactionData(new ArrayList<>(), new Hash("TransactionData 2".getBytes()), "test", 5, new Date());
        transactions.put(transactionData1);
        transactions.put(transactionData2);
        TransactionData transactionData3 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        Assert.assertEquals(transactionData1, transactionData3);
    }

    @Test
    public void saveManyAndRetrieveManyTransactions() {
        TransactionData transactionData1 = new TransactionData(new ArrayList<>(), new Hash("TransactionData 0".getBytes()), "test", 5, new Date());
        TransactionData transactionData2 = new TransactionData(new ArrayList<>(), new Hash("TransactionData 1".getBytes()), "test", 5, new Date());
        transactions.put(transactionData1);
        transactions.put(transactionData2);
        TransactionData transactionData3 = transactions.getByHash(new Hash("TransactionData 0".getBytes()));
        TransactionData transactionData4 = transactions.getByHash(new Hash("TransactionData 1".getBytes()));

        Assert.assertEquals(transactionData1, transactionData3);
        Assert.assertEquals(transactionData2, transactionData4);
    }


    @Test
    public void saveAndGetAddress() {
        AddressData addressData1 = new AddressData(new Hash("AddressData 0".getBytes()));
        addresses.put(addressData1);
        AddressData addressData2 = addresses.getByHash(new Hash("AddressData 0".getBytes()));
        Assert.assertEquals(addressData1, addressData2);
    }

    @Test
    public void saveAndGetAddressesTransactionsHistory() {
        AddressTransactionsHistory addressTransactionsHistory1 = new AddressTransactionsHistory(new Hash("AddressTransactionsHistory 0".getBytes()));
        addressesTransactionsHistory.put(addressTransactionsHistory1);
        AddressTransactionsHistory addressTransactionsHistory2 = addressesTransactionsHistory.getByHash(new Hash("AddressTransactionsHistory 0".getBytes()));
        Assert.assertEquals(addressTransactionsHistory1, addressTransactionsHistory2);
    }

    @Test
    public void saveAndGetTrustScores() {
        TrustScoreData trustScores1 = new TrustScoreData(new Hash("TrustScores 0".getBytes()), 6.6);
        trustScores.put(trustScores1);
        TrustScoreData trustScores2 = trustScores.getByHash(new Hash("TrustScores 0".getBytes()));
        Assert.assertEquals(trustScores1, trustScores2);
    }

    @Test
    public void saveAndGetTransactionVotes() {
        TransactionVoteData transactionVoteData1 = new TransactionVoteData(new Hash("TransactionVote 0".getBytes()), null );
        transactionVotes.put(transactionVoteData1);
        TransactionVoteData transactionVoteData2 = transactionVotes.getByHash(new Hash("TransactionVote 0".getBytes()));
        Assert.assertEquals(transactionVoteData1, transactionVoteData2);
    }

}
