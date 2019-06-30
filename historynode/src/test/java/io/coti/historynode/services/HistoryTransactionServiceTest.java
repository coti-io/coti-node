package io.coti.historynode.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.ReceiverBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.historynode.crypto.GetTransactionsByAddressRequestCrypto;
import io.coti.historynode.crypto.TransactionsRequestCrypto;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.data.AddressTransactionsByDate;
import io.coti.historynode.data.AddressTransactionsByDatesHistory;
import io.coti.historynode.database.HistoryRocksDBConnector;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsRequest;
import io.coti.historynode.http.GetTransactionsRequestOld;
import io.coti.historynode.http.HistoryTransactionResponse;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
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
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Value;

import static utils.TestUtils.*;
import static utils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {HistoryTransactionService.class,
        AddressTransactionsByDatesHistories.class,
        HistoryRocksDBConnector.class,
        Transactions.class,
        TransactionIndexingService.class, AddressTransactionsByDates.class, AddressTransactionsByAddresses.class,
        IDatabaseConnector.class, BaseNodeRocksDBConnector.class, HistoryTransactionService.class, GetTransactionsByAddressRequestCrypto.class,
        TransactionsRequestCrypto.class
})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class HistoryTransactionServiceTest {

    @Value("${storage.server.address}")
    protected String storageServerAddress;

    public static final int NUMBER_OF_ADDRESSES_PER_SENDER = 8;
    public static final int NUMBER_OF_SENDERS = 3;

    @Autowired
    private HistoryTransactionService historyTransactionService;

    @Autowired
    private AddressTransactionsByDatesHistories addressTransactionsByDatesHistories;

    @Autowired
    private Transactions transactions;

    @MockBean
    private IStorageConnector storageConnector;

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
    private HistoryRocksDBConnector historyRocksDBConnector;

    @Autowired
    private GetTransactionsByAddressRequestCrypto getTransactionsByAddressRequestCrypto;
    @Autowired
    private TransactionsRequestCrypto transactionsRequestCrypto;

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
    public void getTransactionsDetails() {
        GetTransactionsRequestOld getTransactionsRequestOld = new GetTransactionsRequestOld();
        IntStream.range(0, NUMBER_OF_SENDERS).forEachOrdered(n -> {
            Hash senderAddress = generateRandomHash();
            storeInDbRandomTransactionsPerSender(senderAddress);
            getTransactionsRequestOld.getAddressesHashes().add(senderAddress);
        });

        ResponseEntity<IResponse> response = historyTransactionService.getTransactionsDetails(getTransactionsRequestOld);
        GetTransactionsResponse getTransactionsResponse = (GetTransactionsResponse) (response.getBody());

        Assert.assertTrue(getTransactionsResponse.getTransactionsData().size() == NUMBER_OF_ADDRESSES_PER_SENDER * NUMBER_OF_SENDERS);
    }

//    @Test
    public void deleteLocalUnconfirmedTransactions() {
        List<TransactionData> transactionsList = new ArrayList<>();
        TransactionData transactionData = createRandomTransaction();
        transactionsList.add(transactionData);
        transactions.put(transactionData);

        historyTransactionService.deleteLocalUnconfirmedTransactions();

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


    @Test
    public void getTransactionsByAddress_NoExceptions() {
        GetTransactionsByAddressRequest request = new GetTransactionsByAddressRequest();
        request.setAddress(generateRandomHash());
        historyTransactionService.getTransactionsByAddress(request);
    }

    @Test
    public void addToHistoryIndexes() {
        TransactionData transactionData = createRandomTransaction();
        Hash transactionHash = transactionData.getHash();
        Instant attachmentTime = Instant.now();
        transactionData.setAttachmentTime(attachmentTime);
        transactionData.setSenderHash(generateRandomHash());
        ReceiverBaseTransactionData receiverBaseTransaction =
                new ReceiverBaseTransactionData(generateRandomHash(), new BigDecimal(7), new BigDecimal(8), Instant.now());
        transactionData.getBaseTransactions().add(receiverBaseTransaction);
        @NotNull Hash receiverAddressHash = receiverBaseTransaction.getAddressHash();

        historyTransactionService.addToHistoryTransactionIndexes(transactionData);

        Hash hashByDate = historyTransactionService.calculateHashByAttachmentTime(attachmentTime);
        AddressTransactionsByDate transactionHashesByDateAddress = addressTransactionsByDates.getByHash(hashByDate);
        Assert.assertTrue(transactionHashesByDateAddress.getTransactionsAddresses().contains(transactionData.getHash()));
//        Assert.assertTrue(transactionHashesByDateAddress.getTransactionsAddresses().contains(receiverAddressHash));

        LocalDate attachmentLocalDate = historyTransactionService.calculateInstantLocalDate(attachmentTime);
        AddressTransactionsByAddress transactionHashesBySenderAddress =
                addressTransactionsByAddresses.getByHash(transactionData.getSenderHash());
        Assert.assertTrue(transactionHashesBySenderAddress.getTransactionHashesByDates().get(attachmentLocalDate).contains(transactionHash));
        AddressTransactionsByAddress transactionHashesByReceiverAddress =
                addressTransactionsByAddresses.getByHash(receiverAddressHash);
        Assert.assertTrue(transactionHashesByReceiverAddress.getTransactionHashesByDates().get(attachmentLocalDate).contains(transactionHash));
    }

    @Test
    public void getTransactionsByAddress_storeAndRetrieve_singleTransactionMatched() throws IOException {
        Instant attachmentTime = Instant.now();
        TransactionData transactionData = generateTransactionDataWithRBTByAttachmentDate(attachmentTime);
        Hash senderHash = transactionData.getSenderHash();
        Hash transactionHash = transactionData.getHash();

        historyTransactionService.addToHistoryTransactionIndexes(transactionData);

        RestTemplate restTemplate = new RestTemplate();
        String endpoint = "/transactions";
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        AddEntitiesBulkRequest addEntitiesBulkRequest = new AddEntitiesBulkRequest();
        Map<Hash, String> hashToEntityJsonDataMap = new HashMap<>();
        String transactionJson = mapper.writeValueAsString(transactionData);
        hashToEntityJsonDataMap.put(transactionHash, transactionJson);
        addEntitiesBulkRequest.setHashToEntityJsonDataMap(hashToEntityJsonDataMap);

        restTemplate.put(storageServerAddress + endpoint,  addEntitiesBulkRequest);

        GetTransactionsByAddressRequest getTransactionByAddressRequest = new GetTransactionsByAddressRequest();
        getTransactionByAddressRequest.setAddress(senderHash);
        @NotNull @Valid Hash userHash = generateRandomHash();
        getTransactionByAddressRequest.setUserHash(userHash);

        ResponseEntity<IResponse> transactionsByAddressResponse = historyTransactionService.getTransactionsByAddress(getTransactionByAddressRequest);

        Assert.assertTrue(((HistoryTransactionResponse)transactionsByAddressResponse.getBody()).getHistoryTransactionResponseData()
                .getHistoryTransactionResults().get(transactionHash).equals(transactionData));
        int iPause = 7;
    }

    @Test
    public void getTransactions_storeAndRetrieve_singleTransactionMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        Instant attachmentTime = Instant.now();
        for(int days = 0; days<numberOfDays; days++) {
            generatedTransactionsData.add(generateTransactionDataWithRBTByAttachmentDate(attachmentTime.minus(days, ChronoUnit.DAYS)));
        }

        // Update indexing with generated transactions data
        for (TransactionData generatedTransactionData : generatedTransactionsData) {
            historyTransactionService.addToHistoryTransactionIndexes(generatedTransactionData);
        }

        // Store some of the transactions data locally
        //TODO 6/30/2019 tomer: implement this
        transactions.put(generatedTransactionsData.get(0));

        // Store the rest of the transactions in storage
        AddEntitiesBulkRequest addEntitiesBulkRequest = new AddEntitiesBulkRequest();
        Map<Hash, String> hashToEntityJsonDataMap = new HashMap<>();

        for( int j = 1; j <numberOfDays; j++ ) {
            hashToEntityJsonDataMap.put(generatedTransactionsData.get(j).getHash(),mapper.writeValueAsString(generatedTransactionsData.get(j)));
        }
        addEntitiesBulkRequest.setHashToEntityJsonDataMap(hashToEntityJsonDataMap);

        RestTemplate restTemplate = new RestTemplate();
        String endpoint = "/transactions";
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        restTemplate.put(storageServerAddress + endpoint,  addEntitiesBulkRequest);


        // Retrieve transactions according to various criteria

//        GetTransactionsRequest getTransactionsRequestByAddress = new GetTransactionsRequest();
//        getTransactionsRequestByAddress.setAddress(generatedTransactionsData.get(2).getSenderHash());
//        @NotNull @Valid Hash userHash = generateRandomHash();
//        getTransactionsRequestByAddress.setUserHash(userHash);
//
//        ResponseEntity<IResponse> transactionsByAddressResponse = historyTransactionService.getTransactions(getTransactionsRequestByAddress);
//        Assert.assertTrue(((HistoryTransactionResponse)transactionsByAddressResponse.getBody()).getHistoryTransactionResponseData()
//                .getHistoryTransactionResults().get(generatedTransactionsData.get(2).getHash()).equals(generatedTransactionsData.get(2)));

        GetTransactionsRequest getTransactionsRequestByDates = new GetTransactionsRequest();
        Instant startDate = generatedTransactionsData.get(generatedTransactionsData.size()-1).getAttachmentTime();
        Instant endDate = generatedTransactionsData.get(0).getAttachmentTime();
        getTransactionsRequestByDates.setStartDate(startDate);
        getTransactionsRequestByDates.setEndDate(endDate);

        ResponseEntity<IResponse> transactionsByDatesResponse = historyTransactionService.getTransactions(getTransactionsRequestByDates);



        int iPause = 7;





    }

    private TransactionData generateTransactionDataWithRBTByAttachmentDate(Instant attachmentTime) {
        TransactionData transactionData = createRandomTransaction();

        transactionData.setAttachmentTime(attachmentTime);
        transactionData.setSenderHash(generateRandomHash());
        ReceiverBaseTransactionData receiverBaseTransaction =
                new ReceiverBaseTransactionData(generateRandomHash(), new BigDecimal(7), new BigDecimal(8), Instant.now());
        transactionData.getBaseTransactions().add(receiverBaseTransaction);
//        @NotNull Hash receiverAddressHash = receiverBaseTransaction.getAddressHash();
        return transactionData;
    }

}