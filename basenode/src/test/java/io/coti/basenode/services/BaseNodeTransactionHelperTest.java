package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {BaseNodeTransactionHelper.class, BaseNodeTransactionService.class, ClusterService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeTransactionHelperTest {

    @Autowired
    private BaseNodeTransactionHelper transactionHelper;
    @MockBean
    private ITransactionPropagationCheckService transactionPropagationCheckServiceLocal;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistoriesLocal;
    @MockBean
    private BaseNodeBalanceService balanceServiceLocal;
    @MockBean
    private Transactions transactionsLocal;
    @MockBean
    private TransactionIndexes transactionIndexesLocal;
    @MockBean
    private BaseNodeEventService eventService;

    @BeforeEach
    void init() {
        clusterService = new ClusterService();
        nodeTransactionHelper = transactionHelper;
        nodeEventService = eventService;
        transactionIndexes = transactionIndexesLocal;
        addressTransactionsHistories = addressTransactionsHistoriesLocal;
        transactions = transactionsLocal;
        transactionPropagationCheckService = transactionPropagationCheckServiceLocal;
        balanceService = balanceServiceLocal;
        clusterService.init();
    }

    @Test
    void getTransaction_updateTransactionOnCluster_coverage() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
        boolean foundTransaction = trustChainConfirmationTransactionHashes.contains(transactionData.getHash());
        Assertions.assertFalse(foundTransaction);

        int originalSize = clusterService.getCopyTrustChainConfirmationCluster().size();
        transactionHelper.updateTransactionOnCluster(transactionData);
        TransactionData updatedTransactionData = clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash());
        int updatedAmount = clusterService.getCopyTrustChainConfirmationCluster().size();
        Assertions.assertNotNull(updatedTransactionData);
        Assertions.assertTrue(updatedAmount > originalSize);

        transactionHelper.updateTransactionOnCluster(transactionData);
        updatedTransactionData = clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash());
        int updatedAmount2 = clusterService.getCopyTrustChainConfirmationCluster().size();
        Assertions.assertNotNull(updatedTransactionData);
        Assertions.assertEquals(updatedAmount, updatedAmount2);
    }

    @Test
    void attachTransactionToCluster_addToTCCCluster() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        // Initial state
        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
        ArrayList<HashSet<Hash>> sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();
        long initialSourcesAmount = clusterService.getTotalSources();
        int initialTotalSources = sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum();
        Assertions.assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        Assertions.assertFalse(trustChainConfirmationTransactionHashes.contains(transactionData.getHash()));

        Assertions.assertTrue(sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).isEmpty());
        Assertions.assertNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));

        transactionHelper.attachTransactionToCluster(transactionData);
        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        Assertions.assertEquals(initialSourcesAmount + 1, totalSources);
        Assertions.assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        Assertions.assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        Assertions.assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
    }

    @Test
    void addExistingTransactionOnInit_coverage() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        // Initial state
        ArrayList<HashSet<Hash>> sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();
        long initialSourcesAmount = clusterService.getTotalSources();
        int initialTotalSources = sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum();
        Assertions.assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        clusterService.addExistingTransactionOnInit(transactionData);
        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        Assertions.assertEquals(initialSourcesAmount + 1, totalSources);
        Assertions.assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        Assertions.assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        Assertions.assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
    }

    @Test
    void addMissingTransactionOnInit_coverage() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        // Initial state
        ArrayList<HashSet<Hash>> sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();
        long initialSourcesAmount = clusterService.getTotalSources();
        int initialTotalSources = sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum();
        Assertions.assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        Set<Hash> trustChainUnconfirmedExistingTransactionHashes = new HashSet<>();
        clusterService.addMissingTransactionOnInit(transactionData, trustChainUnconfirmedExistingTransactionHashes);

        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        Assertions.assertEquals(initialSourcesAmount + 1, totalSources);
        Assertions.assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        Assertions.assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        Assertions.assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
    }

    @Test
    void addTransactionToTrustChainConfirmationCluster_verifyAddition() {
        when(eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        clusterService.addTransactionToTrustChainConfirmationCluster(transactionData);
        ConcurrentHashMap<Hash, TransactionData> copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();

        Assertions.assertNull(copyTrustChainConfirmationCluster.get(transactionData.getHash()));

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        clusterService.addTransactionToTrustChainConfirmationCluster(transactionData);
        copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        Assertions.assertNotNull(copyTrustChainConfirmationCluster.get(transactionData.getHash()));

        TransactionData secondTransactionData = TransactionTestUtils.createRandomTransaction();
        when(eventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        clusterService.addTransactionToTrustChainConfirmationCluster(secondTransactionData);
        copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        Assertions.assertNotNull(copyTrustChainConfirmationCluster.get(secondTransactionData.getHash()));
    }

    @Test
    void endHandleRejectedTransaction_verifyAllMethodsCalled() {
        TransactionData rejectedTransactionData = TransactionTestUtils.createRandomTransaction();
        transactionHelper.continueHandleRejectedTransaction(rejectedTransactionData);
        verify(transactions, atLeastOnce()).deleteByHash(rejectedTransactionData.getHash());
    }

    @Test
    void removeAddressTransactionHistory_verifyRemoval() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Hash addressHash = baseTransactionData.getAddressHash();
            AddressTransactionsHistory addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
            addressHistory.addTransactionHashToHistory(transactionData.getHash());
            when(addressTransactionsHistories.getByHash(addressHash)).thenReturn(addressHistory);
        });

        transactionHelper.removeAddressTransactionHistory(transactionData);
        transactionData.getBaseTransactions().forEach(baseTransactionData -> Assertions.assertEquals(Sets.newConcurrentHashSet(), addressTransactionsHistories.getByHash(baseTransactionData.getAddressHash()).getTransactionsHistory()));
    }

    @Test
    void removeNoneIndexedTransaction_verifyRemoval() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Set<Hash> noneIndexedTransactionHashes = new HashSet<>(Collections.singleton(transactionData.getHash()));
        ReflectionTestUtils.setField(transactionHelper, "noneIndexedTransactionHashes", noneIndexedTransactionHashes);
        transactionHelper.removeNoneIndexedTransaction(transactionData);
        Assertions.assertEquals(Sets.newConcurrentHashSet(), noneIndexedTransactionHashes);
    }
}