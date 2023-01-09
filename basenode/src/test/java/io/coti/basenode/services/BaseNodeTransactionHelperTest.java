package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.GetUserTokensRequestCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.*;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {BaseNodeTransactionHelper.class, BaseNodeTransactionService.class, AddressTransactionsHistories.class,
        TransactionCrypto.class, BaseNodeBalanceService.class, BaseNodeConfirmationService.class, ClusterService.class, Transactions.class,
        TransactionIndexes.class, ExpandedTransactionTrustScoreCrypto.class, BaseNodeCurrencyService.class, BaseNodeMintingService.class,
        BaseNodeEventService.class, INetworkService.class, CurrencyNameIndexes.class, TransactionIndexService.class,
        BaseNodeValidationService.class, BaseNodeDspVoteService.class, ClusterHelper.class, JacksonSerializer.class, RestTemplate.class,
        CurrencyTypeRegistrationCrypto.class, GetUserTokensRequestCrypto.class, UserCurrencyIndexes.class

})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j

public class BaseNodeTransactionHelperTest {

    @Autowired
    private BaseNodeTransactionService transactionService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @MockBean
    private ITransactionPropagationCheckService transactionPropagationCheckService;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @MockBean
    private BaseNodeBalanceService balanceService;
    @Autowired
    private BaseNodeConfirmationService baseNodeConfirmationService;
    @Autowired
    private ClusterService clusterService;
    @MockBean
    private ISourceSelector sourceSelector;
    @MockBean
    private TrustChainConfirmationService trustChainConfirmationService;
    @MockBean
    private Transactions transactions;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @MockBean
    private BaseNodeCurrencyService currencyService;
    @MockBean
    private BaseNodeMintingService mintingService;
    @MockBean
    private BaseNodeEventService eventService;

    @MockBean
    private IDatabaseConnector databaseConnector;
    @MockBean
    private Currencies currencies;
    @MockBean
    private CurrencyNameIndexes currencyNameIndexes;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private IValidationService validationService;
    @MockBean
    private IDspVoteService dspVoteService;
    @MockBean
    private IClusterHelper clusterHelper;
    @MockBean
    private JacksonSerializer jacksonSerializer;
    @MockBean
    private IChunkService chunkService;
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @Autowired
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;

    @Autowired
    private UserCurrencyIndexes userCurrencyIndexes;
    @Autowired
    protected BaseNodeEventService baseNodeEventService;
    @MockBean
    private ISender sender;

    @Test
    public void getTransaction_updateTransactionOnCluster_coverage() {
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
    public void attachTransactionToCluster_addToTCCCluster() {
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
    public void addExistingTransactionOnInit_coverage() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        // Initial state
        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
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
    public void addMissingTransactionOnInit_coverage() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);

        // Initial state
        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
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
    public void addTransactionToTrustChainConfirmationCluster_verifyAddition() {
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
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
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        clusterService.addTransactionToTrustChainConfirmationCluster(secondTransactionData);
        copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        Assertions.assertNotNull(copyTrustChainConfirmationCluster.get(secondTransactionData.getHash()));
    }

    @Test
    public void endHandleRejectedTransaction_verifyAllMethodsCalled() {
        TransactionData rejectedTransactionData = TransactionTestUtils.createRandomTransaction();
        transactionHelper.continueHandleRejectedTransaction(rejectedTransactionData);
        verify(transactions, atLeastOnce()).deleteByHash(rejectedTransactionData.getHash());
    }

    @Test
    public void removeAddressTransactionHistory_verifyRemoval() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Hash addressHash = baseTransactionData.getAddressHash();
            AddressTransactionsHistory addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
            addressHistory.addTransactionHashToHistory(transactionData.getHash());
            when(addressTransactionsHistories.getByHash(addressHash)).thenReturn(addressHistory);
        });

        transactionHelper.removeAddressTransactionHistory(transactionData);
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Assertions.assertEquals(Sets.newConcurrentHashSet(), addressTransactionsHistories.getByHash(baseTransactionData.getAddressHash()).getTransactionsHistory());
        });
    }

    @Test
    public void removeNoneIndexedTransaction_verifyRemoval() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Set<Hash> noneIndexedTransactionHashes = new HashSet<>(Collections.singleton(transactionData.getHash()));
        ReflectionTestUtils.setField(transactionHelper, "noneIndexedTransactionHashes", noneIndexedTransactionHashes);
        transactionHelper.removeNoneIndexedTransaction(transactionData);
        Assertions.assertEquals(Sets.newConcurrentHashSet(), noneIndexedTransactionHashes);
    }
}
