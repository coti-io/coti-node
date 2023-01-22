package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.LockData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.model.UnconfirmedReceivedTransactionHashes;
import io.coti.basenode.services.BaseNodeEventService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.fullnode.data.UnconfirmedReceivedTransactionHashFullNodeData;
import io.coti.fullnode.websocket.WebSocketSender;
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
import org.springframework.test.util.ReflectionTestUtils;
import utils.TestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.fullnode.services.NodeServiceManager.webSocketSender;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {TransactionService.class,
        TransactionPropagationCheckService.class, UnconfirmedReceivedTransactionHashes.class
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private ValidationService validationServiceLocal;
    @MockBean
    private Transactions transactionsLocal;
    @MockBean
    private BaseNodeEventService eventService;
    @MockBean
    private WebSocketSender webSocketSenderLocal;
    @MockBean
    private TransactionPropagationCheckService transactionPropagationCheckServiceLocal;
    @MockBean
    private UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashesLocal;

    @BeforeEach
    void init() {
        webSocketSender = webSocketSenderLocal;
        nodeEventService = eventService;
        validationService = validationServiceLocal;
        unconfirmedReceivedTransactionHashes = unconfirmedReceivedTransactionHashesLocal;
        transactionPropagationCheckService = transactionPropagationCheckServiceLocal;
        nodeTransactionHelper = transactionHelper;
        transactions = transactionsLocal;
        transactionService.init();
    }

    @Test
    void removeTransactionHashFromUnconfirmed_verifyRemoved() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        Map<Hash, UnconfirmedReceivedTransactionHashData> unconfirmedReceivedTransactionHashesMap = new ConcurrentHashMap<>();
        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData = new UnconfirmedReceivedTransactionHashData(transactionData.getHash());
        UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashFullNodeData =
                new UnconfirmedReceivedTransactionHashFullNodeData(unconfirmedReceivedTransactionHashData, 3);
        unconfirmedReceivedTransactionHashesMap.put(unconfirmedReceivedTransactionHashData.getTransactionHash(), unconfirmedReceivedTransactionHashFullNodeData);
        ReflectionTestUtils.setField(transactionPropagationCheckService, "unconfirmedReceivedTransactionHashesMap", unconfirmedReceivedTransactionHashesMap);
        ReflectionTestUtils.setField(transactionPropagationCheckService, "transactionHashLockData", new LockData());
        when(unconfirmedReceivedTransactionHashes.getByHash(transactionData.getHash())).thenReturn(unconfirmedReceivedTransactionHashFullNodeData);
        doCallRealMethod().when(transactionPropagationCheckService).removeTransactionHashFromUnconfirmed(transactionData.getHash());
        doCallRealMethod().when(transactionPropagationCheckService).removeConfirmedReceiptTransaction(transactionData.getHash());
        transactionService.removeTransactionHashFromUnconfirmed(transactionData);
        verify(transactionPropagationCheckService, atLeastOnce()).removeTransactionHashFromUnconfirmed(transactionData.getHash());
        verify(unconfirmedReceivedTransactionHashes, atLeastOnce()).deleteByHash(transactionData.getHash());
        Assertions.assertEquals(new ConcurrentHashMap<>(), unconfirmedReceivedTransactionHashesMap);
    }
}