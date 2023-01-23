package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionPropagationCheckService;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.basenode.utils.TestConstants.NATIVE_CURRENCY_HASH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {BaseNodeTransactionHelper.class, BaseNodeTransactionService.class, ClusterService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeTransactionHelperTest {

    @Autowired
    private BaseNodeTransactionHelper baseNodeTransactionHelper;

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
    @MockBean
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @MockBean
    private IConfirmationService confirmationServiceLocal;

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
        currencyService = baseNodeCurrencyService;
        confirmationService = confirmationServiceLocal;
        clusterService.init();
    }

    @Test
    void testValidateTransactionType_empty_type(CapturedOutput output) {
        assertFalse(baseNodeTransactionHelper.validateTransactionType(new TransactionData(new ArrayList<>())));
        assertTrue(output.getOut().contains("Transaction null has null type"));
    }

    @Test
    void testValidateTransactionType_null_transaction(CapturedOutput output) {
        assertFalse(baseNodeTransactionHelper.validateTransactionType(null));
        assertTrue(output.getOut().contains("Validate transaction type error"));
    }

    @Test
    void testValidateTransactionType_output_base_transactions_number_failure() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        assertFalse(baseNodeTransactionHelper.validateTransactionType(transactionData));
    }

    @Test
    void testValidateTransactionType_illegal_argument_exception(CapturedOutput output) {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setType(TransactionType.Chargeback);
        assertFalse(baseNodeTransactionHelper.validateTransactionType(transactionData));
        assertTrue(output.getOut().contains("Invalid transaction type"));
    }

    @Test
    void testValidateTransactionType_output_base_transactions_order_failure() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        Hash addressHash = baseTransactions.get(0).getAddressHash();
        baseTransactions.add(0, new NetworkFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), new Hash(NATIVE_CURRENCY_HASH), new BigDecimal(5), new BigDecimal(4), Instant.now()));
        baseTransactions.add(1, new FullNodeFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), new Hash(NATIVE_CURRENCY_HASH), new BigDecimal(5), Instant.now()));
        assertFalse(baseNodeTransactionHelper.validateTransactionType(transactionData));
    }

    @Test
    void testValidateTransactionType_original_currency_not_same_failure() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        Hash addressHash = baseTransactions.get(0).getAddressHash();
        baseTransactions.add(0, new NetworkFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), new Hash(NATIVE_CURRENCY_HASH), new BigDecimal(5), new BigDecimal(4), Instant.now()));
        baseTransactions.add(0, new FullNodeFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), new Hash(TransactionTestUtils.generateRandomHash().toHexString()), new BigDecimal(5), Instant.now()));
        assertFalse(baseNodeTransactionHelper.validateTransactionType(transactionData));
    }

    @Test
    void testValidateTransactionType_original_currency_null() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        Hash addressHash = baseTransactions.get(0).getAddressHash();
        baseTransactions.add(0, new NetworkFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), null, new BigDecimal(5), new BigDecimal(4), Instant.now()));
        baseTransactions.add(0, new FullNodeFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), new Hash(NATIVE_CURRENCY_HASH), new BigDecimal(5), Instant.now()));
        when(currencyService.getNativeCurrencyHash()).thenReturn(OriginatorCurrencyCrypto.calculateHash("COTI"));
        assertTrue(baseNodeTransactionHelper.validateTransactionType(transactionData));
    }

    @Test
    void testValidateTransactionType() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        Hash addressHash = baseTransactions.get(0).getAddressHash();
        baseTransactions.add(0, new NetworkFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), new Hash(NATIVE_CURRENCY_HASH), new BigDecimal(5), new BigDecimal(4), Instant.now()));
        baseTransactions.add(0, new FullNodeFeeData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(1), new Hash(NATIVE_CURRENCY_HASH), new BigDecimal(5), Instant.now()));
        assertTrue(baseNodeTransactionHelper.validateTransactionType(transactionData));
    }

    @Test
    void validateTransactionTimeFields() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        assertTrue(baseNodeTransactionHelper.validateTransactionTimeFields(transactionData));
    }

    @Test
    void validateTransactionTimeFields_false() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        Instant time = baseTransactions.get(0).getCreateTime().minus(65, ChronoUnit.MINUTES);
        baseTransactions.get(0).setCreateTime(time);
        assertFalse(baseNodeTransactionHelper.validateTransactionTimeFields(transactionData));
    }

    @Test
    void getTransaction_updateTransactionOnCluster_coverage() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();

        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
        boolean foundTransaction = trustChainConfirmationTransactionHashes.contains(transactionData.getHash());
        assertFalse(foundTransaction);

        int originalSize = clusterService.getCopyTrustChainConfirmationCluster().size();
        transactionHelper.attachTransactionToCluster(transactionData);
        TransactionData updatedTransactionData = clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash());
        int updatedAmount = clusterService.getCopyTrustChainConfirmationCluster().size();
        assertNotNull(updatedTransactionData);
        assertTrue(updatedAmount > originalSize);

        transactionHelper.attachTransactionToCluster(transactionData);
        updatedTransactionData = clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash());
        int updatedAmount2 = clusterService.getCopyTrustChainConfirmationCluster().size();
        assertNotNull(updatedTransactionData);
        assertEquals(updatedAmount, updatedAmount2);
    }

    @Test
    void testGetReceiverBaseTransactionAddressHash_null() {
        assertNull(baseNodeTransactionHelper.getReceiverBaseTransactionAddressHash(new TransactionData(new ArrayList<>())));
    }

    @Test
    void testGetReceiverBaseTransactionAddressHash() {
        ArrayList<BaseTransactionData> baseTransactionDataList = new ArrayList<>();
        Hash addressHash = TransactionTestUtils.generateRandomAddressHash();
        Hash currencyHash = TransactionTestUtils.generateRandomHash();
        BigDecimal amount = new BigDecimal(10);
        LocalDateTime atStartOfDayResult = LocalDate.of(1970, 1, 1).atStartOfDay();
        baseTransactionDataList.add(new ReceiverBaseTransactionData(addressHash, currencyHash, amount, currencyHash,
                amount, atStartOfDayResult.atZone(ZoneId.of("UTC")).toInstant()));
        TransactionData transactionData = mock(TransactionData.class);
        when(transactionData.getBaseTransactions()).thenReturn(baseTransactionDataList);
        assertSame(addressHash, baseNodeTransactionHelper.getReceiverBaseTransactionAddressHash(transactionData));
        verify(transactionData).getBaseTransactions();
    }

    @Test
    void testGetReceiverBaseTransactionHash_null() {
        assertNull(baseNodeTransactionHelper.getReceiverBaseTransactionHash(new TransactionData(new ArrayList<>())));
    }

    @Test
    void testGetReceiverBaseTransactionHash() {
        ArrayList<BaseTransactionData> baseTransactionDataList = new ArrayList<>();
        Hash addressHash = TransactionTestUtils.generateRandomAddressHash();
        Hash currencyHash = TransactionTestUtils.generateRandomHash();
        BigDecimal amount = new BigDecimal(10);
        LocalDateTime atStartOfDayResult = LocalDate.of(1970, 1, 1).atStartOfDay();
        BaseTransactionData baseTransactionData = new ReceiverBaseTransactionData(addressHash, currencyHash, amount, currencyHash,
                amount, atStartOfDayResult.atZone(ZoneId.of("UTC")).toInstant());
        baseTransactionDataList.add(baseTransactionData);
        BaseTransactionCrypto.getByBaseTransactionClass(ReceiverBaseTransactionData.class).createAndSetBaseTransactionHash(baseTransactionData);
        TransactionData transactionData = mock(TransactionData.class);
        when(transactionData.getBaseTransactions()).thenReturn(baseTransactionDataList);
        assertNotNull(baseNodeTransactionHelper.getReceiverBaseTransactionHash(transactionData));
        verify(transactionData).getBaseTransactions();
    }

    @Test
    void attachTransactionToCluster_addToTCCCluster() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        // Initial state
        final Set<Hash> trustChainConfirmationTransactionHashes = clusterService.getTrustChainConfirmationTransactionHashes();
        ArrayList<HashSet<Hash>> sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();
        long initialSourcesAmount = clusterService.getTotalSources();
        int initialTotalSources = sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum();
        assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        assertFalse(trustChainConfirmationTransactionHashes.contains(transactionData.getHash()));

        assertTrue(sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).isEmpty());
        assertNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));

        transactionHelper.attachTransactionToCluster(transactionData);
        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        assertEquals(initialSourcesAmount + 1, totalSources);
        assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
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
        assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        clusterService.addExistingTransactionOnInit(transactionData);
        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        assertEquals(initialSourcesAmount + 1, totalSources);
        assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
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
        assertEquals(initialSourcesAmount, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        int sourcesForTxAmount = sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size();

        Set<Hash> trustChainUnconfirmedExistingTransactionHashes = new HashSet<>();
        clusterService.addMissingTransactionOnInit(transactionData, trustChainUnconfirmedExistingTransactionHashes);

        long totalSources = clusterService.getTotalSources();
        sourceSetsByTrustScore = clusterService.getSourceSetsByTrustScore();

        assertEquals(initialSourcesAmount + 1, totalSources);
        assertEquals(initialTotalSources + 1, sourceSetsByTrustScore.stream().mapToInt(HashSet::size).sum());
        assertEquals(sourcesForTxAmount + 1, sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).size());
        assertNotNull(clusterService.getCopyTrustChainConfirmationCluster().get(transactionData.getHash()));
    }

    @Test
    void addTransactionToTrustChainConfirmationCluster_verifyAddition() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        clusterService.attachToCluster(transactionData);
        ConcurrentHashMap<Hash, TransactionData> copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();

        assertNotNull(copyTrustChainConfirmationCluster.get(transactionData.getHash()));

        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);
        long index = 7;
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), index, "7".getBytes());
        when(transactionIndexes.getByHash(any(Hash.class))).thenReturn(transactionIndexData);

        clusterService.attachToCluster(transactionData);
        copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        assertNotNull(copyTrustChainConfirmationCluster.get(transactionData.getHash()));

        TransactionData secondTransactionData = TransactionTestUtils.createRandomTransaction();
        clusterService.attachToCluster(secondTransactionData);
        copyTrustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        assertNotNull(copyTrustChainConfirmationCluster.get(secondTransactionData.getHash()));
    }

    @Test
    void removeNoneIndexedTransaction_verifyRemoval() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Set<Hash> noneIndexedTransactionHashes = new HashSet<>(Collections.singleton(transactionData.getHash()));
        ReflectionTestUtils.setField(transactionHelper, "noneIndexedTransactionHashes", noneIndexedTransactionHashes);
        transactionHelper.removeNoneIndexedTransaction(transactionData);
        assertEquals(Sets.newConcurrentHashSet(), noneIndexedTransactionHashes);
    }
}