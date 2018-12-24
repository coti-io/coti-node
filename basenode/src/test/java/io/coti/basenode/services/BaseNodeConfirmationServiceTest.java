package io.coti.basenode.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeConfirmationService;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.TestUtils.*;


//@TestPropertySource(locations = "classpath:test.properties")
//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {
//        // Addresses.class,
//        RocksDBConnector.class,
//        BaseNodeConfirmationService.class,
//        LiveViewService.class,
//        BaseNodeBalanceService.class,
//        TransactionHelper.class,
//        TransactionIndexService.class,
//        Transactions.class,
//        AddressTransactionsHistories.class}
//)
//@Slf4j
@ContextConfiguration(classes = {
        // Addresses.class,
        //RocksDBConnector.class,
        BaseNodeConfirmationService.class}
        //LiveViewService.class,
        //BaseNodeBalanceService.class,
       // TransactionHelper.class,
       // TransactionIndexService.class,
       // Transactions.class,
       // AddressTransactionsHistories.class}
)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeConfirmationServiceTest {
    private static final int SIZE_OF_HASH = 64;

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
        Hash hash = generateRandomHash(SIZE_OF_HASH);
        when(transactions.getByHash(any(Hash.class))).thenReturn(createTransactionWithSpecificHash(hash));
        baseNodeConfirmationService.init();
    }

    @Test
    public void testSetTccToTrue_noExceptionIsThrown() {
        try {
            Hash hash = generateRandomHash(SIZE_OF_HASH);
            transactions.put(createTransactionWithSpecificHash(hash));
            baseNodeConfirmationService.setTccToTrue(new TccInfo(hash, null, generateRandomTrustScore()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSetDspcToTrue_noExceptionIsThrown() {
        try {
            Hash hash = generateRandomHash(SIZE_OF_HASH);
            when(transactions.getByHash(any(Hash.class))).thenReturn(createTransactionWithSpecificHash(hash));
            transactions.put(createTransactionWithSpecificHash(hash));
            baseNodeConfirmationService.setDspcToTrue(new DspConsensusResult(hash));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void getConfirmedNumber() {
        insertSavedTransaction();
        long totalConfirmed = baseNodeConfirmationService.getTotalConfirmed();
        long tccConfirmed = baseNodeConfirmationService.getTccConfirmed();
        long dspConfirmed = baseNodeConfirmationService.getDspConfirmed();
        Assert.assertTrue(totalConfirmed != 0 && dspConfirmed != 0);
    }


    private void insertSavedTransaction() {
        try {
            TransactionData transactionData = generateRandomTransaction();
            transactionData.setDspConsensusResult(new DspConsensusResult(generateRandomHash(SIZE_OF_HASH)));
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
