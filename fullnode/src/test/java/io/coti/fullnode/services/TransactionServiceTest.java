package io.coti.fullnode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.*;
import io.coti.basenode.services.BaseNodeEventService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.*;
import io.coti.fullnode.crypto.ResendTransactionRequestCrypto;
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

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {TransactionService.class,
        TransactionPropagationCheckService.class, UnconfirmedReceivedTransactionHashes.class
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private ValidationService validationService;
    @MockBean
    private IDspVoteService dspVoteService;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private IClusterHelper clusterHelper;
    @MockBean
    private Transactions transactions;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private JacksonSerializer jacksonSerializer;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private ICurrencyService currencyService;
    @MockBean
    private IMintingService mintingService;
    @MockBean
    private IChunkService chunkService;
    @MockBean
    private BaseNodeEventService eventService;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private WebSocketSender webSocketSender;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private PotService potService;
    @MockBean
    private TransactionPropagationCheckService transactionPropagationCheckService;
    @MockBean
    private UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashes;
    @MockBean
    private ResendTransactionRequestCrypto resendTransactionRequestCrypto;

    @BeforeEach
    public void init() {
        transactionService.init();
    }

    @Test
    public void removeTransactionHashFromUnconfirmed_verifyRemoved() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        Map<Hash, UnconfirmedReceivedTransactionHashData> unconfirmedReceivedTransactionHashesMap = new ConcurrentHashMap<>();
        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData = new UnconfirmedReceivedTransactionHashData(transactionData.getHash());
        UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashFullNodeData =
                new UnconfirmedReceivedTransactionHashFullNodeData(unconfirmedReceivedTransactionHashData, 3);
        unconfirmedReceivedTransactionHashesMap.put(unconfirmedReceivedTransactionHashData.getTransactionHash(), unconfirmedReceivedTransactionHashFullNodeData);
        ReflectionTestUtils.setField(transactionPropagationCheckService, "unconfirmedReceivedTransactionHashesMap", unconfirmedReceivedTransactionHashesMap);
        ReflectionTestUtils.setField(transactionPropagationCheckService, "unconfirmedReceivedTransactionHashes", unconfirmedReceivedTransactionHashes);
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