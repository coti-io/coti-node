package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.when;
import static testUtils.TestUtils.generateRandomTransaction;

@ContextConfiguration(classes =
        {BaseNodeTransactionService.class,
                RocksDBConnector.class,
                Transactions.class}
)

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeTransactionServiceTest {
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private IValidationService validationService;

    @Autowired
    private Transactions transactions;
    @Autowired
    private ITransactionService baseNodeTransactionService;

    @Test
    public void handlePropagatedTransaction_noExceptionIsThrown() {
        log.info("Starting  - " + this.getClass().getSimpleName());

        try {
            TransactionData transactionData = generateRandomTransaction();
            when(validationService.validatePropagatedTransactionDataIntegrity(transactionData)).thenReturn(true);
            when(validationService.validateBalancesAndAddToPreBalance(transactionData)).thenReturn(true);
            baseNodeTransactionService.handlePropagatedTransaction(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
