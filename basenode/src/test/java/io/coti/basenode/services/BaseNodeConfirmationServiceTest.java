package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSender;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetTransactionResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
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

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.basenode.utils.TransactionTestUtils.createTransactionIndexData;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {BaseNodeConfirmationService.class, BaseNodeEventService.class,
        BaseNodeTransactionHelper.class
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeConfirmationServiceTest {

    @Autowired
    private BaseNodeConfirmationService confirmationService;
    @Autowired
    BaseNodeTransactionHelper transactionHelper;
    @Autowired
    BaseNodeEventService baseNodeEventService;
    @MockBean
    Transactions transactionsLocal;
    @MockBean
    TransactionIndexes transactionIndexesLocal;
    @MockBean
    BaseNodeNetworkService networkServiceLocal;
    @MockBean
    TransactionIndexService transactionIndexServiceLocal;
    @MockBean
    ZeroMQSender senderLocal;
    @MockBean
    ClusterService clusterServiceLocal;


    private static final double DELTA = 1e-15;

    @BeforeEach
    public void init() {
        BaseNodeServiceManager.nodeTransactionHelper = this.transactionHelper;
        nodeEventService = baseNodeEventService;
        transactions = transactionsLocal;
        transactionIndexes = transactionIndexesLocal;
        networkService = networkServiceLocal;
        transactionIndexService = transactionIndexServiceLocal;
        zeroMQSender = senderLocal;
        clusterService = clusterServiceLocal;
        confirmationService.init();
    }

    @Test
    void continue_handle_dsp_confirmed_transaction() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        confirmationService.continueHandleDSPConfirmedTransaction(transactionData);

        TransactionData eventTransactionData = TransactionTestUtils.createHardForkTrustScoreConsensusTransaction();
        when(transactions.getByHash(any(Hash.class))).thenReturn(eventTransactionData);
        TransactionIndexData transactionIndexData = createTransactionIndexData(eventTransactionData.getHash(), 7);
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        baseNodeEventService.checkEventAndUpdateEventsTable(eventTransactionData);

        eventTransactionData.setTrustChainConsensus(true);
        DspConsensusResult dspConsensusResult = new DspConsensusResult(eventTransactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        eventTransactionData.setDspConsensusResult(dspConsensusResult);

        ResponseEntity<IResponse> confirmedEventTransactionDataResponse = baseNodeEventService.getConfirmedEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
        Assertions.assertEquals(HttpStatus.OK, confirmedEventTransactionDataResponse.getStatusCode());
        Assertions.assertEquals(eventTransactionData.getHash().toString(), ((GetTransactionResponse) confirmedEventTransactionDataResponse.getBody()).getTransactionData().getHash());

        confirmationService.continueHandleDSPConfirmedTransaction(transactionData);

        transactionData.setTrustChainTrustScore(120);
        transactionData.setTrustChainConsensus(true);

        confirmationService.continueHandleDSPConfirmedTransaction(transactionData);
    }

    @Test
    void handle_tcc_info_update() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        TccInfo tccInfo = new TccInfo(transactionData.getHash(), 115, Instant.now());
        doAnswer(invocation -> {
            Consumer<TransactionData> arg0 = invocation.getArgument(1, Consumer.class);
            arg0.accept(transactionData);
            confirmationService.getInitialTccConfirmationFinished().set(true);
            return null;
        }).when(transactions).lockAndGetByHash(any(Hash.class), any(Consumer.class));
        confirmationService.setTccToTrue(tccInfo);
        await().atMost(5, SECONDS).until(() -> confirmationService.getInitialTccConfirmationFinished().get());
        Assertions.assertTrue(confirmationService.getInitialTccConfirmationFinished().get());
        Assertions.assertEquals(115.0, transactionData.getTrustChainTrustScore(), DELTA);
    }

    @Test
    void handleMissingIndexes() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndex(2);
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), 0L, "0".getBytes());
        when(transactionIndexService.getLastTransactionIndexData()).thenReturn(transactionIndexData);
        when(transactionIndexService.insertNewTransactionIndex(any(TransactionData.class))).thenReturn(Optional.of(Boolean.FALSE));
        NetworkNodeData networkNodeData = mock(NetworkNodeData.class);
        when(networkService.getNetworkNodeData()).thenReturn(networkNodeData);
        when(networkNodeData.getNodeHash()).thenReturn(TransactionTestUtils.generateRandomHash());
        when(networkNodeData.getNodeType()).thenReturn(NodeType.FullNode);
        NetworkNodeData recoveryServer = mock(NetworkNodeData.class);
        when(networkService.getRecoveryServer()).thenReturn(recoveryServer);
        when(recoveryServer.getReceivingFullAddress()).thenReturn("tcp://localhost:7030");
        Assertions.assertFalse(confirmationService.insertNewTransactionIndex(transactionData));
        await().atMost(8500, MILLISECONDS).until(() -> confirmationService.getResendDcrCounter() > 0);
        verify(zeroMQSender, atLeastOnce()).send(any(NodeResendDcrData.class), any(String.class));
    }
}
