package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import static testUtils.TestUtils.generateRandomHash;
import static testUtils.TestUtils.generateRandomTrustScore;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest(classes = TransactionData.class)
@RunWith(SpringRunner.class)
@Slf4j
public class TransactionDataTest {
    private static final String TRANSACTION_DESCRIPTION = "test";
    private static final int SIZE_OF_HASH = 64;

    @Before
    public void setUp() {
        log.info("Starting  - " + this.getClass().getSimpleName());
    }


    @Test
    public void testGetRoundedSenderTrustScore() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, 82.666, new Date(), TransactionType.Payment);
        Assert.assertTrue(transactionData.getRoundedSenderTrustScore() == 83);
    }

    @Test
    public void addToChildrenTransactions__noExceptionIsThrown() {
        try {
            TransactionData transactionData = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Payment);
            transactionData.addToChildrenTransactions(generateRandomHash(SIZE_OF_HASH));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void isSource_NullChildrenList_returnTrue() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Payment);
        Assert.assertTrue(transactionData.isSource());
    }


    @Test
    public void isSource_EmptyChildrenList_returnTrue() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Payment);
        transactionData.setChildrenTransactions(new LinkedList<>());
        Assert.assertTrue(transactionData.isSource());
    }

    @Test
    public void isSource_NonEmptyChildrenList_returnFalse() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Payment);
        transactionData.setChildrenTransactions(
                Collections.singletonList(new Hash("TransactionData 1".getBytes())));
        Assert.assertFalse(transactionData.isSource());
    }

    @Test
    public void equals_whenOtherTransactionHasTheSameHash_returnTrue() {
        Hash hash = generateRandomHash(SIZE_OF_HASH);
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(hash);
        TransactionData transactionData2 = TestUtils.createTransactionWithSpecificHash(hash);
        Assert.assertTrue(transactionData1.equals(transactionData2));
    }

    @Test
    public void equals_whenOtherTransactionHasDifferentHash_returnFalse() {
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(generateRandomHash(SIZE_OF_HASH));
        TransactionData transactionData2 = TestUtils.createTransactionWithSpecificHash(generateRandomHash(SIZE_OF_HASH));
        Assert.assertFalse(transactionData1.equals(transactionData2));
    }

    @Test
    public void equals_whenOtherObjectIsNotATransactionData_returnFalse() {
        Hash hash = generateRandomHash(SIZE_OF_HASH);
        TransactionData transactionData = TestUtils.createTransactionWithSpecificHash(hash);
        Assert.assertFalse(transactionData.equals(hash));
    }
}
