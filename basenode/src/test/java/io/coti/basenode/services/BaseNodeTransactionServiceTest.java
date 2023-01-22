package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IChunkService;
import io.coti.basenode.services.interfaces.IEventService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionPropagationCheckService;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeTransactionHelper;
import static io.coti.basenode.services.BaseNodeServiceManager.transactions;

@ContextConfiguration(classes = {BaseNodeTransactionService.class, BaseNodeTransactionHelper.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeTransactionServiceTest {

    @Autowired
    private BaseNodeTransactionService baseNodeTransactionService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @MockBean
    private Transactions transactionsLocal;
    @MockBean
    protected IChunkService chunkService;
    @MockBean
    protected IEventService eventService;
    @MockBean
    protected ITransactionPropagationCheckService transactionPropagationCheckService;

    @BeforeEach
    void init() {
        nodeTransactionHelper = transactionHelper;
        transactions = transactionsLocal;
    }

    @Test
    void check_transaction_already_propagated_and_start_handle() {
        AtomicBoolean isTransactionAlreadyPropagated = new AtomicBoolean(false);
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        baseNodeTransactionService.checkTransactionAlreadyPropagatedAndStartHandle(transactionData, isTransactionAlreadyPropagated);
        Assertions.assertTrue(transactionHelper.isTransactionHashProcessing(transactionData.getHash()));
    }

    @Test
    void has_one_of_parents_missing() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Assertions.assertFalse(baseNodeTransactionService.hasOneOfParentsMissing(transactionData));
    }
}