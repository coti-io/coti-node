package io.coti.basenode.services;

import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetTransactionResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.coti.basenode.utils.TransactionTestUtils.createTransactionIndexData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeEventService.class, IEventService.class,
        BaseNodeTransactionHelper.class

})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith({SpringExtension.class})
@Slf4j
public class BaseNodeEventServiceTest {
    @Autowired
    private IEventService baseNodeEventService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @MockBean
    private ITransactionPropagationCheckService transactionPropagationCheckService;
    @MockBean
    private Transactions transactions;

    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private IClusterService clusterService;

    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @MockBean
    private BaseNodeCurrencyService currencyService;
    @MockBean
    private IMintingService mintingService;

    @MockBean
    private INetworkService networkService;

    @Test
    public void handleNewHardForkEvent_TrustScoreConsensusBeforeTCC_validation() {
        TransactionData transactionData = TransactionTestUtils.createHardForkTrustScoreConsensusTransaction();

        ResponseEntity<IResponse> eventTransactionDataResponse = baseNodeEventService.getEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, eventTransactionDataResponse.getStatusCode());
        Assertions.assertEquals("Event Not Found", ((Response) eventTransactionDataResponse.getBody()).getMessage());

        ResponseEntity<IResponse> confirmedEventTransactionDataResponse = baseNodeEventService.getConfirmedEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, confirmedEventTransactionDataResponse.getStatusCode());
        Assertions.assertEquals("Event Not Found", ((Response) confirmedEventTransactionDataResponse.getBody()).getMessage());

        boolean addedEvent = baseNodeEventService.checkEventAndUpdateEventsTable(transactionData);
        Assertions.assertTrue(addedEvent);
        when(transactions.getByHash(any(Hash.class))).thenReturn(transactionData);
        TransactionIndexData transactionIndexData = createTransactionIndexData(transactionData.getHash(), 7);
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        addedEvent = baseNodeEventService.checkEventAndUpdateEventsTable(transactionData);
        Assertions.assertFalse(addedEvent);

        eventTransactionDataResponse = baseNodeEventService.getEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
        Assertions.assertEquals(HttpStatus.OK, eventTransactionDataResponse.getStatusCode());
        Assertions.assertEquals(transactionData.getHash().toString(), ((GetTransactionResponse) eventTransactionDataResponse.getBody()).getTransactionData().getHash());

        confirmedEventTransactionDataResponse = baseNodeEventService.getConfirmedEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, confirmedEventTransactionDataResponse.getStatusCode());
        Assertions.assertEquals("Event Not Found", ((Response) confirmedEventTransactionDataResponse.getBody()).getMessage());

        transactionData.setTrustChainConsensus(true);
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        confirmedEventTransactionDataResponse = baseNodeEventService.getConfirmedEventTransactionDataResponse(Event.TRUST_SCORE_CONSENSUS);
        Assertions.assertEquals(HttpStatus.OK, confirmedEventTransactionDataResponse.getStatusCode());
        Assertions.assertEquals(transactionData.getHash().toString(), ((GetTransactionResponse) confirmedEventTransactionDataResponse.getBody()).getTransactionData().getHash());
    }

}
