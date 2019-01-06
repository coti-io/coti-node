package io.coti.basenode.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.TestUtils.*;


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
        when(transactions.getByHash(any(Hash.class))).thenReturn(TestUtils.createRandomTransaction(hash));
        baseNodeConfirmationService.init();
    }

    @Test
    public void setTccToTrue_noExceptionIsThrown() {
        try {
            Hash hash = generateRandomHash();
            transactions.put(TestUtils.createRandomTransaction(hash));
            baseNodeConfirmationService.setTccToTrue(new TccInfo(hash, null, generateRandomTrustScore()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void setDspcToTrue_noExceptionIsThrown() {
        try {
            Hash hash = generateRandomHash();
            when(transactions.getByHash(any(Hash.class))).thenReturn(TestUtils.createRandomTransaction(hash));
            transactions.put(TestUtils.createRandomTransaction(hash));
            baseNodeConfirmationService.setDspcToTrue(new DspConsensusResult(hash));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void getConfirmedNumber() {
        insertSavedTransaction();
        long totalConfirmed = baseNodeConfirmationService.getTotalConfirmed();
        long dspConfirmed = baseNodeConfirmationService.getDspConfirmed();
        Assert.assertTrue(totalConfirmed != 0 && dspConfirmed != 0);
    }


    private void insertSavedTransaction() {
        try {
            TransactionData transactionData = createRandomTransaction();
            transactionData.setDspConsensusResult(new DspConsensusResult(generateRandomHash()));
            when(transactionIndexService.insertNewTransactionIndex(transactionData)).thenReturn(true);
            when(transactionHelper.isConfirmed(transactionData)).thenReturn(true);
            when(transactionHelper.isDspConfirmed(transactionData)).thenReturn(true);
            when(transactionHelper.isDspConfirmed(transactionData)).thenReturn(true);
            baseNodeConfirmationService.insertSavedTransaction(transactionData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void shutdown() {
        baseNodeConfirmationService.shutdown();
    }
}
