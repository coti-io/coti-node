package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetTransactionResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

import static io.coti.basenode.utils.TransactionTestUtils.createTransactionIndexData;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {BaseNodeConfirmationService.class,
        BaseNodeBalanceService.class, BaseNodeTransactionHelper.class, BaseNodeEventService.class,
        BaseNodeCurrencyService.class,
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
public class BaseNodeConfirmationServiceTest {

    @Autowired
    private BaseNodeConfirmationService baseNodeConfirmationService;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private IMintingService mintingService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @MockBean
    private ITransactionPropagationCheckService transactionPropagationCheckService;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private Transactions transactions;
    @Autowired
    BaseNodeEventService baseNodeEventService;

    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @MockBean
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private ISender sender;
    private static final double DELTA = 1e-15;

    @BeforeEach
    public void init() {
        baseNodeConfirmationService.init();
    }

    @Test
    public void continue_handle_dsp_confirmed_transaction() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        baseNodeConfirmationService.continueHandleDSPConfirmedTransaction(transactionData);

        TransactionData eventTransactionData = TransactionTestUtils.createHardForkMultiDagTransaction();
        when(transactions.getByHash(any(Hash.class))).thenReturn(eventTransactionData);
        TransactionIndexData transactionIndexData = createTransactionIndexData(eventTransactionData.getHash(), 7);
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        baseNodeEventService.checkEventAndUpdateEventsTable(eventTransactionData);

        eventTransactionData.setTrustChainConsensus(true);
        DspConsensusResult dspConsensusResult = new DspConsensusResult(eventTransactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        eventTransactionData.setDspConsensusResult(dspConsensusResult);

        ResponseEntity<IResponse> confirmedEventTransactionDataResponse = baseNodeEventService.getConfirmedEventTransactionDataResponse(Event.MULTI_DAG);
        Assertions.assertEquals(HttpStatus.OK, confirmedEventTransactionDataResponse.getStatusCode());
        Assertions.assertEquals(eventTransactionData.getHash().toString(), ((GetTransactionResponse) confirmedEventTransactionDataResponse.getBody()).getTransactionData().getHash());

        baseNodeConfirmationService.continueHandleDSPConfirmedTransaction(transactionData);

        transactionData.setTrustChainTrustScore(120);
        transactionData.setTrustChainConsensus(true);

        baseNodeConfirmationService.continueHandleDSPConfirmedTransaction(transactionData);
    }

    @Test
    public void handle_tcc_info_update() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        TccInfo tccInfo = new TccInfo(transactionData.getHash(), 115, Instant.now());
        doAnswer(invocation -> {
            Consumer<TransactionData> arg0 = invocation.getArgument(1, Consumer.class);
            arg0.accept(transactionData);
            baseNodeConfirmationService.getInitialConfirmationFinished().set(true);
            return null;
        }).when(transactions).lockAndGetByHash(any(Hash.class), any(Consumer.class));
        baseNodeConfirmationService.setTccToTrue(tccInfo);
        await().atMost(5, SECONDS).until(() -> baseNodeConfirmationService.getInitialConfirmationFinished().get());
        Assertions.assertTrue(baseNodeConfirmationService.getInitialConfirmationFinished().get());
        Assertions.assertEquals(115.0, transactionData.getTrustChainTrustScore(), DELTA);
    }
}
