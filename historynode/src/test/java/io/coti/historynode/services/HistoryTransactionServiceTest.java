//package io.coti.historynode.services;
//
//import io.coti.basenode.communication.JacksonSerializer;
//import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
//import io.coti.basenode.crypto.NodeCryptoHelper;
//import io.coti.basenode.crypto.TransactionCrypto;
//import io.coti.basenode.crypto.TransactionSenderCrypto;
//import io.coti.basenode.data.Hash;
//import io.coti.basenode.data.TransactionData;
//import io.coti.basenode.database.BaseNodeRocksDBConnector;
//import io.coti.basenode.database.interfaces.IDatabaseConnector;
//import io.coti.basenode.http.HttpJacksonSerializer;
//import io.coti.basenode.model.AddressTransactionsHistories;
//import io.coti.basenode.model.TransactionIndexes;
//import io.coti.basenode.model.Transactions;
//import io.coti.basenode.services.*;
//import io.coti.basenode.services.interfaces.IBalanceService;
//import io.coti.basenode.services.interfaces.ITransactionHelper;
//import io.coti.basenode.services.liveview.LiveViewService;
//import io.coti.historynode.crypto.GetTransactionsByAddressRequestCrypto;
//import io.coti.historynode.data.AddressTransactionsByAddress;
//import io.coti.historynode.data.AddressTransactionsByDate;
//import io.coti.historynode.http.GetTransactionsByAddressRequest;
//import io.coti.historynode.http.GetTransactionsByDateRequest;
//import io.coti.historynode.model.AddressTransactionsByAddresses;
//import io.coti.historynode.model.AddressTransactionsByDates;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//import utils.TransactionTestUtils;
//
//import java.io.UnsupportedEncodingException;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static utils.TransactionTestUtils.generateRandomAddressHash;
//import static utils.TransactionTestUtils.generateRandomHash;
//
//@ContextConfiguration(classes = {TransactionService.class,
//        Transactions.class,
//        AddressTransactionsByDates.class, AddressTransactionsByAddresses.class,
//        IDatabaseConnector.class, BaseNodeRocksDBConnector.class, TransactionService.class,
//        GetTransactionsByAddressRequestCrypto.class, BaseNodeTransactionHelper.class, StorageConnector.class,
//        AddressTransactionsHistories.class, TransactionCrypto.class, NodeCryptoHelper.class, BaseNodeBalanceService.class,
//        BaseNodeConfirmationService.class, LiveViewService.class, TransactionIndexService.class, TransactionIndexes.class,
//        ClusterService.class, JacksonSerializer.class, ChunkService.class, HttpJacksonSerializer.class, NodeCryptoHelper.class,
//        ExpandedTransactionTrustScoreCrypto.class, BaseNodeValidationService.class, TransactionSenderCrypto.class, BaseNodePotService.class,
//        LiveViewService.class, ClusterService.class, SimpMessagingTemplate.class, MessageChannel.class, SourceSelector.class,
//        TrustChainConfirmationService.class, ClusterHelper.class
//})
//@TestPropertySource(locations = "classpath:test.properties")
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class HistoryTransactionServiceTest {
//
//    public static final String EMPTY_OUTPUT = "[]";
//
//    @Value("${storage.server.address}")
//    protected String storageServerAddress;
//    @Value("${global.private.key}")
//    protected String globalPrivateKey;
//    @Value("${historynode.seed}")
//    private String seed;
//
//
//    @Autowired
//    private TransactionService transactionService;
//    @Autowired
//    private StorageConnector storageConnector;
//    @MockBean
//    private GetTransactionsByAddressRequestCrypto getTransactionsByAddressRequestCrypto;
//    @Autowired
//    private ChunkService chunkService;
//    @Autowired
//    private HttpJacksonSerializer httpJacksonSerializer;
//    @Autowired
//    private JacksonSerializer jacksonSerializer;
//    @Autowired
//    private LiveViewService liveViewService;
//    @Autowired
//    private IBalanceService balanceService;
//    @Autowired
//    private ITransactionHelper transactionHelper;
//    @Autowired
//    private TransactionIndexService transactionIndexService;
//    @Autowired
//    private TransactionIndexes transactionIndexes;
//
//
//    @MockBean
//    private Transactions transactions;
//    @MockBean
//    private AddressTransactionsByDates addressTransactionsByDates;
//    @MockBean
//    private AddressTransactionsByAddresses addressTransactionsByAddresses;
//    @MockBean
//    private BaseNodeDspVoteService baseNodeDspVoteService;
//    @MockBean
//    private SimpMessagingTemplate messagingSender;
//
//    @Before
//    public void init() {
//    }
//
//
//    @Test
//    public void getTransactionHashesToRetrieve_noAddress_emptyResult() {
//        int amountOfDaysBack = 1;
//        Hash address = generateRandomAddressHash();
//        LocalDate startDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//        LocalDate endDate = transactionService.calculateInstantLocalDate(Instant.now());
//        GetTransactionsByAddressRequest getTransactionsByAddressRequest = getGetTransactionsByAddressRequest(null, startDate, endDate);
//        // No Address -> Empty result
//        List<Hash> transactionHashesToRetrieve =
//                transactionService.getTransactionHashesToRetrieve(getTransactionsByAddressRequest);
//        Assert.assertTrue(transactionHashesToRetrieve.isEmpty());
//        // Default Mock -> No AddressTransactionsByAddress -> Empty result
//        transactionHashesToRetrieve =
//                transactionService.getTransactionHashesToRetrieve(getGetTransactionsByAddressRequest(address, startDate, endDate));
//        Assert.assertTrue(transactionHashesToRetrieve.isEmpty());
//    }
//
//    @Test
//    public void getTransactionHashesToRetrieve_withAddressNoneInDates_emptyResult() {
//        int amountOfDaysBack = 1;
//        Hash address = generateRandomAddressHash();
//        LocalDate startDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//        LocalDate endDate = transactionService.calculateInstantLocalDate(Instant.now());
//
//        Hash transactionAddressHash = generateRandomAddressHash();
//        HashMap<LocalDate, HashSet<Hash>> transactionHashesByDate = new HashMap<>();
//        LocalDate earliestLocalDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//        // Mock -> AddressTransactionsByAddress with no entries -> Empty result
//        AddressTransactionsByAddress mockedAddressTransactionsByAddress = new AddressTransactionsByAddress(transactionAddressHash, transactionHashesByDate, earliestLocalDate);
//
//        when(addressTransactionsByAddresses.getByHash(address)).thenReturn(mockedAddressTransactionsByAddress);
//        List<Hash> transactionHashesToRetrieve =
//                transactionService.getTransactionHashesToRetrieve(getGetTransactionsByAddressRequest(address, startDate, endDate));
//        Assert.assertTrue(transactionHashesToRetrieve.isEmpty());
//    }
//
//    @Test
//    public void getTransactionHashesToRetrieve_withSingleAddress_resultMatched() {
//        int amountOfDaysBack = 1;
//        Hash address = generateRandomAddressHash();
//        LocalDate startDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//        LocalDate endDate = transactionService.calculateInstantLocalDate(Instant.now());
//
//        HashSet<Hash> transactionHashes = getTransactionHashesMocked(amountOfDaysBack, address, startDate);
//
//        List<Hash> transactionHashesToRetrieve =
//                transactionService.getTransactionHashesToRetrieve(getGetTransactionsByAddressRequest(address, startDate, endDate));
//        Assert.assertTrue(transactionHashesToRetrieve.containsAll(transactionHashes));
//    }
//
//    @Test
//    public void getTransactionHashesToRetrieve_withSingleAddressNoEndDate_resultMatched() {
//        int amountOfDaysBack = 1;
//        Hash address = generateRandomAddressHash();
//        LocalDate startDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//
//        HashSet<Hash> transactionHashes = getTransactionHashesMocked(amountOfDaysBack, address, startDate);
//
//        List<Hash> transactionHashesToRetrieve =
//                transactionService.getTransactionHashesToRetrieve(getGetTransactionsByAddressRequest(address, startDate, null));
//        Assert.assertTrue(transactionHashesToRetrieve.containsAll(transactionHashes));
//    }
//
//    @Test
//    public void getTransactionHashesToRetrieve_withSingleAddressNoStartDate_resultMatched() {
//        int amountOfDaysBack = 1;
//        Hash address = generateRandomAddressHash();
//        LocalDate startDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//        LocalDate endDate = transactionService.calculateInstantLocalDate(Instant.now());
//
//        HashSet<Hash> transactionHashes = getTransactionHashesMocked(amountOfDaysBack, address, startDate);
//
//        List<Hash> transactionHashesToRetrieve =
//                transactionService.getTransactionHashesToRetrieve(getGetTransactionsByAddressRequest(address, null, endDate));
//        Assert.assertTrue(transactionHashesToRetrieve.containsAll(transactionHashes));
//    }
//
//    @Test
//    public void getTransactionHashesToRetrieve_withSingleAddressNoDates_resultMatched() {
//        int amountOfDaysBack = 1;
//        Hash address = generateRandomAddressHash();
//        LocalDate startDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//
//        HashSet<Hash> transactionHashes = getTransactionHashesMocked(amountOfDaysBack, address, startDate);
//
//        List<Hash> transactionHashesToRetrieve =
//                transactionService.getTransactionHashesToRetrieve(getGetTransactionsByAddressRequest(address, null, null));
//        Assert.assertTrue(transactionHashesToRetrieve.containsAll(transactionHashes));
//    }
//
//
//    protected HashSet<Hash> getTransactionHashesMocked(int amountOfDaysBack, Hash address, LocalDate startDate) {
//        Hash transactionAddressHash = generateRandomAddressHash();
//        HashMap<LocalDate, HashSet<Hash>> transactionHashesByDate = new HashMap<>();
//        LocalDate earliestLocalDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//
//        HashSet<Hash> transactionHashes = new HashSet<>();
//        transactionHashes.add(generateRandomHash());
//        transactionHashesByDate.put(startDate, transactionHashes);
//        AddressTransactionsByAddress mockedAddressTransactionsByAddress = new AddressTransactionsByAddress(transactionAddressHash, transactionHashesByDate, earliestLocalDate);
//        when(addressTransactionsByAddresses.getByHash(address)).thenReturn(mockedAddressTransactionsByAddress);
//        return transactionHashes;
//    }
//
//    protected GetTransactionsByAddressRequest getGetTransactionsByAddressRequest(Hash address, LocalDate startDate, LocalDate endDate) {
//        GetTransactionsByAddressRequest request = new GetTransactionsByAddressRequest();
//        request.setAddress(address);
//        request.setStartDate(startDate);
//        request.setEndDate(endDate);
//        return request;
//    }
//
//    @Test
//    public void getTransactionsByAddress_invalidSignature_unauthorized() {
//        GetTransactionsByAddressRequest request = new GetTransactionsByAddressRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        when(getTransactionsByAddressRequestCrypto.verifySignature(any(GetTransactionsByAddressRequest.class))).thenReturn(Boolean.FALSE);
//
//        transactionService.getTransactionsByAddress(request, response);
//        Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
//    }
//
//    @Test
//    public void getTransactionsByAddress_validSignatureNoAddress_authorizedEmptyResponse() throws UnsupportedEncodingException {
//        GetTransactionsByAddressRequest request = new GetTransactionsByAddressRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        when(getTransactionsByAddressRequestCrypto.verifySignature(any(GetTransactionsByAddressRequest.class))).thenReturn(Boolean.TRUE);
//        transactionService.getTransactionsByAddress(request, response);
//        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Assert.assertEquals(EMPTY_OUTPUT, response.getContentAsString());
//    }
//
//    @Test
//    public void getTransactionsByAddress_validSignatureWithAddress_authorizedNotEmptyResponseFromLocal() throws UnsupportedEncodingException {
//        GetTransactionsByAddressRequest request = new GetTransactionsByAddressRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        when(getTransactionsByAddressRequestCrypto.verifySignature(any(GetTransactionsByAddressRequest.class))).thenReturn(Boolean.TRUE);
//
//        int amountOfDaysBack = 1;
//        Hash address = generateRandomAddressHash();
//        LocalDate startDate = transactionService.calculateInstantLocalDate(Instant.now().minus(amountOfDaysBack, ChronoUnit.DAYS));
//
//        HashSet<Hash> transactionHashes = getTransactionHashesMocked(amountOfDaysBack, address, startDate);
//
//        request.setAddress(address);
//        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
//        when(transactions.getByHash((transactionHashes.stream().collect(Collectors.toList())).get(0))).thenReturn(transactionData);
//        transactionService.getTransactionsByAddress(request, response);
//        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
//
//        Assert.assertNotEquals(EMPTY_OUTPUT, response.getContentAsString());
//    }
//
//    @Test
//    public void getTransactionsByDate_noDate_emptyResponse() throws UnsupportedEncodingException {
//        GetTransactionsByDateRequest request = new GetTransactionsByDateRequest();
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        transactionService.getTransactionsByDate(request, response);
//
//        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Assert.assertEquals(EMPTY_OUTPUT, response.getContentAsString());
//    }
//
//    @Test
//    public void getTransactionsByDate_noTransactionsInDate_emptyResponse() throws UnsupportedEncodingException {
//        GetTransactionsByDateRequest request = new GetTransactionsByDateRequest();
//        Instant today = Instant.now();
//        request.setDate(transactionService.calculateInstantLocalDate(today));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        when(addressTransactionsByDates.getByHash(transactionService.calculateHashByTime(today))).thenReturn(null);
//
//        transactionService.getTransactionsByDate(request, response);
//        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Assert.assertEquals(EMPTY_OUTPUT, response.getContentAsString());
//
//        response = new MockHttpServletResponse();
//        AddressTransactionsByDate addressTransactionsByDate = new AddressTransactionsByDate(today, null);
//        when(addressTransactionsByDates.getByHash(transactionService.calculateHashByTime(today))).thenReturn(addressTransactionsByDate);
//
//        transactionService.getTransactionsByDate(request, response);
//        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Assert.assertEquals(EMPTY_OUTPUT, response.getContentAsString());
//
//        response = new MockHttpServletResponse();
//        HashSet<Hash> transactionHashes = new HashSet<>();
//        addressTransactionsByDate = new AddressTransactionsByDate(today, transactionHashes);
//        when(addressTransactionsByDates.getByHash(transactionService.calculateHashByTime(today))).thenReturn(addressTransactionsByDate);
//
//        transactionService.getTransactionsByDate(request, response);
//        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Assert.assertEquals(EMPTY_OUTPUT, response.getContentAsString());
//    }
//
//
//    @Test
//    public void getTransactionsByDate_transactionsInDate_notEmptyResponse() throws UnsupportedEncodingException {
//        GetTransactionsByDateRequest request = new GetTransactionsByDateRequest();
//        Instant today = Instant.now();
//        request.setDate(transactionService.calculateInstantLocalDate(today));
//        MockHttpServletResponse response = new MockHttpServletResponse();
//
//        when(addressTransactionsByDates.getByHash(transactionService.calculateHashByTime(today))).thenReturn(null);
//
//        HashSet<Hash> transactionHashes = new HashSet<>();
//        Hash transactionHashToRetrieveLocally = generateRandomHash();
//        transactionHashes.add(transactionHashToRetrieveLocally);
//
//        AddressTransactionsByDate addressTransactionsByDate = new AddressTransactionsByDate(today, transactionHashes);
//        when(addressTransactionsByDates.getByHash(transactionService.calculateHashByTime(today))).thenReturn(addressTransactionsByDate);
//        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
//        when(transactions.getByHash(any(Hash.class))).thenReturn(transactionData);
//
//        transactionService.getTransactionsByDate(request, response);
//        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
//        Assert.assertNotEquals(EMPTY_OUTPUT, response.getContentAsString());
//    }
//
//}