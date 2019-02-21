package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.historynode.data.AddressTransactionsByDatesHistory;
import io.coti.historynode.database.HistoryRocksDBConnector;
import io.coti.historynode.http.GetTransactionsRequest;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import io.coti.historynode.model.AddressTransactionsByDatesHistories;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static utils.TestUtils.*;

@ContextConfiguration(classes = {TransactionService.class,
        AddressTransactionsByDatesHistories.class,
        HistoryRocksDBConnector.class,
        Transactions.class
})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {

    public static final int NUMBER_OF_ADDRESSES_PER_SENDER = 8;
    public static final int NUMBER_OF_SENDERS = 3;

    @Autowired
    public HistoryRocksDBConnector databaseConnector;

    @Autowired
    protected TransactionService transactionService;

    @Autowired
    AddressTransactionsByDatesHistories addressTransactionsByDatesHistories;

    @Autowired
    private Transactions transactions;

    @MockBean
    private IStorageConnector storageConnector;

    @Before
    public void init() {
        databaseConnector.init();
    }

    @Test
    public void getTransactionsDetails() {
        GetTransactionsRequest getTransactionsRequest = new GetTransactionsRequest();
        IntStream.range(0, NUMBER_OF_SENDERS).forEachOrdered(n -> {
            Hash senderAddress = generateRandomHash();
            storeInDbRandomTransactionsPerSender(senderAddress);
            getTransactionsRequest.getAddressesHashes().add(senderAddress);
        });

        ResponseEntity<IResponse> response = transactionService.getTransactionsDetails(getTransactionsRequest);
        GetTransactionsResponse getTransactionsResponse = (GetTransactionsResponse) (response.getBody());

        Assert.assertTrue(getTransactionsResponse.getTransactionsData().size() == NUMBER_OF_ADDRESSES_PER_SENDER * NUMBER_OF_SENDERS);
    }

    @Test
    public void deleteLocalUnconfirmedTransactions() {
        List<TransactionData> transactionsList = new ArrayList<>();
        TransactionData transactionData = createRandomTransaction();
        transactionsList.add(transactionData);
        transactions.put(transactionData);

        transactionService.deleteLocalUnconfirmedTransactions();

        Assert.assertNull(transactions.getByHash(transactionData.getHash()));
    }

    private List<TransactionData> storeInDbRandomTransactionsPerSender(Hash senderHash) {
        List<TransactionData> transactionsList = new ArrayList<>();
        AddressTransactionsByDatesHistory addressTransactionsByDatesHistory =
                new AddressTransactionsByDatesHistory(senderHash);
        IntStream.range(0, NUMBER_OF_ADDRESSES_PER_SENDER).forEachOrdered(n -> {
            TransactionData transactionData = createRandomTransactionWithSenderAddress(senderHash);
            transactionsList.add(transactionData);
            transactions.put(transactionData);
            addressTransactionsByDatesHistory.getTransactionsHistory()
                    .put(new Random().nextLong(), transactionData.getHash());
        });
        addressTransactionsByDatesHistories.put(addressTransactionsByDatesHistory);
        return transactionsList;
    }
}