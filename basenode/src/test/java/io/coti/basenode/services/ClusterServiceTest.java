package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static org.mockito.Mockito.when;


@ContextConfiguration(classes = {ClusterService.class, Transactions.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class ClusterServiceTest {

    @Autowired
    private ClusterService clusterServiceLocal;
    @Autowired
    private Transactions transactionsLocal;
    @MockBean
    private IDatabaseConnector databaseConnectorLocal;
    @MockBean
    INetworkService networkService;

    @BeforeEach
    public void init() {
        databaseConnector = databaseConnectorLocal;
        clusterService = clusterServiceLocal;
        transactions = transactionsLocal;
        clusterService.init();
        transactions.init();
    }

    @Test
    void detachFromCluster() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        TransactionData leftParent = TransactionTestUtils.createRandomTransaction();
        TransactionData leftParentWithChild = SerializationUtils.deserialize(SerializationUtils.serialize(leftParent));
        leftParentWithChild.addToChildrenTransactions(transactionData.getHash());
        TransactionData rightParent = TransactionTestUtils.createRandomTransaction();
        TransactionData rightParentWithChild = SerializationUtils.deserialize(SerializationUtils.serialize(rightParent));
        rightParentWithChild.addToChildrenTransactions(transactionData.getHash());
        transactionData.setLeftParentHash(leftParent.getHash());
        transactionData.setRightParentHash(rightParent.getHash());
        when(databaseConnector.getByKey("io.coti.basenode.model.Transactions", leftParent.getHash().getBytes()))
                .thenReturn(SerializationUtils.serialize(leftParentWithChild)).thenReturn(SerializationUtils.serialize(leftParent));
        when(databaseConnector.getByKey("io.coti.basenode.model.Transactions", rightParent.getHash().getBytes()))
                .thenReturn(SerializationUtils.serialize(rightParentWithChild)).thenReturn(SerializationUtils.serialize(rightParent));
        ArrayList<HashSet<Hash>> sourceSetsByTrustScore = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            sourceSetsByTrustScore.add(Sets.newHashSet());
        }
        sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).add(transactionData.getHash());
        HashMap<Hash, TransactionData> sourceMap = new HashMap<>();
        sourceMap.put(transactionData.getHash(), transactionData);
        ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>();
        trustChainConfirmationCluster.put(leftParent.getHash(), leftParent);
        trustChainConfirmationCluster.put(rightParent.getHash(), rightParent);
        AtomicLong totalSources = new AtomicLong(1);
        ReflectionTestUtils.setField(clusterService, "trustChainConfirmationCluster", trustChainConfirmationCluster);
        ReflectionTestUtils.setField(clusterService, "sourceMap", sourceMap);
        ReflectionTestUtils.setField(clusterService, "sourceSetsByTrustScore", sourceSetsByTrustScore);
        ReflectionTestUtils.setField(clusterService, "totalSources", totalSources);
        clusterService.detachFromCluster(transactionData);
        Assertions.assertEquals(new AtomicLong(2).get(), clusterService.getTotalSources());
        Assertions.assertEquals(2, clusterService.getSourceSetsByTrustScore().stream().filter(source -> !source.isEmpty()).count());
    }
}