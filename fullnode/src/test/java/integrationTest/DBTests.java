package integrationTest;

import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.*;
import io.coti.basenode.services.*;
import io.coti.basenode.services.LiveView.LiveViewService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;

@TestPropertySource(locations = "../test.properties")
@ContextConfiguration(classes = {BaseNodeBalanceService.class,
        Transactions.class,
        Addresses.class,
        AddressTransactionsHistories.class,
        TrustScores.class,
        TransactionIndexes.class,
        TransactionVotes.class,
        LiveViewService.class,
        SimpMessagingTemplate.class,
        RocksDBConnector.class,
        TransactionHelper.class,
        ClusterService.class,
        SourceSelector.class,
        TccConfirmationService.class,
        TransactionIndexService.class,
        DspConsensusCrypto.class,
        TransactionTrustScoreCrypto.class
})
@SpringBootTest
@RunWith(SpringRunner.class)
public class DBTests {
    @Autowired
    private Transactions transactions;
    @Autowired
    private Addresses addresses;
    @Autowired
    private AddressTransactionsHistories addressesTransactionsHistory;
    @Autowired
    private TrustScores trustScores;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private TransactionVotes transactionVotes;
    @Autowired
    private RocksDBConnector rocksDBConnector;
    @MockBean
    private LiveViewService liveViewService;
    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

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
    public void saveAndGetAddressesTransactionsHistory() {
        AddressTransactionsHistory addressTransactionsHistory1 =
                new AddressTransactionsHistory(new Hash("AddressTransactionsHistory "));
        addressesTransactionsHistory.put(addressTransactionsHistory1);
        AddressTransactionsHistory addressTransactionsHistory2 = addressesTransactionsHistory.getByHash(new Hash("AddressTransactionsHistory "));
        Assert.assertEquals(addressTransactionsHistory1, addressTransactionsHistory2);
    }

    @Test
    public void saveAndGetAddress() {
        AddressData addressData1 = new AddressData(new Hash("AddressData 0".getBytes()));
        addresses.put(addressData1);
        AddressData addressData2 = addresses.getByHash(new Hash("AddressData 0".getBytes()));
        Assert.assertEquals(addressData1, addressData2);
    }

    @Test
    public void saveAndGetTrustScores() {
        TrustScoreData trustScoreData1 =
                new TrustScoreData(new Hash("TrustScoreData 0".getBytes()), 7);
        trustScores.put(trustScoreData1);
        TrustScoreData trustScoreData2 = trustScores.getByHash(new Hash("TrustScoreData 0".getBytes()));
        Assert.assertEquals(trustScoreData1, trustScoreData2);
    }

    @Test
    public void saveAndGetTransactionIndexes() {
        TransactionIndexData transactionIndexData1 =
                new TransactionIndexData(new Hash("TransactionIndexData 0".getBytes()),
                        6,
                        null);
        transactionIndexes.put(transactionIndexData1);
        TransactionIndexData transactionIndexData2 =
                transactionIndexes.getByHash(transactionIndexData1.getHash());
        Assert.assertEquals(transactionIndexData1, transactionIndexData2);
    }

    @Test
    public void saveAndGetTransactionVotes() {
        TransactionVoteData transactionVoteData1 =
                new TransactionVoteData(new Hash("TransactionVoteData 0".getBytes()), null);
        transactionVotes.put(transactionVoteData1);
        TransactionVoteData transactionVoteData2 =
                transactionVotes.getByHash(new Hash("TransactionVoteData 0".getBytes()));
        Assert.assertEquals(transactionVoteData1, transactionVoteData2);
    }

}
