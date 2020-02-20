package io.coti.historynode.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.*;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.services.liveview.LiveViewService;
import io.coti.historynode.crypto.GetTransactionsByAddressRequestCrypto;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.data.AddressTransactionsByDate;
import io.coti.historynode.database.RocksDBConnector;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import io.coti.historynode.http.GetTransactionsByDateRequest;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.ContentCachingResponseWrapper;
import utils.HashTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static utils.TestConstants.MAX_TRUST_SCORE;
import static utils.TransactionTestUtils.createRandomTransaction;
import static utils.TransactionTestUtils.generateRandomHash;


@ContextConfiguration(classes = {TransactionService.class,
        Transactions.class, RocksDBConnector.class,
        AddressTransactionsByDates.class, AddressTransactionsByAddresses.class,
        IDatabaseConnector.class, BaseNodeRocksDBConnector.class, TransactionService.class,
        GetTransactionsByAddressRequestCrypto.class, TransactionHelper.class, StorageConnector.class,
        AddressTransactionsHistories.class, TransactionCrypto.class, NodeCryptoHelper.class, BaseNodeBalanceService.class,
        BaseNodeConfirmationService.class, LiveViewService.class, TransactionIndexService.class, TransactionIndexes.class,
        ClusterService.class, JacksonSerializer.class, ChunkService.class, HttpJacksonSerializer.class, NodeCryptoHelper.class,
        ExpandedTransactionTrustScoreCrypto.class, BaseNodeValidationService.class, TransactionSenderCrypto.class, BaseNodePotService.class,
        LiveViewService.class, ClusterService.class, SimpMessagingTemplate.class, MessageChannel.class, SourceSelector.class,
        TrustChainConfirmationService.class, ClusterHelper.class
})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class HistoryTransactionServiceIntegrationTest {

    @Value("${storage.server.address}")
    protected String storageServerAddress;
    @Value("${global.private.key}")
    protected String globalPrivateKey;
    @Value("${historynode.seed}")
    private String seed;


    @Autowired
    private RocksDBConnector rocksDBConnector;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private StorageConnector storageConnector;
    @Autowired
    private AddressTransactionsByDates addressTransactionsByDates;
    @Autowired
    private AddressTransactionsByAddresses addressTransactionsByAddresses;
    @Autowired
    public IDatabaseConnector databaseConnector;
    @Autowired
    private BaseNodeRocksDBConnector baseNodeRocksDBConnector;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private TransactionSenderCrypto transactionSenderCrypto;
    @Autowired
    private IPotService potService;
    @Autowired
    private ISourceSelector sourceSelector;
    @Autowired
    private TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    private IClusterHelper clusterHelper;


    @Autowired
    private GetTransactionsByAddressRequestCrypto getTransactionsByAddressRequestCrypto;
    @Autowired
    private ChunkService chunkService;
    @Autowired
    private JacksonSerializer jacksonSerializer;
    @Autowired
    private HttpJacksonSerializer httpJacksonSerializer;

    @Autowired
    private LiveViewService liveViewService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @Autowired
    private BaseNodeValidationService baseNodeValidationService;
    @MockBean
    private BaseNodeDspVoteService baseNodeDspVoteService;
    @MockBean
    private SimpMessagingTemplate messagingSender;


    @Before
    public void init() {
        rocksDBConnector.setColumnFamily();
        databaseConnector.init();
    }


    //            @Test
    public void indexTransactions_differentBaseTransactionsTypes_indexOfTransactionsMatch() {
        TransactionData transactionData = createRandomTransaction();
        Hash transactionHash = transactionData.getHash();
        Instant attachmentTime = Instant.now();
        transactionData.setAttachmentTime(attachmentTime);
        transactionData.setSenderHash(generateRandomHash());

        ReceiverBaseTransactionData receiverBaseTransaction =
                new ReceiverBaseTransactionData(generateRandomHash(), new BigDecimal(7), new BigDecimal(8), Instant.now());
        transactionData.getBaseTransactions().add(receiverBaseTransaction);
        Hash receiverBaseTransactionAddressHash = receiverBaseTransaction.getAddressHash();

        InputBaseTransactionData inputBaseTransaction =
                new InputBaseTransactionData(generateRandomHash(), new BigDecimal(-6), Instant.now());
        transactionData.getBaseTransactions().add(inputBaseTransaction);
        Hash inputBaseTransactionAddressHash = receiverBaseTransaction.getAddressHash();

        transactionService.addToHistoryTransactionIndexes(transactionData);

        Hash hashByDate = transactionService.calculateHashByTime(attachmentTime);
        AddressTransactionsByDate transactionHashesByDateAddress = addressTransactionsByDates.getByHash(hashByDate);
        Assert.assertTrue(transactionHashesByDateAddress.getTransactionHashes().contains(transactionData.getHash()));

        LocalDate attachmentLocalDate = transactionService.calculateInstantLocalDate(attachmentTime);
        AddressTransactionsByAddress transactionHashesBySenderAddress =
                addressTransactionsByAddresses.getByHash(inputBaseTransactionAddressHash);
        Assert.assertTrue(transactionHashesBySenderAddress.getTransactionHashesByDates().get(attachmentLocalDate).contains(transactionHash));
        AddressTransactionsByAddress transactionHashesByReceiverAddress =
                addressTransactionsByAddresses.getByHash(receiverBaseTransactionAddressHash);
        Assert.assertTrue(transactionHashesByReceiverAddress.getTransactionHashesByDates().get(attachmentLocalDate).contains(transactionHash));
    }

    //            @Test
    public void indexTransactions_getTransactionsHashesByDate_indexOfTransactionsMatch() throws JsonProcessingException {
        // Generate transactions data
        int numberOfDays = 5;
        int numberOfLocalTransactions = 5;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, true, true);

        GetTransactionsByDateRequest request = new GetTransactionsByDateRequest();
        request.setDate(transactionService.calculateInstantLocalDate(Instant.now()));

        List<Hash> transactionsHashesToRetrieve = transactionService.getTransactionHashesByDate(request.getDate());
        Assert.assertTrue(transactionsHashesToRetrieve.containsAll(generatedTransactionsData.stream().map(TransactionData::getHash).collect(Collectors.toList())));

        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, false, false);
        generatedTransactionsData.forEach(transactionData -> {
            List<Hash> transactionsHashesToRetrieve2 = transactionService.getTransactionHashesByDate(transactionService.calculateInstantLocalDate(transactionData.getAttachmentTime()));
            Assert.assertTrue(transactionsHashesToRetrieve2.contains(transactionData.getHash()));
        });
    }

    //            @Test
    public void indexTransactions_getTransactionsHashesToRetrieve_indexOfTransactionsMatch() throws JsonProcessingException {
        // Generate transactions data
        int numberOfDays = 5;
        int numberOfLocalTransactions = 5;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, true, false);

        Hash address = generatedTransactionsData.get(0).getBaseTransactions().get(1).getAddressHash();
        LocalDate startDate = transactionService.calculateInstantLocalDate(generatedTransactionsData.get(numberOfDays - 2).getAttachmentTime());
        LocalDate endDate = transactionService.calculateInstantLocalDate(generatedTransactionsData.get(1).getAttachmentTime());

        GetTransactionsByAddressRequest request = new GetTransactionsByAddressRequest();
        request.setAddress(address);
        request.setStartDate(startDate);
        request.setEndDate(null);
        List<Hash> transactionsHashesToRetrieve = transactionService.getTransactionHashesToRetrieve(request);
        Assert.assertEquals(numberOfDays - 1, transactionsHashesToRetrieve.size());

        request.setAddress(address);
        request.setStartDate(null);
        request.setEndDate(endDate);
        transactionsHashesToRetrieve = transactionService.getTransactionHashesToRetrieve(request);
        Assert.assertEquals(numberOfDays - 1, transactionsHashesToRetrieve.size());

        request.setAddress(address);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        transactionsHashesToRetrieve = transactionService.getTransactionHashesToRetrieve(request);
        Assert.assertEquals(numberOfDays - 1 - 1, transactionsHashesToRetrieve.size());

        request.setAddress(address);
        request.setStartDate(null);
        request.setEndDate(null);
        transactionsHashesToRetrieve = transactionService.getTransactionHashesToRetrieve(request);
        Assert.assertEquals(numberOfDays, transactionsHashesToRetrieve.size());

        request.setAddress(null);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        transactionsHashesToRetrieve = transactionService.getTransactionHashesToRetrieve(request);
        Assert.assertEquals(0, transactionsHashesToRetrieve.size());
    }

    //            @Test
    public void getTransactionsByDate_storeAndRetrieveByDateFromLocal_singleTransactionsMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        int numberOfLocalTransactions = 1;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, true, false);

        // Retrieve transactions according to various criteria
        GetTransactionsByDateRequest getTransactionsByDateRequest = new GetTransactionsByDateRequest();
        TransactionData transactionDataToRetrieve = generatedTransactionsData.get(0);
        LocalDate startDate = transactionService.calculateInstantLocalDate(transactionDataToRetrieve.getAttachmentTime());
        getTransactionsByDateRequest.setDate(startDate);

        MockHttpServletResponse response = new MockHttpServletResponse();
        transactionService.getTransactionsByDate(getTransactionsByDateRequest, response);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatusCode());
        Assert.assertTrue(responseStatus.equals(HttpStatus.OK));

        //TODO 7/23/2019 tomer: fix below
//        Assert.assertTrue(((HistoryTransactionResponse)transactionsByDatesResponse.getBody()).getHistoryTransactionResponseData().getHistoryTransactionResults().containsValue(transactionDataToRetrieve));
    }

    //            @Test
    public void getTransactionsByDate_storeAndRetrieveByDateFromElasticSearch_singleTransactionsMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        int numberOfLocalTransactions = 1;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, true, false);

        // Retrieve transactions according to various criteria
        GetTransactionsByDateRequest getTransactionsByDateRequest = new GetTransactionsByDateRequest();
        TransactionData transactionDataToRetrieve = generatedTransactionsData.get(generatedTransactionsData.size() - 1);
        LocalDate startDate = transactionService.calculateInstantLocalDate(transactionDataToRetrieve.getAttachmentTime());
        getTransactionsByDateRequest.setDate(startDate);

        MockHttpServletResponse response = new MockHttpServletResponse();
        transactionService.getTransactionsByDate(getTransactionsByDateRequest, response);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatusCode());
        Assert.assertTrue(responseStatus.equals(HttpStatus.OK));

        //TODO 7/23/2019 tomer: fix below
//        Assert.assertTrue(((HistoryTransactionResponse)transactionsByDatesResponse.getBody()).getHistoryTransactionResponseData().getHistoryTransactionResults().containsValue(transactionDataToRetrieve));
    }

    //            @Test
    public void getTransactionsByDate_storeAndRetrieveByDate_multipleTransactionsMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        int numberOfLocalTransactions = 1;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, true, true);

        // Retrieve transactions according to various criteria
        GetTransactionsByDateRequest getTransactionsByDateRequest = new GetTransactionsByDateRequest();
        TransactionData transactionDataToRetrieve = generatedTransactionsData.get(generatedTransactionsData.size() - 1);
        LocalDate startDate = transactionService.calculateInstantLocalDate(transactionDataToRetrieve.getAttachmentTime());
        getTransactionsByDateRequest.setDate(startDate);

        MockHttpServletResponse response = new MockHttpServletResponse();
        transactionService.getTransactionsByDate(getTransactionsByDateRequest, response);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatusCode());
        Assert.assertTrue(responseStatus.equals(HttpStatus.OK));

        //TODO 7/23/2019 tomer: fix below
//        generatedTransactionsData.forEach(transactionData -> {
//            Assert.assertTrue(((HistoryTransactionResponse)transactionsByDatesResponse.getBody()).getHistoryTransactionResponseData().getHistoryTransactionResults().containsValue(transactionData));
//                });
    }

    //            @Test
    public void getTransactionsByAddress_storeAndRetrieveByAddressAndDates_multipleTransactionsMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        int numberOfLocalTransactions = 1;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, true, false);

        // Retrieve transactions according to various criteria
        GetTransactionsByAddressRequest getTransactionsRequestByDates = new GetTransactionsByAddressRequest();
        LocalDate startDate = transactionService.calculateInstantLocalDate(generatedTransactionsData.get(generatedTransactionsData.size() - 1).getAttachmentTime());
        LocalDate endDate = transactionService.calculateInstantLocalDate(generatedTransactionsData.get(0).getAttachmentTime());
        getTransactionsRequestByDates.setAddress(generatedTransactionsData.get(0).getBaseTransactions().get(0).getAddressHash());
        getTransactionsRequestByDates.setStartDate(startDate);
        getTransactionsRequestByDates.setEndDate(endDate);


        MockHttpServletResponse response = new MockHttpServletResponse();
        //        HttpServletResponse response = null;
        transactionService.getTransactionsByAddress(getTransactionsRequestByDates, response);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatusCode());
        Assert.assertTrue(responseStatus.equals(HttpStatus.OK));

        //TODO 7/23/2019 tomer: fix below
//        for( int i = 0 ; i<numberOfDays; i++) {
//            Assert.assertTrue(((HistoryTransactionResponse)transactionsByDatesResponse.getBody()).getHistoryTransactionResponseData()
//                    .getHistoryTransactionResults().get(generatedTransactionsData.get(i).getHash()).equals(generatedTransactionsData.get(i)));
//        }
    }


    //            @Test
    public void getTransactionsByAddress_storeAndRetrieveByAddress_multipleTransactionsMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        int numberOfLocalTransactions = 1;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, true, false);

        // Retrieve transactions according to various criteria
        GetTransactionsByAddressRequest getTransactionsRequestByDates = new GetTransactionsByAddressRequest();
        getTransactionsRequestByDates.setAddress(generatedTransactionsData.get(0).getBaseTransactions().get(0).getAddressHash());

        MockHttpServletResponse response = new MockHttpServletResponse();
        transactionService.getTransactionsByAddress(getTransactionsRequestByDates, response);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatusCode());
        Assert.assertTrue(responseStatus.equals(HttpStatus.OK));

        //TODO 7/23/2019 tomer: fix below
//        for( int i = 0 ; i<numberOfDays; i++) {
//            Assert.assertTrue(((HistoryTransactionResponse)transactionsByDatesResponse.getBody()).getHistoryTransactionResponseData()
//                    .getHistoryTransactionResults().get(generatedTransactionsData.get(i).getHash()).equals(generatedTransactionsData.get(i)));
//        }
    }

    //            @Test
    public void getTransactionsByAddress_storeAndRetrieveByAddress_singleTransactionsMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        int numberOfLocalTransactions = 1;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, false, false);

        // Retrieve transactions according to various criteria
        GetTransactionsByAddressRequest getTransactionsByAddressRequest = new GetTransactionsByAddressRequest();
        getTransactionsByAddressRequest.setAddress(generatedTransactionsData.get(2).getBaseTransactions().get(1).getAddressHash());
        Hash userHash = generateRandomHash();
        getTransactionsByAddressRequest.setUserHash(userHash);

        MockHttpServletResponse response = new MockHttpServletResponse();
        transactionService.getTransactionsByAddress(getTransactionsByAddressRequest, response);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatusCode());
        Assert.assertTrue(responseStatus.equals(HttpStatus.OK));

        //TODO 7/23/2019 tomer: fix below
//        Assert.assertTrue(((HistoryTransactionResponse)transactionsByAddressResponse.getBody()).getHistoryTransactionResponseData()
//                .getHistoryTransactionResults().get(generatedTransactionsData.get(2).getHash()).equals(generatedTransactionsData.get(2)));
    }

    //            @Test
    public void getTransactionsByAddress_storeAndRetrieveByDatesNoAddress_noTransactionsMatched() throws IOException {
        // Generate transactions data
        int numberOfDays = 4;
        int numberOfLocalTransactions = 1;

        ArrayList<TransactionData> generatedTransactionsData = new ArrayList<>();
        generateIndexAndStoreTransactions(numberOfDays, generatedTransactionsData, numberOfLocalTransactions, false, false);

        // Retrieve transactions according to various criteria
        GetTransactionsByAddressRequest getTransactionsRequestByDates = new GetTransactionsByAddressRequest();
        LocalDate startDate = transactionService.calculateInstantLocalDate(generatedTransactionsData.get(generatedTransactionsData.size() - 1).getAttachmentTime());
        LocalDate endDate = transactionService.calculateInstantLocalDate(generatedTransactionsData.get(0).getAttachmentTime());
        getTransactionsRequestByDates.setStartDate(startDate);
        getTransactionsRequestByDates.setEndDate(endDate);

        MockHttpServletResponse response = new MockHttpServletResponse();
        transactionService.getTransactionsByAddress(getTransactionsRequestByDates, response);

        // Expected Address in request to not be null
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatusCode());
        Assert.assertTrue(responseStatus.equals(HttpStatus.OK));
    }


    private void generateIndexAndStoreTransactions(int numberOfDays, ArrayList<TransactionData> generatedTransactionsData, int numberOfLocalTransactions, boolean singleAddress, boolean singleAttachmentDate) throws JsonProcessingException {
        Instant attachmentTime = Instant.now();
        for (int days = 0; days < numberOfDays; days++) {
            generatedTransactionsData.add(generateInitialTransactionByAttachmentDate(attachmentTime.minus(days, ChronoUnit.DAYS)));
        }

//        if (singleAddress) {
//            Hash rbtAddress = generatedTransactionsData.get(0).getBaseTransactions().get(1).getAddressHash();
//            generatedTransactionsData.forEach(transactionData -> transactionData.getBaseTransactions().get(1).setAddressHash(rbtAddress));
//        }

        if (singleAttachmentDate) {
            Instant onlyAttachmentTime = generatedTransactionsData.get(0).getAttachmentTime();
            generatedTransactionsData.forEach(transactionData -> transactionData.setAttachmentTime(onlyAttachmentTime));
        }

        // Update indexing with generated transactions data
        for (TransactionData generatedTransactionData : generatedTransactionsData) {
            transactionService.addToHistoryTransactionIndexes(generatedTransactionData);
        }

        // Store some of the transactions data locally
        for (int num = 0; num < numberOfLocalTransactions; num++) {
            transactions.put(generatedTransactionsData.get(num));
        }

        // Store the rest of the transactions in storage
        AddEntitiesBulkRequest addEntitiesBulkRequest = new AddEntitiesBulkRequest();
        Map<Hash, String> hashToEntityJsonDataMap = new HashMap<>();

        for (int j = numberOfLocalTransactions; j < numberOfDays; j++) {
            hashToEntityJsonDataMap.put(generatedTransactionsData.get(j).getHash(), jacksonSerializer.serializeAsString(generatedTransactionsData.get(j)));
        }
        if (hashToEntityJsonDataMap.isEmpty()) {
            return;
        }
        addEntitiesBulkRequest.setHashToEntityJsonDataMap(hashToEntityJsonDataMap);

        String endpoint = "/transactions";
        ResponseEntity<AddHistoryEntitiesResponse> storeResponseEntity = transactionService.storeEntitiesByType(storageServerAddress + endpoint, addEntitiesBulkRequest);
        Assert.assertTrue(storeResponseEntity.getBody().getHashToStoreResultMap().size() == addEntitiesBulkRequest.getHashToEntityJsonDataMap().size());
        Assert.assertTrue(storeResponseEntity.getBody().getHashToStoreResultMap().values().stream().allMatch(Boolean::booleanValue));
    }


    private TransactionData generateInitialTransactionByAttachmentDate(Instant attachmentTime) {
        BigDecimal amount = new BigDecimal(4);
        int genesisAddressIndex = 2;
        Hash cotiGenesisAddress = nodeCryptoHelper.generateAddress(seed, genesisAddressIndex);
        Hash fundAddress = HashTestUtils.generateRandomAddressHash();


        List<BaseTransactionData> baseTransactions = new ArrayList<>();

        InputBaseTransactionData ibt = new InputBaseTransactionData(cotiGenesisAddress, amount.multiply(new BigDecimal(-1)), Instant.now());

        ReceiverBaseTransactionData rbt = new ReceiverBaseTransactionData(fundAddress, amount, amount, Instant.now());
        baseTransactions.add(ibt);
        baseTransactions.add(rbt);

        double trustScore = MAX_TRUST_SCORE;
        TransactionData initialTransactionData = new TransactionData(baseTransactions, TransactionType.Initial.toString(), trustScore, Instant.now(), TransactionType.Initial);

//        if (!balanceService.checkBalancesAndAddToPreBalance(initialTransactionData.getBaseTransactions())) {
//            throw new TransactionValidationException("Balance check failed");
//        }
        clusterService.selectSources(initialTransactionData);
        initialTransactionData.setAttachmentTime(attachmentTime);

        Map<Hash, Integer> addressHashToAddressIndexMap = new HashMap<>();
        addressHashToAddressIndexMap.put(cotiGenesisAddress, genesisAddressIndex);

        signBaseTransactions(initialTransactionData, addressHashToAddressIndexMap);
        transactionCrypto.signMessage(initialTransactionData);
//        transactionHelper.attachTransactionToCluster(initialTransactionData);
//        propagationPublisher.propagate(initialTransactionData, Arrays.asList(NodeType.ZeroSpendServer, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.DspNode, NodeType.HistoryNode));

        return initialTransactionData;
    }

    public void signBaseTransactions(TransactionData transactionData, Map<Hash, Integer> addressHashToAddressIndexMap) {

        try {
            if (transactionData.getHash() == null) {
                transactionCrypto.setTransactionHash(transactionData);
            }
            for (BaseTransactionData baseTransactionData : transactionData.getInputBaseTransactions()) {
                BaseTransactionCrypto.getByBaseTransactionClass(baseTransactionData.getClass()).signMessage(transactionData, baseTransactionData, addressHashToAddressIndexMap.get(baseTransactionData.getAddressHash()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}