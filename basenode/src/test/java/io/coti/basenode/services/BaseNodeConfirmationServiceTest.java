package io.coti.basenode.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.BaseNodeTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.BaseNodeTestUtils.*;


@ContextConfiguration(classes = {
        BaseNodeConfirmationService.class}
)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeConfirmationServiceTest {

    @Autowired
    private BaseNodeConfirmationService baseNodeConfirmationService;

    @MockBean
    private Transactions transactions;
    @MockBean
    private LiveViewService liveViewService;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private TransactionIndexService transactionIndexService;

    @Before
    public void setUp() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        Hash hash = generateRandomHash();
        when(transactions.getByHash(any(Hash.class))).thenReturn(BaseNodeTestUtils.createRandomTransaction(hash));
        baseNodeConfirmationService.init();
    }

    @After
    public void shutdown() {

        baseNodeConfirmationService.shutdown();
    }

    @Test
    public void insertNewTransactionIndex() {
        TransactionData txData = generateRandomTxData();
        // Requires consensus result
        Assert.assertFalse(baseNodeConfirmationService.insertNewTransactionIndex(txData));

        // Updating Index from Dsp Consensus unsuccessfully
        DspConsensusResult dspConsensusResult = BaseNodeTestUtils.generateRandomDspConsensusResult();
        txData.setDspConsensusResult(dspConsensusResult);
        Assert.assertFalse(baseNodeConfirmationService.insertNewTransactionIndex(txData));
        Mockito.verify(transactionIndexService, Mockito.times(1)).insertNewTransactionIndex(any(TransactionData.class));

        // Updating Tx Inde successfully
        when(transactionIndexService.insertNewTransactionIndex(txData)).thenReturn(true);
        Assert.assertTrue(baseNodeConfirmationService.insertNewTransactionIndex(txData));
    }


    @Test
    public void insertSavedTransaction() {
        TransactionData txData = createRandomTransaction();
        DspConsensusResult dspConsensusResult = generateRandomDspConsensusResult();
        txData.setDspConsensusResult(dspConsensusResult);
        // TODO: consider using less Mocks here
        when(transactionIndexService.insertNewTransactionIndex(txData)).thenReturn(true);
        when(transactionHelper.isConfirmed(txData)).thenReturn(true);
        when(transactionHelper.isDspConfirmed(txData)).thenReturn(true);
        when(transactionHelper.isDspConfirmed(txData)).thenReturn(true);

        txData.setTrustChainConsensus(true);

        long initTotalConfirmed = baseNodeConfirmationService.getTotalConfirmed();
        long initDspConfirmed = baseNodeConfirmationService.getDspConfirmed();

        baseNodeConfirmationService.insertSavedTransaction(txData);

        long updatedTotalConfirmed = baseNodeConfirmationService.getTotalConfirmed();
        long updatedDspConfirmed = baseNodeConfirmationService.getDspConfirmed();

        Assert.assertEquals(initTotalConfirmed+1, updatedTotalConfirmed);
        Assert.assertEquals(initDspConfirmed+1, updatedDspConfirmed);

    }




}
