package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.LockData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionState;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.coti.basenode.data.TransactionState.RECEIVED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeTransactionService.class, BaseNodeTransactionHelper.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeTransactionServiceTest {

    @Autowired
    private BaseNodeTransactionService baseNodeTransactionService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @MockBean
    private IValidationService validationService;
    @MockBean
    private IDspVoteService dspVoteService;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private IClusterHelper clusterHelper;
    @MockBean
    private Transactions transactions;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private JacksonSerializer jacksonSerializer;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private IMintingService mintingService;
    @MockBean
    protected IChunkService chunkService;
    @MockBean
    protected IEventService eventService;
    @MockBean
    protected ITransactionPropagationCheckService transactionPropagationCheckService;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private BaseNodeCurrencyService currencyService;

    @Test
    void check_transaction_already_propagated_and_start_handle() {
        AtomicBoolean isTransactionAlreadyPropagated = new AtomicBoolean(false);
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        baseNodeTransactionService.checkTransactionAlreadyPropagatedAndStartHandle(transactionData, isTransactionAlreadyPropagated);
        Assertions.assertTrue(transactionHelper.isTransactionHashProcessing(transactionData.getHash()));
    }

    @Test
    void has_one_of_parents_missing() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Assertions.assertFalse(baseNodeTransactionService.hasOneOfParentsMissing(transactionData));
    }
}