package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ContextConfiguration(classes = {BaseNodeTransactionHelper.class, BaseNodeTransactionService.class, AddressTransactionsHistories.class,
        TransactionCrypto.class, BaseNodeBalanceService.class, BaseNodeConfirmationService.class, ClusterService.class, Transactions.class,
        TransactionIndexes.class, ExpandedTransactionTrustScoreCrypto.class, BaseNodeCurrencyService.class, BaseNodeMintingService.class,
        BaseNodeEventService.class, INetworkService.class, CurrencyNameIndexes.class, TransactionIndexService.class,
        BaseNodeValidationService.class, BaseNodeDspVoteService.class, ClusterHelper.class, JacksonSerializer.class, RestTemplate.class,
        CurrencyTypeRegistrationCrypto.class, GetUserTokensRequestCrypto.class, UserCurrencyIndexes.class

})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j

public class BaseNodeTransactionHelperTest {

    @Autowired
    private BaseNodeTransactionService transactionService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
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
    @Autowired
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

    @Test
    public void getTransaction_updateTransactionOnCluster_coverage() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
        boolean foundTransaction = trustChainConfirmationTransactionHashes.contains(transactionData.getHash());
        Assert.assertFalse(foundTransaction);

        int originalSize = clusterService.getCopyTrustChainConfirmationCluster().size();
        transactionHelper.updateTransactionOnCluster(transactionData);
        TransactionData updatedTransactionData = clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash());
        int updatedAmount = clusterService.getCopyTrustChainConfirmationCluster().size();
        Assert.assertNotNull(updatedTransactionData);
        Assert.assertTrue(updatedAmount > originalSize);

        transactionHelper.updateTransactionOnCluster(transactionData);
        updatedTransactionData = clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash());
        int updatedAmount2 = clusterService.getCopyTrustChainConfirmationCluster().size();
        Assert.assertNotNull(updatedTransactionData);
        Assert.assertEquals(updatedAmount, updatedAmount2);
    }

    @Test
    public void attachTransactionToCluster_addToTCCCluster() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        // Initial state
        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
        ArrayList<HashSet<Hash>> sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();
        long initialSourcesAmount = clusterService.getTotalSources();
        int initialTotalSources = sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum();
        Assert.assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        Assert.assertFalse(trustChainConfirmationTransactionHashes.contains(transactionData.getHash()));

        Assert.assertTrue(sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).isEmpty());
        Assert.assertNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));

        transactionHelper.attachTransactionToCluster(transactionData);
        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        Assert.assertEquals(initialSourcesAmount + 1, totalSources);
        Assert.assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        Assert.assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        Assert.assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
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
        Assert.assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        clusterService.addExistingTransactionOnInit(transactionData);
        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        Assert.assertEquals(initialSourcesAmount + 1, totalSources);
        Assert.assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        Assert.assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        Assert.assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
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
        Assert.assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        Set<Hash> trustChainUnconfirmedExistingTransactionHashes = new HashSet<>();
        clusterService.addMissingTransactionOnInit(transactionData, trustChainUnconfirmedExistingTransactionHashes);

        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        Assert.assertEquals(initialSourcesAmount + 1, totalSources);
        Assert.assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        Assert.assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        Assert.assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
    }

    @Test
    public void addTransactionToTrustChainConfirmationCluster_verifyAddition() {
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(true);
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        clusterService.addTransactionToTrustChainConfirmationCluster(transactionData);
        ConcurrentHashMap<Hash, TransactionData> copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();

        Assert.assertNull(copyTrustChainConfirmationCluster.get(transactionData.getHash()));

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash("7"), index, "7".getBytes(StandardCharsets.UTF_8));
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        clusterService.addTransactionToTrustChainConfirmationCluster(transactionData);
        copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        Assert.assertNotNull(copyTrustChainConfirmationCluster.get(transactionData.getHash()));

        TransactionData secondTransactionData = TransactionTestUtils.createRandomTransaction();
        when(baseNodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)).thenReturn(false);
        clusterService.addTransactionToTrustChainConfirmationCluster(secondTransactionData);
        copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        Assert.assertNotNull(copyTrustChainConfirmationCluster.get(secondTransactionData.getHash()));
    }

}
