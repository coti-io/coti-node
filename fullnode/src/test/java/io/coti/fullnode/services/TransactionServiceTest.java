package io.coti.fullnode.services;

import io.coti.basenode.communication.ZeroMQSubscriber;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.config.WebShutDown;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.TestUtils.*;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TransactionServiceTest {

    @Autowired
    TransactionService transactionService;

    @MockBean
    private BaseNodeInitializationService baseNodeInitializationService;
    @MockBean
    private BalanceService balanceService;
    @MockBean
    private TransactionHelper transactionHelper;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IValidationService validationService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private ISender sender;
    @MockBean
    private AddressTransactionsHistories addressTransactionHistories;
    @MockBean
    private Transactions transactions;
    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private PotService potService;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private ConfirmationService confirmationService;
    @MockBean
    private MonitorService monitorService;
    @MockBean
    private LiveViewService liveViewService;
    @MockBean
    private AddressService addressService;
    @MockBean
    private IDspVoteService dspVoteService;
    @MockBean
    private ZeroMQSubscriber zeroMQSubscriber;
    @MockBean
    private WebShutDown webShutDown;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void addNewTransaction() {
        when(validationService.validateTransactionDataIntegrity(any(TransactionData.class))).thenReturn(true);
        when(validationService.validateBaseTransactionAmounts(any(TransactionData.class))).thenReturn(true);
        when(validationService.validateTransactionTrustScore(any(TransactionData.class))).thenReturn(true);
        ResponseEntity<Response> response = transactionService.addNewTransaction(generateAddTransactionRequest());

        Assert.assertTrue(response.getBody().getMessage().equals("Balance for address is insufficient!"));
    }

    @Test
    public void getAddressTransactions() {
        Hash addressHash = generateRandomHash();
        when(addressTransactionHistories.getByHash(addressHash)).thenReturn(new AddressTransactionsHistory(addressHash));

        ResponseEntity<BaseResponse> response = transactionService.getAddressTransactions(addressHash);

        Assert.assertTrue(response.getBody().getStatus().equals("Success"));
    }

    @Test
    public void getTransactionDetails() {
        Hash transactionHash = generateRandomHash();
        when(transactions.getByHash(transactionHash)).thenReturn(createTransactionWithSpecificHash(transactionHash));

        ResponseEntity<BaseResponse> response = transactionService.getTransactionDetails(transactionHash);

        Assert.assertTrue(response.getBody().getStatus().equals("Success"));
    }

    @Test
    public void continueHandlePropagatedTransaction_noExceptionIsThrown() {
        try {
            transactionService.continueHandlePropagatedTransaction(generateRandomTransaction());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
