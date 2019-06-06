package io.coti.basenode.services;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.BaseNodeTestUtils;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.BaseNodeTestUtils.generateRandomHash;

@ContextConfiguration(classes = ClusterService.class)

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class ClusterServiceTest {

    private static final Hash TRANSACTION_ONE_HASH = generateRandomHash();
    private static final double TRANSACTION_ONE_TRUSTSCORE = 70;
    private static final Hash TRANSACTION_TWO_HASH = generateRandomHash();
    private static final double TRANSACTION_TWO_TRUSTSCORE = 83;
    private static final Hash TRANSACTION_THREE_HASH = generateRandomHash();
    private static final double TRANSACTION_THREE_TRUSTSCORE = 92;
    private static final String TRANSACTION_DESCRIPTION = "test";

    @Autowired
    private ClusterService cluster;
    @MockBean
    private Transactions transactions;
    @MockBean
    private ISourceSelector sourceSelector;
    @MockBean
    private TrustChainConfirmationService trustChainConfirmationCluster;
    @MockBean
    private LiveViewService liveViewService;
    @MockBean
    private IConfirmationService confirmationService;
    private TransactionData transactionData1, transactionData2, transactionData3;

    @Before
    public void setUpTransactions() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        transactionData1 = new TransactionData(new ArrayList<>(), TRANSACTION_ONE_HASH, TRANSACTION_DESCRIPTION, TRANSACTION_ONE_TRUSTSCORE, Instant.now(), TransactionType.Payment);
        List<Hash> hashChildren = new Vector<>();
        transactionData1.setChildrenTransactionHashes(hashChildren);
        transactionData1.setCreateTime(Instant.now());
        transactionData1.setAttachmentTime(Instant.now());

        transactionData2 = new TransactionData(new ArrayList<>(), TRANSACTION_TWO_HASH, TRANSACTION_DESCRIPTION, TRANSACTION_TWO_TRUSTSCORE, Instant.now(), TransactionType.Payment);
        transactionData2.setRightParentHash(transactionData1.getHash());
        transactionData2.setCreateTime(Instant.now());
        transactionData2.setAttachmentTime(Instant.now());

        transactions.put(transactionData1);
        transactions.put(transactionData2);

        cluster.addTransactionOnInit(transactionData1);
        when(transactions.getByHash(TRANSACTION_ONE_HASH)).thenReturn(transactionData1);
        cluster.addTransactionOnInit(transactionData2);

        cluster.finalizeInit();
    }

    @Test
    public void finalizeInit_noExceptionIsThrown() {
        try {
            cluster.finalizeInit();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void checkForTrustChainConfirmedTransaction_noExceptionIsThrown() {
        try {
            cluster.checkForTrustChainConfirmedTransaction();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void attachToCluster_noExceptionIsThrown() {
        try {
            transactionData3 = new TransactionData(new ArrayList<>(), TRANSACTION_THREE_HASH, TRANSACTION_DESCRIPTION, TRANSACTION_THREE_TRUSTSCORE, Instant.now(), TransactionType.Payment);
            transactionData3.setLeftParentHash(TRANSACTION_ONE_HASH);
            when(transactions.getByHash(TRANSACTION_ONE_HASH)).thenReturn(transactionData1);
            cluster.attachToCluster(transactionData3);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void selectSources_onlyOneSourceAvailable_transactionDataAttachedAsLeftParent() {
        TransactionData TransactionData = new TransactionData(new ArrayList<>(), TRANSACTION_THREE_HASH, TRANSACTION_DESCRIPTION, TRANSACTION_THREE_TRUSTSCORE, Instant.now(), TransactionType.Payment);
        when(sourceSelector.selectSourcesForAttachment(any(List.class),
                any(double.class)))
                .thenReturn(new Vector<>(Collections.singletonList(transactionData2)));
        cluster.selectSources(TransactionData);
        Assert.assertEquals(TransactionData.getLeftParentHash(), TRANSACTION_TWO_HASH);
    }

    @Test
    public void selectSources_twoSourcesAvailable_twoSourcesAsParents() {
        TransactionData TransactionData = new TransactionData(new ArrayList<>(), TRANSACTION_THREE_HASH, TRANSACTION_DESCRIPTION, TRANSACTION_THREE_TRUSTSCORE, Instant.now(), TransactionType.Payment);
        when(sourceSelector.selectSourcesForAttachment(any(List.class),
                any(double.class)))
                .thenReturn(new Vector<>(Arrays.asList(transactionData1, transactionData2)));
        cluster.selectSources(TransactionData);
        Assert.assertTrue(TransactionData.getLeftParentHash().equals(TRANSACTION_ONE_HASH) &&
                TransactionData.getRightParentHash().equals(TRANSACTION_TWO_HASH));
    }


}
