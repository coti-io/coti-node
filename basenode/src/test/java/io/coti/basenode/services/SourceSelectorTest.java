package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.BaseNodeTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ContextConfiguration(classes = SourceSelector.class)

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class SourceSelectorTest {

    @Autowired
    private SourceSelector sourceSelector;

    @Value("${max.neighbourhood.radius}")
    private int maxNeighbourhoodRadius;

    @Test
    public void selectTwoOptimalSources_selectFromDifferentSelectionSizes_noException() throws InterruptedException {
        List<TransactionData> transactionDataInputList = new ArrayList<>();
        List<TransactionData> transactionDataList = null;

        TransactionData transactionData1 = BaseNodeTestUtils.generateRandomTxData();
        transactionData1.setAttachmentTime(Instant.now());
        transactionDataInputList.add(transactionData1);
        transactionDataList = sourceSelector.selectTwoOptimalSources(transactionDataInputList);
        Assert.assertTrue(transactionDataList.size()==1);
        Assert.assertTrue( transactionDataList.containsAll(transactionDataInputList) );

        Thread.sleep(2000); // To simulate real conditions
        TransactionData transactionData2 = BaseNodeTestUtils.generateRandomTxData();
        transactionData2.setAttachmentTime(Instant.now());
        transactionDataInputList.add(transactionData2);

        transactionDataList = sourceSelector.selectTwoOptimalSources(transactionDataInputList);
        Assert.assertTrue(transactionDataList.size()==2);
        Assert.assertTrue( transactionDataList.containsAll(transactionDataInputList) );


        Thread.sleep(2000);
        TransactionData transactionData3 = BaseNodeTestUtils.generateRandomTxData();
        transactionData3.setAttachmentTime(Instant.now());
        transactionDataInputList.add(transactionData3);

        transactionDataList = sourceSelector.selectTwoOptimalSources(transactionDataInputList);
        Assert.assertTrue(transactionDataList.size()==2);
    }

    @Test
    public void selectSourcesForAttachment_noExceptions() {
        List<Set<TransactionData>> trustScoreToTransactionMapping = new ArrayList<>();
        for(int i = 0; i<=100 ; i++)
        {
            Set<TransactionData> transactionDataSetI = new HashSet<>();
            TransactionData transactionDataI = BaseNodeTestUtils.generateRandomTxData();
            transactionDataI.setAttachmentTime(Instant.now());
            transactionDataI.setTrustChainTrustScore(BaseNodeTestUtils.generateRandomTrustScore());
            transactionDataSetI.add(transactionDataI);
            trustScoreToTransactionMapping.add(transactionDataSetI);
        }
        double transactionTrustScore = BaseNodeTestUtils.generateRandomTrustScore();

        List<TransactionData> transactionDataList = sourceSelector.selectSourcesForAttachment(trustScoreToTransactionMapping, transactionTrustScore);
        Assert.assertTrue(transactionDataList.size()==2);
    }


}
