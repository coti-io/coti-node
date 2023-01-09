package io.coti.fullnode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.data.TransactionStatus;
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

import java.time.Instant;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

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
    @MockBean
    private RejectedTransactions rejectedTransactions;

    @BeforeEach
    public void init() {
        transactionService.init();
    }

    @Test
    public void removeDataFromMemory_verifyDataRemoved() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        Map<Hash, NavigableMap<Instant, Set<Hash>>> addressToTransactionsByAttachmentMap = new ConcurrentHashMap<>();
        NavigableMap<Instant, Set<Hash>> transactionHashesByAttachmentMap = new ConcurrentSkipListMap<>();
        Set<Hash> transactionHashSet = Sets.newConcurrentHashSet();
        transactionHashSet.add(transactionData.getHash());
        transactionHashesByAttachmentMap.put(transactionData.getAttachmentTime(), transactionHashSet);
        addressToTransactionsByAttachmentMap.put(transactionData.getBaseTransactions().get(0).getAddressHash(), transactionHashesByAttachmentMap);
        ReflectionTestUtils.setField(transactionService, "addressToTransactionsByAttachmentMap", addressToTransactionsByAttachmentMap);

        transactionService.removeDataFromMemory(transactionData);
        Assertions.assertEquals(Sets.newConcurrentHashSet(), transactionHashSet);
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

    @Test
    public void handlePropagatedRejectedTransaction_noRejectionIfEventNotHappened() {
        RejectedTransactionData rejectedTransaction = TestUtils.createRejectedTransaction();
        when(eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        transactionService.handlePropagatedRejectedTransaction(rejectedTransaction);
        verify(transactions, never()).getByHash(rejectedTransaction.getHash());
    }

    @Test
    public void handlePropagatedRejectedTransaction_noRejectionIfTransactionNull() {
        RejectedTransactionData rejectedTransaction = TestUtils.createRejectedTransaction();
        rejectedTransaction.setHash(null);
        when(eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        transactionService.handlePropagatedRejectedTransaction(rejectedTransaction);
        verify(transactions, never()).getByHash(rejectedTransaction.getHash());
    }

    @Test
    public void handlePropagatedRejectedTransaction_noRejectionIfTransactionDoesNotExist() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        RejectedTransactionData rejectedTransaction = TestUtils.createRejectedTransaction(transactionData);
        when(eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        when(transactionHelper.isTransactionHashExists(rejectedTransaction.getHash())).thenReturn(false);
        transactionService.handlePropagatedRejectedTransaction(rejectedTransaction);
        verify(validationService, never()).validatePropagatedRejectedTransactionDataIntegrity(rejectedTransaction);
    }

    @Test
    public void handlePropagatedRejectedTransaction_noRejectionIfDataIntegrityCheckFailed() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        RejectedTransactionData rejectedTransaction = TestUtils.createRejectedTransaction(transactionData);
        when(eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        when(transactionHelper.isTransactionHashExists(rejectedTransaction.getHash())).thenReturn(true);
        when(validationService.validatePropagatedRejectedTransactionDataIntegrity(rejectedTransaction)).thenReturn(false);
        transactionService.handlePropagatedRejectedTransaction(rejectedTransaction);
        verify(webSocketSender, never()).notifyTransactionHistoryChange(transactionData, TransactionStatus.REJECTED);
    }

    @Test
    public void handlePropagatedRejectedTransaction() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        RejectedTransactionData rejectedTransaction = TestUtils.createRejectedTransaction(transactionData);
        when(eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        when(transactionHelper.isTransactionHashExists(rejectedTransaction.getHash())).thenReturn(true);
        when(validationService.validatePropagatedRejectedTransactionDataIntegrity(rejectedTransaction)).thenReturn(true);
        when(transactions.getByHash(rejectedTransaction.getHash())).thenReturn(transactionData).thenReturn(null);
        transactionService.handlePropagatedRejectedTransaction(rejectedTransaction);
        verify(transactionHelper, atLeastOnce()).isTransactionHashExists(rejectedTransaction.getHash());
        verify(validationService, atLeastOnce()).validatePropagatedRejectedTransactionDataIntegrity(rejectedTransaction);
        verify(webSocketSender, atLeastOnce()).notifyTransactionHistoryChange(transactionData, TransactionStatus.REJECTED);
    }
}