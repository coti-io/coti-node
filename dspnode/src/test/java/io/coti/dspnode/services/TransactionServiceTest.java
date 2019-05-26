package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {TransactionService.class,
        BaseNodeTransactionService.class,
        ITransactionHelper.class})
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private IPropagationPublisher propagationPublisher;
    @MockBean
    private IValidationService validationService;
    @MockBean
    private ISender sender;
    @MockBean
    private DspVoteCrypto dspVoteCrypto;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private Transactions Transactions;


    @Test
    public void handleNewTransactionFromFullNode() {
        TransactionData txData = TestUtils.generateRandomTxData();
        transactionService.init();

        // When Transaction already exists
        when(transactionHelper.isTransactionAlreadyPropagated(any(TransactionData.class))).thenReturn(true);
        transactionService.handleNewTransactionFromFullNode(txData);
        Mockito.verify(transactionHelper, Mockito.times(1)).isTransactionAlreadyPropagated(any(TransactionData.class));
        Mockito.verify(transactionHelper, Mockito.times(0)).attachTransactionToCluster(any(TransactionData.class));

        // When Invalid Transaction Received
        when(transactionHelper.isTransactionAlreadyPropagated(any(TransactionData.class))).thenReturn(false);
        transactionService.handleNewTransactionFromFullNode(txData);
        Mockito.verify(transactionHelper, Mockito.times(2)).isTransactionAlreadyPropagated(any(TransactionData.class));
        Mockito.verify(transactionHelper, Mockito.times(0)).attachTransactionToCluster(any(TransactionData.class));

        // When it reaches Finished state
        when(validationService.validatePropagatedTransactionDataIntegrity(any(TransactionData.class))).thenReturn(true);
        when(validationService.validateBalancesAndAddToPreBalance(any(TransactionData.class))).thenReturn(true);
        transactionService.handleNewTransactionFromFullNode(txData);
        Mockito.verify(transactionHelper, Mockito.times(1)).setTransactionStateToFinished(any(TransactionData.class));
        Mockito.verify(transactionHelper, Mockito.times(1)).setTransactionStateToFinished(txData);

    }
}
