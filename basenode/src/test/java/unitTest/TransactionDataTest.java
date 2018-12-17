package unitTest;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TransactionData.class)
@Slf4j
public class TransactionDataTest {

    @Test
    public void isSource_NullChildrenList_returnTrue() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), new Hash("11"), "test", 83, new Date(), TransactionType.Payment);
        Assert.assertTrue(transactionData.isSource());
    }

    //
    @Test
    public void isSource_EmptyChildrenList_returnTrue() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 83, new Date(), TransactionType.Payment);
        transactionData.setChildrenTransactions(new LinkedList<>());
        Assert.assertTrue(transactionData.isSource());
    }

    @Test
    public void isSource_NonEmptyChildrenList_returnFalse() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), new Hash("33"), "test", 83, new Date(), TransactionType.Payment);
        transactionData.setChildrenTransactions(
                Collections.singletonList(new Hash("TransactionData 1".getBytes())));
        Assert.assertFalse(transactionData.isSource());
    }

    @Test
    public void equals_whenOtherTransactionHasTheSameHash_returnTrue() {
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(new Hash("ab"));
        TransactionData transactionData2 = TestUtils.createTransactionWithSpecificHash(new Hash("ab"));
        Assert.assertTrue(transactionData1.equals(transactionData2));
    }

    @Test
    public void equals_whenOtherTransactionHasDifferentHash_returnFalse() {
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(new Hash("ab"));
        TransactionData transactionData2 = TestUtils.createTransactionWithSpecificHash(new Hash("cb"));
        Assert.assertFalse(transactionData1.equals(transactionData2));
    }

    @Test
    public void equals_whenOtherObjectIsNotATransactionData_returnFalse() {
        TransactionData transactionData = TestUtils.createTransactionWithSpecificHash(new Hash("ab"));
        Assert.assertFalse(transactionData.equals(new Hash("ab")));
    }
}
