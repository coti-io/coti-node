package unitTest;

import io.coti.common.database.RocksDBConnector;
import io.coti.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;


@ContextConfiguration(classes = {Transactions.class,
        Addresses.class,
        RocksDBConnector.class,
        AddressesTransactionsHistory.class,
        TrustScores.class,
        TransactionIndexes.class,
        TransactionVotes.class
})
@RunWith(SpringRunner.class)

@TestPropertySource(locations = "../test.properties")
@Slf4j
public class RocksDBConnectorTest {
    @Autowired
    private RocksDBConnector rocksDBConnector;

    @Test
    public void TestInsertAndGet_AssertEquals() {
        rocksDBConnector.put(Transactions.class.getName(), "ABCDEF".getBytes(), "Object1".getBytes());
        byte[] returnedObject = rocksDBConnector.getByKey(Transactions.class.getName(), "ABCDEF".getBytes());
        Assert.assertTrue(Arrays.equals(returnedObject, "Object1".getBytes()));
    }
}
