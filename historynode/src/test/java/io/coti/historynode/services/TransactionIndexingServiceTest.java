package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.ReceiverBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.data.AddressTransactionsByDate;
import io.coti.historynode.database.HistoryRocksDBConnector;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
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

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static utils.TestUtils.createRandomTransaction;
import static utils.TestUtils.generateRandomHash;

@Deprecated
@ContextConfiguration(classes = {TransactionIndexingService.class, AddressTransactionsByDates.class, AddressTransactionsByAddresses.class,
        IDatabaseConnector.class, BaseNodeRocksDBConnector.class, AddressTransactionsByDatesHistories.class, HistoryRocksDBConnector.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionIndexingServiceTest {

    private static final int ANY_NUMBER = 7;
    @Autowired
    private TransactionIndexingService transactionIndexingService;
    @Autowired
    private AddressTransactionsByDates addressTransactionsByDates;
    @Autowired
    private AddressTransactionsByAddresses addressTransactionsByAddresses;
    @Autowired
    public IDatabaseConnector databaseConnector;
    @Autowired
    private BaseNodeRocksDBConnector baseNodeRocksDBConnector;
    @Autowired
    private AddressTransactionsByDatesHistories addressTransactionsByDatesHistories;
    @Autowired
    private HistoryRocksDBConnector historyRocksDBConnector;

    private ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
        historyRocksDBConnector.setColumnFamily();
        databaseConnector.init();
    }

//    @Test
//    public void addToHistoryIndexes() {
//        TransactionData transactionData = createRandomTransaction();
//        Hash transactionHash = transactionData.getHash();
//        Instant attachmentTime = Instant.now();
//        transactionData.setAttachmentTime(attachmentTime);
//        transactionData.setSenderHash(generateRandomHash());
//        ReceiverBaseTransactionData receiverBaseTransaction =
//                new ReceiverBaseTransactionData(generateRandomHash(), new BigDecimal(7), new BigDecimal(8), Instant.now());
//        transactionData.getBaseTransactions().add(receiverBaseTransaction);
//        @NotNull Hash receiverAddressHash = receiverBaseTransaction.getAddressHash();
//
//        transactionIndexingService.addToHistoryTransactionIndexes(transactionData);
//
//        Hash hashByDate = transactionIndexingService.calculateHashByAttachmentTime(attachmentTime);
//        AddressTransactionsByDate transactionHashesByDateAddress = addressTransactionsByDates.getByHash(hashByDate);
//        Assert.assertTrue(transactionHashesByDateAddress.getTransactionsAddresses().contains(transactionData.getHash()));
////        Assert.assertTrue(transactionHashesByDateAddress.getTransactionsAddresses().contains(receiverAddressHash));
//
//        LocalDate attachmentLocalDate = transactionIndexingService.calculateInstantLocalDate(attachmentTime);
//        AddressTransactionsByAddress transactionHashesBySenderAddress =
//                addressTransactionsByAddresses.getByHash(transactionData.getSenderHash());
//        Assert.assertTrue(transactionHashesBySenderAddress.getTransactionHashesByDates().get(attachmentLocalDate).contains(transactionHash));
//        AddressTransactionsByAddress transactionHashesByReceiverAddress =
//                addressTransactionsByAddresses.getByHash(receiverAddressHash);
//        Assert.assertTrue(transactionHashesByReceiverAddress.getTransactionHashesByDates().get(attachmentLocalDate).contains(transactionHash));
//    }

}
