package io.coti.basenode.services;

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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static io.coti.basenode.utils.TransactionTestUtils.createTransactionIndexData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeConfirmationService.class,
        BaseNodeBalanceService.class, BaseNodeTransactionHelper.class, BaseNodeEventService.class,
        BaseNodeCurrencyService.class,
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
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

    @Test
    public void continueHandleDSPConfirmedTransaction() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        baseNodeConfirmationService.continueHandleDSPConfirmedTransaction(transactionData);

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
        Assert.assertEquals(HttpStatus.OK, confirmedEventTransactionDataResponse.getStatusCode());
        Assert.assertEquals(eventTransactionData.getHash().toString(), ((GetTransactionResponse) confirmedEventTransactionDataResponse.getBody()).getTransactionData().getHash());

        baseNodeConfirmationService.continueHandleDSPConfirmedTransaction(transactionData);

        transactionData.setTrustChainTrustScore(120);
        transactionData.setTrustChainConsensus(true);

        baseNodeConfirmationService.continueHandleDSPConfirmedTransaction(transactionData);
    }

}
