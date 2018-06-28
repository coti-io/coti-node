package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.SourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Vector;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
public class SourceSelectorTest {
    @Autowired
    private SourceSelector sourceSelector;
    private Date now;
    private List<TransactionData> newTransactions;

    @Before
    public void init() {
        now = new Date();
        newTransactions = new Vector();
        TransactionData TransactionData2 = new TransactionData(new Hash("22"));
        TransactionData2.setSenderTrustScore(92);
        TransactionData2.setAttachmentTime(new Date(now.getTime() - 2000));
        //now.setTime(now.getTime() + 5000)

        TransactionData TransactionData3 = new TransactionData(new Hash("33"));
        TransactionData3.setSenderTrustScore(84);
        TransactionData3.setAttachmentTime(new Date(now.getTime() - 3000));

        TransactionData TransactionData4 = new TransactionData(new Hash("44"));
        TransactionData4.setSenderTrustScore(86);
        TransactionData4.setAttachmentTime(new Date(now.getTime() - 4000));

        TransactionData TransactionData5 = new TransactionData(new Hash("55"));
        TransactionData5.setSenderTrustScore(76);
        TransactionData5.setAttachmentTime(new Date(now.getTime() - 5000));

        TransactionData TransactionData6 = new TransactionData(new Hash("66"));
        TransactionData6.setSenderTrustScore(60);
        TransactionData6.setAttachmentTime(new Date(now.getTime() - 6000));

        TransactionData TransactionData7 = new TransactionData(new Hash("77"));
        TransactionData7.setSenderTrustScore(86);
        TransactionData7.setAttachmentTime(new Date(now.getTime() - 7000));

        TransactionData TransactionData8 = new TransactionData(new Hash("88"));
        TransactionData8.setSenderTrustScore(80);
        TransactionData8.setAttachmentTime(new Date(now.getTime() - 8000));

        TransactionData TransactionData9 = new TransactionData(new Hash("99"));
        TransactionData9.setSenderTrustScore(72);
        TransactionData9.setAttachmentTime(new Date(now.getTime() - 9000));

        newTransactions.add(TransactionData2);
        newTransactions.add(TransactionData3);
        newTransactions.add(TransactionData4);
        newTransactions.add(TransactionData5);
        newTransactions.add(TransactionData6);
        newTransactions.add(TransactionData7);
        newTransactions.add(TransactionData8);
        newTransactions.add(TransactionData9);
    }

    @Test
    public void selectTwoOptimalSources() {
        List<TransactionData> sources = sourceSelector.selectTwoOptimalSources(newTransactions);
        Assert.assertTrue(sources.size() == 2);
    }

    @Test
    public void selectSourcesForAttachment() {
        Vector<TransactionData>[] trustScoreToSourceListMapping = new Vector[101];
        for (int i = 0; i < trustScoreToSourceListMapping.length; i++) {
            trustScoreToSourceListMapping[i] = (new Vector<>());
        }
        for (TransactionData transaction : newTransactions) {
            trustScoreToSourceListMapping[transaction.getRoundedSenderTrustScore()].add(transaction);
        }
        List<TransactionData> sources0 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 38);
        Assert.assertTrue(sources0.size() == 0);

        List<TransactionData> sources1 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 50);
        Assert.assertTrue(sources1.size() == 1);

        List<TransactionData> sources2 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 92);
        Assert.assertTrue(sources2.size() == 1);

        TransactionData TransactionData10 = new TransactionData(new Hash("10"));
        TransactionData10.setSenderTrustScore(80);
        TransactionData10.setAttachmentTime(new Date(now.getTime() - 8000));

        TransactionData TransactionData11 = new TransactionData(new Hash("1111"));
        TransactionData11.setSenderTrustScore(72);
        TransactionData11.setAttachmentTime(new Date(now.getTime() - 9000));

        newTransactions.add(TransactionData10);
        trustScoreToSourceListMapping[TransactionData10.getRoundedSenderTrustScore()].add(TransactionData10);
        newTransactions.add(TransactionData11);
        trustScoreToSourceListMapping[TransactionData11.getRoundedSenderTrustScore()].add(TransactionData11);

        List<TransactionData> sources3 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 92);
        Assert.assertTrue(sources3.size() == 2);
        log.info("End selectSourcesForAttachment test!!!");
    }

}