import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TransactionData.class)
@Slf4j
public class TransactionDataTest {

    @Test
    public void isSource_NullChildrenList_True() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), new Hash("11"), "test", 83, new Date());
        Assert.assertTrue(transactionData.isSource());
    }

    //
    @Test
    public void isSource_EmptyChildrenList_True() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 83, new Date());
        transactionData.setChildrenTransactions(new LinkedList<>());
        Assert.assertTrue(transactionData.isSource());
    }

    @Test
    public void isSource_NonEmptyChildrenList_False() {
        TransactionData transactionData = new TransactionData(new ArrayList<>(), new Hash("33"), "test", 83, new Date());
        transactionData.setChildrenTransactions(
                Collections.singletonList(new Hash("TransactionData 1".getBytes())));
        Assert.assertFalse(transactionData.isSource());
    }

//    @Test
//    public void confirmationDataPlusTransactionDataPUTtest() {
//        List<BaseTransactionData> baseTransactionsList = new LinkedList<>();
//        Hash address1 = new Hash("AAAA");
//        BaseTransactionData baseTransactionData1 = new BaseTransactionData(address1, new BigDecimal(5.5));
//        Hash address2 = new Hash("BBBB");
//
//        BaseTransactionData baseTransactionData2 = new BaseTransactionData(address2, new BigDecimal(6.6));
//        baseTransactionsList.add(baseTransactionData1);
//        baseTransactionsList.add(baseTransactionData2);
//
//        Hash transactionHash = new Hash("ABCD");
//        TransactionData transactionData = new TransactionData(transactionHash, baseTransactionsList);
//
//        transactionData.setDspConsensus(false);
//        transactionData.setTrustChainConsensus(false);
//        transactions.put(transactionData);
//
//
//        Map<Hash, BigDecimal> addressToAmountMap = new ConcurrentHashMap<>();
//
//        addressToAmountMap.put(address1, new BigDecimal(100));
//        addressToAmountMap.put(address2, new BigDecimal(200));
//
//        ConfirmationData confirmationData = new ConfirmationData();
//        confirmationData.setTransactionData(transactionData);
//        confirmationData.setHash(new Hash("ABCDEA"));
//        confirmationData.setAddressHashToValueTransferredMapping(addressToAmountMap);
//        confirmationData.setDoubleSpendPreventionConsensus(true);
//        confirmationData.setTrustChainConsensus(true);
//        confirmedTransactions.put(confirmationData);
//
//        transactionData = transactions.getByHash(transactionHash);
//
//
//        RocksIterator transactionsDBiterator = transactions.getIterator();
//        transactionsDBiterator.seekToFirst();
//        while (transactionsDBiterator.isValid()) {
//            TransactionData transaction = (TransactionData) SerializationUtils
//                    .deserialize(transactionsDBiterator.value());
//            log.info("Transaction {} is in TransactionsDB", transaction.getHash());
//            transactionsDBiterator.next();
//        }
//
//
//        Assert.assertTrue(transactionData.isTrustChainConsensus());
//        Assert.assertTrue(transactionData.isDspConsensus());
//        baseTransactionsList = transactionData.getBaseTransactions();
//        BigDecimal firstBaseTransactionNewAmount = baseTransactionsList.get(0).getAmount();
//        BigDecimal secondBaseTransactionNewAmount = baseTransactionsList.get(1).getAmount();
//
//        Assert.assertTrue(firstBaseTransactionNewAmount.compareTo(new BigDecimal(100)) == 0);
//        Assert.assertTrue(secondBaseTransactionNewAmount.compareTo(new BigDecimal(200)) == 0);
//
//    }
}
