package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.ZeroSpendTransactionRequest;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.zerospend.crypto.TransactionCryptoCreator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.TestUtils.generateRandomHash;
import static testUtils.TestUtils.generateRandomTransaction;

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
        String result = transactionCreationService.createNewStarvationZeroSpendTransaction(generateRandomTransaction());
        Assert.assertEquals("Ok", result);
    }

    @Test
    public void createNewGenesisZeroSpendTransaction() {
        TransactionData transactionData = generateRandomTransaction();
        Hash zeroSpendTransactionRequestHash = generateRandomHash();
        ZeroSpendTransactionRequest zeroSpendTransactionRequest = new ZeroSpendTransactionRequest();
        zeroSpendTransactionRequest.setTransactionData(transactionData);
        zeroSpendTransactionRequest.setHash(zeroSpendTransactionRequestHash);

        String result = transactionCreationService.createNewGenesisZeroSpendTransaction(zeroSpendTransactionRequest);

        Assert.assertEquals("Ok", result);
    }


    @Test
    public void createGenesisTransactions_noExceptionIsThrown() {
        try {
            transactionCreationService.createGenesisTransactions();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}