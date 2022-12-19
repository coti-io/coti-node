package io.coti.basenode.services;

import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utils.TransactionTestUtils;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.coti.basenode.utils.TestConstants.MAX_TRUST_SCORE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TrustChainConfirmationService.class,
        ClusterHelper.class, BaseNodeTransactionHelper.class
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j

public class TrustChainConfirmationServiceTest {

    @Autowired
    private TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    private ClusterHelper clusterHelper;
    @Autowired
    private BaseNodeTransactionHelper transactionHelper;
    @MockBean
    private BaseNodeEventService baseNodeEventService;
    @MockBean
    private Transactions transactions;

    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @MockBean
    private BaseNodeCurrencyService currencyService;
    @MockBean
    private IMintingService mintingService;
    @MockBean
    private INetworkService networkService;


    @Test
    public void getTrustChainConfirmedTransactions_preTrustScoreConsensus() {
        // Before Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();
        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) Math.round(trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore()));
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    public void getTrustChainConfirmedTransactions_postTrustScoreConsensus() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash("7"), index, "7".getBytes(StandardCharsets.UTF_8));
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assert.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

        TransactionData childTransactionData = TransactionTestUtils.createRandomTransaction();
        childTransactionData.setAttachmentTime(Instant.now());
        childTransactionData.setSenderTrustScore(majorityTCCThreshold);
        childTransactionData.setLeftParentHash(transactionData.getHash());
        List<Hash> childrenHashes = new ArrayList<>();
        childrenHashes.add(childTransactionData.getHash());
        transactionData.setChildrenTransactionHashes(childrenHashes);

        trustChainConfirmationCluster.put(childTransactionData.getHash(), childTransactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);

        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assert.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

        DspConsensusResult childDSPConsensusResult = new DspConsensusResult(transactionData.getHash());
        childDSPConsensusResult.setDspConsensus(true);
        childTransactionData.setDspConsensusResult(childDSPConsensusResult);

        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assert.assertTrue(trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore() > MAX_TRUST_SCORE);
        Assert.assertEquals(1, trustChainConfirmedTransactions.size());
    }

    @Test
    public void init_preTrustScoreConsensus() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // Before Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash("7"), index, "7".getBytes(StandardCharsets.UTF_8));
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

        TransactionData childTransactionData = TransactionTestUtils.createRandomTransaction();
        childTransactionData.setSenderTrustScore(majorityTCCThreshold);

        List<Hash> childrenHashes = new ArrayList<>();
        childrenHashes.add(childTransactionData.getHash());
        transactionData.setChildrenTransactionHashes(childrenHashes);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

        when(transactions.getByHash(childTransactionData.getHash())).thenReturn(childTransactionData);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(transactionData.getRoundedSenderTrustScore() + childTransactionData.getRoundedSenderTrustScore(), (int) Math.round(trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore()));
        Assert.assertFalse(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    public void init_postTrustScoreConsensus() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash("7"), index, "7".getBytes(StandardCharsets.UTF_8));
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

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

        Assert.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

        when(transactions.getByHash(childTransactionData.getHash())).thenReturn(childTransactionData);
        when(transactions.getByHash(grandChildTransactionData.getHash())).thenReturn(grandChildTransactionData);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

        DspConsensusResult dspConsensusResult = new DspConsensusResult(grandChildTransactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        grandChildTransactionData.setDspConsensusResult(dspConsensusResult);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    public void init_missingChildDSPCParentNotDSPC_tccByChild() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash("7"), index, "7".getBytes(StandardCharsets.UTF_8));
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(majorityTCCThreshold);

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();

        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(0, (int) trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore());
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());

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
        TransactionIndexData childTransactionIndexData = new TransactionIndexData(new Hash("8"), index, "8".getBytes(StandardCharsets.UTF_8));
        when(transactionIndexes.getByHash(childTransactionData.getHash())).thenReturn(childTransactionIndexData);

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assert.assertEquals(childTransactionData.getSenderTrustScore(), trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore(), 0);
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    public void getTrustChainConfirmedTransactions_postDSPCEventTransactionDSPC_transactionAdded() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash("7"), index, "7".getBytes(StandardCharsets.UTF_8));
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
        trustChainConfirmationService.getTrustChainConfirmedTransactions();

        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();
        Assert.assertEquals(transactionData.getHash(), trustChainConfirmedTransactions.get(0).getHash());
    }

    @Test
    public void getTrustChainConfirmedTransactions_preDSPCEventTransactionNotDSPC_transactionNotAdded() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash("7"), index, "7".getBytes(StandardCharsets.UTF_8));
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
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    public void getTrustChainConfirmedTransactions_postDSPCEventTransactionNotDSPC_transactionNotAdded() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
        // After Event.TRUST_SCORE_CONSENSUS
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);

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
        Assert.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

}
