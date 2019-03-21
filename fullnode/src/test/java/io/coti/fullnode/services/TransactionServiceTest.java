package io.coti.fullnode.services;

import io.coti.basenode.communication.ZeroMQSubscriber;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.config.WebShutDown;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.controllers.AddressController;
import io.coti.fullnode.http.GetAddressTransactionHistoryResponse;
import io.coti.fullnode.http.GetTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static testUtils.TestUtils.generateAddTransactionRequest;


@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {TransactionService.class, Transactions.class, BaseNodeTransactionService.class})
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    TransactionService transactionService;

    @MockBean
    private IValidationService validationService;
    @MockBean
    private TransactionHelper transactionHelper;
    @MockBean
    private Transactions transactions;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private AddressTransactionsHistories addressTransactionHistories;
    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private INetworkService mockINetworkService;
    @MockBean
    private PotService potService;





//    @MockBean
//    private BaseNodeInitializationService baseNodeInitializationService;
//    @MockBean
//    private BalanceService balanceService;
//    @MockBean
//    private ISender sender;

//    @MockBean
//    private TransactionIndexService transactionIndexService;
//    @MockBean
//    private ConfirmationService confirmationService;
//    @MockBean
//    private MonitorService monitorService;
//    @MockBean
//    private LiveViewService liveViewService;
//    @MockBean
//    private AddressService addressService;
//    @MockBean
//    private IDspVoteService dspVoteService;
//    @MockBean
//    private ZeroMQSubscriber zeroMQSubscriber;
//    @MockBean
//    private WebShutDown webShutDown;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void init() {
    }

    @Test
    public void handlePropagatedTransaction() {
    }

    @Test
    public void continueHandlePropagatedTransaction() {
    }

    @Test
    public void totalPostponedTransactions() {
    }

    @Test
    public void addNewTransaction_Succeed() {
        ResponseEntity<Response> response = null;
        // Fail on Data Integrity check
        response = transactionService.addNewTransaction(generateAddTransactionRequest());
        Assert.assertEquals(AUTHENTICATION_FAILED_MESSAGE, response.getBody().getMessage());

        // Fail on Base Tx sum check
        when(validationService.validateTransactionDataIntegrity(any(TransactionData.class))).thenReturn(true);
        response = transactionService.addNewTransaction(generateAddTransactionRequest());
        Assert.assertEquals(ILLEGAL_TRANSACTION_MESSAGE, response.getBody().getMessage());

        // Fail on Tx Trust-score check
        when(validationService.validateBaseTransactionAmounts(any(TransactionData.class))).thenReturn(true);
        response = transactionService.addNewTransaction(generateAddTransactionRequest());
        Assert.assertEquals(INVALID_TRUST_SCORE_MESSAGE, response.getBody().getMessage());

        // Fail on Balance check
        when(validationService.validateTransactionTrustScore(any(TransactionData.class))).thenReturn(true);
        response = transactionService.addNewTransaction(generateAddTransactionRequest());
        Assert.assertEquals(INSUFFICIENT_FUNDS_MESSAGE, response.getBody().getMessage());

        // TODO: currently does not invalidate the flow, requires changing
        // Fail on Sources check
        when(validationService.validateBalancesAndAddToPreBalance(any(TransactionData.class))).thenReturn(true);
//        response = transactionService.addNewTransaction(generateAddTransactionRequest());
//        verify(clusterService, atLeast(1)).selectSources(any());


    }

    @Test
    public void selectSources() {
    }

    @Test
    public void getAddressTransactions() {
        // when addressTransactionHistories returned are null
        Hash addressHash = TestUtils.generateRandomHash();
        ResponseEntity<IResponse> addressTransactionsResponse = transactionService.getAddressTransactions(addressHash);
        Assert.assertTrue(addressTransactionsResponse.getStatusCode().equals(HttpStatus.OK));
        Assert.assertTrue(((GetAddressTransactionHistoryResponse)addressTransactionsResponse.getBody()).getTransactionsData().isEmpty());

        // when addressTransactionHistories returned are not null, but with no TxData
        AddressTransactionsHistory addressTxHistory = new AddressTransactionsHistory(addressHash);
        List<Hash> txsHistoryList = new ArrayList<>();
        txsHistoryList.add(TestUtils.generateRandomHash());
        addressTxHistory.setTransactionsHistory(txsHistoryList);

        when(addressTransactionHistories.getByHash(any(Hash.class))).thenReturn(addressTxHistory);
        addressTransactionsResponse = transactionService.getAddressTransactions(addressHash);
        Assert.assertTrue(addressTransactionsResponse.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
        Assert.assertTrue( ((Response)addressTransactionsResponse.getBody()).getStatus().equals(STATUS_ERROR) );

        // when addressTransactionHistories returned are not null, nor is TxData
        Hash txDataHash = TestUtils.generateRandomHash();
        TransactionData txData = TestUtils.createRandomTransaction(txDataHash);
        when(transactions.getByHash(any(Hash.class))).thenReturn(txData);
        addressTransactionsResponse = transactionService.getAddressTransactions(addressHash);
        Assert.assertTrue(addressTransactionsResponse.getStatusCode().equals(HttpStatus.OK));

        Assert.assertTrue(((GetAddressTransactionHistoryResponse)addressTransactionsResponse.getBody()).getTransactionsData().size()==1);
        Assert.assertTrue(((GetAddressTransactionHistoryResponse)addressTransactionsResponse.getBody()).getTransactionsData().get(0).getHash().equals(txDataHash.toString()));
    }

    @Test
    public void getTransactionDetails() {
        // When get Tx Data by hash is null
        Hash txHash = TestUtils.generateRandomHash();
        ResponseEntity<IResponse> txDetailsResponse = transactionService.getTransactionDetails(txHash);
        Assert.assertTrue(txDetailsResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        Assert.assertTrue(((Response)txDetailsResponse.getBody()).getStatus().equals(STATUS_ERROR));
        Assert.assertTrue(((Response)txDetailsResponse.getBody()).getMessage().equals(TRANSACTION_DOESNT_EXIST_MESSAGE));

        // When get Tx Data by hash is found
        Hash txDataHash = TestUtils.generateRandomHash();
        TransactionData txData = TestUtils.createRandomTransaction(txDataHash);
        when(transactions.getByHash(any(Hash.class))).thenReturn(txData);
        txDetailsResponse = transactionService.getTransactionDetails(txHash);
        Assert.assertTrue(txDetailsResponse.getStatusCode().equals(HttpStatus.OK));

        Assert.assertTrue(((GetTransactionResponse)txDetailsResponse.getBody()).getTransactionData().getHash().equals(txDataHash.toHexString()));
        Assert.assertTrue(((GetTransactionResponse)txDetailsResponse.getBody()).getStatus().equals(BaseNodeHttpStringConstants.STATUS_SUCCESS));


    }

    @Test
    public void continueHandlePropagatedTransaction1() {
    }
}