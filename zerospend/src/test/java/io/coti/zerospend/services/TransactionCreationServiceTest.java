package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.ZeroSpendTransactionRequest;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.zerospend.crypto.TransactionCryptoCreator;
import io.coti.zerospend.data.ZeroSpendTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.TestUtils.createRandomTransaction;
import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TransactionCreationService.class)
@Slf4j
public class TransactionCreationServiceTest {

    @Autowired
    private TransactionCreationService transactionCreationService;

    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private TransactionHelper transactionHelper;
    @MockBean
    private IValidationService validationService;
    @MockBean
    private IPropagationPublisher propagationPublisher;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private TransactionCryptoCreator transactionCryptoCreator;
    @MockBean
    private DspVoteService dspVoteService;

    @Before
    public void setUp() {
        when(validationService.fullValidation(any(TransactionData.class))).thenReturn(true);
        when(transactionCryptoCreator.getAddress()).thenReturn(generateRandomHash());
    }

    @Test
    public void createNewStarvationZeroSpendTransaction() {
        TransactionData txData = createRandomTransaction();
        String result = transactionCreationService.createNewStarvationZeroSpendTransaction(txData);
        Assert.assertEquals("Ok", result);

        when(validationService.fullValidation(any(TransactionData.class))).thenReturn(false);
        result = transactionCreationService.createNewStarvationZeroSpendTransaction(txData);
        Assert.assertEquals("Invalid", result);
    }

    @Test
    public void createNewGenesisZeroSpendTransaction() {
        ZeroSpendTransactionRequest zeroSpendTransactionRequest = TestUtils.generateZeroSpendTxRequest();

        String result = transactionCreationService.createNewGenesisZeroSpendTransaction(zeroSpendTransactionRequest);
        Assert.assertEquals("Ok", result);

        when(validationService.fullValidation(any(TransactionData.class))).thenReturn(false);
        result = transactionCreationService.createNewGenesisZeroSpendTransaction(zeroSpendTransactionRequest);
        Assert.assertEquals("Invalid", result);
    }

    @Test
    public void  createNewZeroSpendTransaction() {
        TransactionData txData = TestUtils.generateRandomTxData();
        String result = transactionCreationService.createNewZeroSpendTransaction(txData, ZeroSpendTransactionType.STARVATION);
        // TODO: final result is not affected by what happens in createZeroSpendTransaction
        Assert.assertEquals("Ok", result);

        result = transactionCreationService.createNewZeroSpendTransaction(txData, ZeroSpendTransactionType.GENESIS);
        // TODO: final result is not affected by what happens in createZeroSpendTransaction
        Assert.assertEquals("Ok", result);

        when(validationService.fullValidation(any(TransactionData.class))).thenReturn(false);
        result = transactionCreationService.createNewZeroSpendTransaction(txData, ZeroSpendTransactionType.STARVATION);
        Assert.assertEquals("Invalid", result);
    }


    @Test
    public void createGenesisTransactions() {
        try {
            transactionCreationService.createGenesisTransactions();
            Mockito.verify(dspVoteService, Mockito.times(11)).setIndexForDspResult(any(TransactionData.class), any(DspConsensusResult.class));
            Mockito.verify(transactionHelper, Mockito.times(11)).attachTransactionToCluster(any(TransactionData.class));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}