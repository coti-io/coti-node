package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionIndexData;
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
    public void getTrustChainConfirmedTransactions_preTrustScoreConsensus() {

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
    void init_preTrustScoreConsensus() {
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
