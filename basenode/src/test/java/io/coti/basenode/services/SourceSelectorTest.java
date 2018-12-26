package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static testUtils.TestUtils.generateRandomHash;

//@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = SourceSelector.class
)
//@TestPropertySource(locations = "classpath:test.properties")
//@Slf4j
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@Slf4j
public class SourceSelectorTest {
    private static final String TRANSACTION_DESCRIPTION = "test";
    private static final int SECOND_IN_MILLISECOND = 1000;

    @Autowired
    private SourceSelector sourceSelector;

    private Date now;
    private List<TransactionData> newTransactions;

    @Before
    public void setUp() {
        log.info("Starting  - " + this.getClass().getSimpleName());

        now = new Date();
        newTransactions = new Vector();
        double[] trustScores = new double[]{92, 84, 86, 76, 60, 86, 80, 72};
        for (int i = 0; i < 8; i++) {
            TransactionData transactionData = new TransactionData(new ArrayList<>(), generateRandomHash(), TRANSACTION_DESCRIPTION, trustScores[i], new Date(), TransactionType.Payment);
            transactionData.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND * i));
            newTransactions.add(transactionData);
        }
    }

    @Test
    public void selectTwoOptimalSources() {
        List<TransactionData> sources = sourceSelector.selectTwoOptimalSources(newTransactions);
        Assert.assertEquals(2, sources.size());
    }

    @Test
    public void selectSourcesForAttachment() {
        List<List<TransactionData>> trustScoreToSourceListMapping = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            trustScoreToSourceListMapping.add(new ArrayList<>());
        }
        for (TransactionData transaction : newTransactions) {
            trustScoreToSourceListMapping.get(transaction.getRoundedSenderTrustScore()).add(transaction);
        }
        List<TransactionData> sources0 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 38);
        Assert.assertEquals(0, sources0.size());

        List<TransactionData> sources1 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 50);
        Assert.assertEquals(1, sources1.size());

        List<TransactionData> sources2 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 92);
        Assert.assertEquals(1, sources2.size());

        TransactionData transactionData8 = new TransactionData(new ArrayList<>(), generateRandomHash(), TRANSACTION_DESCRIPTION, 100, new Date(), TransactionType.Payment);
        transactionData8.setSenderTrustScore(80);
        transactionData8.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData9 = new TransactionData(new ArrayList<>(), generateRandomHash(), TRANSACTION_DESCRIPTION, 100, new Date(), TransactionType.Payment);
        transactionData9.setSenderTrustScore(72);
        transactionData9.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        newTransactions.add(transactionData8);
        trustScoreToSourceListMapping.get(transactionData8.getRoundedSenderTrustScore()).add(transactionData8);
        newTransactions.add(transactionData9);
        trustScoreToSourceListMapping.get(transactionData9.getRoundedSenderTrustScore()).add(transactionData9);

        List<TransactionData> sources3 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 92);
        Assert.assertEquals(2, sources3.size());
    }

}