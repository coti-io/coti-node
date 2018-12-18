package unitTest;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.SourceSelector;
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

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = SourceSelector.class
)
@TestPropertySource(locations = "../test.properties")
public class SourceSelectorTest {
    public static final String TRANSACTION_DESCRIPTION = "test";
    public static final int SECOND_IN_MILLISECOND = 1000;
    public static final int SIZE_OF_HASH = 64;

    @Autowired
    private SourceSelector sourceSelector;
    private Date now;
    private List<TransactionData> newTransactions;

    @Before
    public void init() {
        now = new Date();
        newTransactions = new Vector();
        TransactionData transactionData0 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 92, new Date(), TransactionType.Payment);
        transactionData0.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData1 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 84, new Date(), TransactionType.Payment);
        transactionData1.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData2 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 86, new Date(), TransactionType.Payment);
        transactionData2.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData3 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 76, new Date(), TransactionType.Payment);
        transactionData3.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData4 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 60, new Date(), TransactionType.Payment);
        transactionData4.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData5 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 86, new Date(), TransactionType.Payment);
        transactionData5.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData6 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 80, new Date(), TransactionType.Payment);
        transactionData6.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData7 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 72, new Date(), TransactionType.Payment);
        transactionData7.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        newTransactions.add(transactionData0);
        newTransactions.add(transactionData2);
        newTransactions.add(transactionData2);
        newTransactions.add(transactionData3);
        newTransactions.add(transactionData4);
        newTransactions.add(transactionData5);
        newTransactions.add(transactionData6);
        newTransactions.add(transactionData7);
    }

    @Test
    public void selectTwoOptimalSources() {
        List<TransactionData> sources = sourceSelector.selectTwoOptimalSources(newTransactions);
        Assert.assertTrue(sources.size() == 2);
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
        Assert.assertTrue(sources0.size() == 0);

        List<TransactionData> sources1 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 50);
        Assert.assertTrue(sources1.size() == 1);

        List<TransactionData> sources2 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 92);
        Assert.assertTrue(sources2.size() == 1);

        TransactionData transactionData8 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 100, new Date(), TransactionType.Payment);
        transactionData8.setSenderTrustScore(80);
        transactionData8.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        TransactionData transactionData9 = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 100, new Date(), TransactionType.Payment);
        transactionData9.setSenderTrustScore(72);
        transactionData9.setAttachmentTime(new Date(now.getTime() - SECOND_IN_MILLISECOND));

        newTransactions.add(transactionData8);
        trustScoreToSourceListMapping.get(transactionData8.getRoundedSenderTrustScore()).add(transactionData8);
        newTransactions.add(transactionData9);
        trustScoreToSourceListMapping.get(transactionData9.getRoundedSenderTrustScore()).add(transactionData9);

        List<TransactionData> sources3 = sourceSelector.selectSourcesForAttachment(trustScoreToSourceListMapping, 92);
        Assert.assertTrue(sources3.size() == 2);
    }

}