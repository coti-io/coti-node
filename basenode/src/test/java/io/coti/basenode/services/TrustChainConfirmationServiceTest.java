package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IEventService;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.basenode.utils.TestConstants.MAX_TRUST_SCORE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TrustChainConfirmationService.class, ClusterHelper.class, ClusterService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class TrustChainConfirmationServiceTest {

    @Autowired
    TrustChainConfirmationService trustChainConfirmationServiceLocal;
    @Autowired
    ClusterHelper clusterHelperLocal;
    @Autowired
    ClusterService clusterServiceLocal;
    @MockBean
    IEventService nodeEventServiceLocal;
    @MockBean
    BaseNodeTransactionHelper baseNodeTransactionHelper;
    @MockBean
    TransactionIndexes transactionIndexesLocal;
    @MockBean
    Transactions transactionsLocal;

    @BeforeEach
    public void init() {
        trustChainConfirmationService = trustChainConfirmationServiceLocal;
        clusterService = clusterServiceLocal;
        clusterHelper = clusterHelperLocal;
        nodeEventService = nodeEventServiceLocal;
        nodeTransactionHelper = baseNodeTransactionHelper;
        transactionIndexes = transactionIndexesLocal;
        transactions = transactionsLocal;
    }

    @Test
    void getTrustChainConfirmedTransactions_preTrustScoreConsensus() {
        // Before Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();
        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationServiceLocal.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationServiceLocal.getTrustChainConfirmedTransactions();


        Assertions.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) Math.round(trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore()));
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    void getTrustChainConfirmedTransactions_postTrustScoreConsensus() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);
        when(nodeTransactionHelper.isDspConfirmed(any(TransactionData.class))).thenReturn(true);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assertions.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        TransactionData childTransactionData = TransactionTestUtils.createRandomTransaction();
        childTransactionData.setAttachmentTime(Instant.now());
        childTransactionData.setSenderTrustScore(majorityTCCThreshold);
        childTransactionData.setLeftParentHash(transactionData.getHash());
        List<Hash> childrenHashes = new ArrayList<>();
        childrenHashes.add(childTransactionData.getHash());
        transactionData.setChildrenTransactionHashes(childrenHashes);

        trustChainConfirmationCluster.put(childTransactionData.getHash(), childTransactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);

        DspConsensusResult childDSPConsensusResult = new DspConsensusResult(transactionData.getHash());
        childDSPConsensusResult.setDspConsensus(true);
        childTransactionData.setDspConsensusResult(childDSPConsensusResult);

        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assertions.assertTrue(trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore() > MAX_TRUST_SCORE);
        Assertions.assertEquals(1, trustChainConfirmedTransactions.size());
    }

    @Test
    void init_preTrustScoreConsensus() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // Before Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        TransactionData childTransactionData = TransactionTestUtils.createRandomTransaction();
        childTransactionData.setSenderTrustScore(majorityTCCThreshold);

        List<Hash> childrenHashes = new ArrayList<>();
        childrenHashes.add(childTransactionData.getHash());
        transactionData.setChildrenTransactionHashes(childrenHashes);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        when(transactions.getByHash(childTransactionData.getHash())).thenReturn(childTransactionData);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(transactionData.getRoundedSenderTrustScore() + childTransactionData.getRoundedSenderTrustScore(), (int) Math.round(trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore()));
        Assertions.assertFalse(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    void init_postTrustScoreConsensus() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        TransactionData childTransactionData = TransactionTestUtils.createRandomTransaction();
        childTransactionData.setAttachmentTime(Instant.now());

        List<Hash> childrenHashes = new ArrayList<>();
        childrenHashes.add(childTransactionData.getHash());
        transactionData.setChildrenTransactionHashes(childrenHashes);

        TransactionData grandChildTransactionData = TransactionTestUtils.createRandomTransaction();
        grandChildTransactionData.setAttachmentTime(Instant.now());

        List<Hash> grandChildrenHashes = new ArrayList<>();
        grandChildrenHashes.add(grandChildTransactionData.getHash());
        childTransactionData.setChildrenTransactionHashes(grandChildrenHashes);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        when(transactions.getByHash(childTransactionData.getHash())).thenReturn(childTransactionData);
        when(transactions.getByHash(grandChildTransactionData.getHash())).thenReturn(grandChildTransactionData);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        DspConsensusResult dspConsensusResult = new DspConsensusResult(grandChildTransactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        grandChildTransactionData.setDspConsensusResult(dspConsensusResult);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    void init_missingChildDSPCParentNotDSPC_tccByChild() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());

        TransactionData childTransactionData = TransactionTestUtils.createRandomTransaction();
        childTransactionData.setAttachmentTime(Instant.now());
        DspConsensusResult dspConsensusResult = new DspConsensusResult(childTransactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        childTransactionData.setDspConsensusResult(dspConsensusResult);

        List<Hash> childrenHashes = new ArrayList<>();
        childrenHashes.add(childTransactionData.getHash());
        transactionData.setChildrenTransactionHashes(childrenHashes);

        when(transactions.getByHash(childTransactionData.getHash())).thenReturn(childTransactionData);
        TransactionIndexData childTransactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "8".getBytes());
        when(transactionIndexes.getByHash(childTransactionData.getHash())).thenReturn(childTransactionIndexData);
        when(nodeTransactionHelper.isDspConfirmed(any(TransactionData.class))).thenReturn(true);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(childTransactionData.getSenderTrustScore() + transactionData.getSenderTrustScore(), trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore(), 0);
    }

    @Test
    void getTrustChainConfirmedTransactions_postDSPCEventTransactionDSPC_transactionAdded() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        when(nodeTransactionHelper.isDspConfirmed(any(TransactionData.class))).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);
        transactionData.setTrustChainConsensus(false);
        transactionData.setTrustChainTrustScore(MAX_TRUST_SCORE + 1);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        transactionData.setAttachmentTime(Instant.now());
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);

        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assertions.assertEquals(transactionData.getHash(), trustChainConfirmedTransactions.get(0).getHash());
    }

    @Test
    void getTrustChainConfirmedTransactions_preDSPCEventTransactionNotDSPC_transactionNotAdded() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);
        transactionData.setTrustChainConsensus(true);
        transactionData.setTrustChainTrustScore(MAX_TRUST_SCORE + 1);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        transactionData.setAttachmentTime(Instant.now());
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmationService.getTrustChainConfirmedTransactions();

        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    void getTrustChainConfirmedTransactions_postDSPCEventTransactionNotDSPC_transactionNotAdded() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);
        transactionData.setTrustChainConsensus(false);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        transactionData.setAttachmentTime(Instant.now());
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmationService.getTrustChainConfirmedTransactions();

        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

}
