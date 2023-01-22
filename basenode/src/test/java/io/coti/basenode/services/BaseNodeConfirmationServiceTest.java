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
import java.util.function.Consumer;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.basenode.utils.TransactionTestUtils.createTransactionIndexData;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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

        confirmationService.continueHandleDSPConfirmedTransaction(transactionData);

        transactionData.setTrustChainTrustScore(120);
        transactionData.setTrustChainConsensus(true);

        confirmationService.continueHandleDSPConfirmedTransaction(transactionData);
    }

    @Test
    public void handle_tcc_info_update() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        TccInfo tccInfo = new TccInfo(transactionData.getHash(), 115, Instant.now());
        doAnswer(invocation -> {
            Consumer<TransactionData> arg0 = invocation.getArgument(1, Consumer.class);
            arg0.accept(transactionData);
            confirmationService.getInitialConfirmationFinished().set(true);
            return null;
        }).when(transactions).lockAndGetByHash(any(Hash.class), any(Consumer.class));
        confirmationService.setTccToTrue(tccInfo);
        await().atMost(5, SECONDS).until(() -> confirmationService.getInitialConfirmationFinished().get());
        Assertions.assertTrue(confirmationService.getInitialConfirmationFinished().get());
        Assertions.assertEquals(115.0, transactionData.getTrustChainTrustScore(), DELTA);
    }
}
