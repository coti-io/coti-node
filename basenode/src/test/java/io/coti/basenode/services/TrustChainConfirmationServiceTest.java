package io.coti.basenode.services;

import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
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
@ExtendWith(SpringExtension.class)
@Slf4j

public class TrustChainConfirmationServiceTest {

    @Autowired
    private TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    private ClusterHelper clusterHelper;
    @Autowired
    private BaseNodeTransactionHelper transactionHelper;
    @MockBean
    private ITransactionPropagationCheckService transactionPropagationCheckService;
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

        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());

        ConcurrentMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();
        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> trustChainConfirmedTransactions = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        Assertions.assertEquals(transactionData.getRoundedSenderTrustScore(), (int) Math.round(trustChainConfirmationCluster.get(transactionData.getHash()).getTrustChainTrustScore()));
        Assertions.assertTrue(trustChainConfirmedTransactions.isEmpty());
    }

    @Test
    public void init_preTrustScoreConsensus() {
        int majorityTCCThreshold = MAX_TRUST_SCORE / 2 + 5;
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
}
