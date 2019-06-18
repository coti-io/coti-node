package io.coti.historynode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.historynode.database.HistoryRocksDBConnector;
import io.coti.historynode.model.AddressTransactionsByDatesHistories;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Date;

import static utils.TestUtils.createRandomTransaction;
import static utils.TestUtils.createRandomTransactionWithSenderAddress;

@ContextConfiguration(classes = { AddressTransactionsService.class,
        AddressTransactionsByDatesHistories.class,
        HistoryRocksDBConnector.class
})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressTransactionsServiceTest {

    @Autowired
    private AddressTransactionsByDatesHistories addressTransactionsByDatesHistories;

    @Autowired
    private AddressTransactionsService addressTransactionsService;

    @Autowired
    public HistoryRocksDBConnector databaseConnector;

    @Before
    public void init() {
        databaseConnector.init();
    }

    @Test
    public void saveToAddressTransactionsHistories() {
        TransactionData transactionData = createRandomTransactionWithSenderAddress();
        transactionData.setAttachmentTime(Instant.now());

        addressTransactionsService.saveToAddressTransactionsHistories(transactionData);

        Assert.assertNotNull(addressTransactionsByDatesHistories.getByHash(transactionData.getSenderHash()));
    }
}