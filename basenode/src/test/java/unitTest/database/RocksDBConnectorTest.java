package unitTest.database;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.TransactionVotes;
import io.coti.basenode.model.Transactions;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;


@ContextConfiguration(classes = {Transactions.class,
        Addresses.class,
        RocksDBConnector.class,
        TransactionIndexes.class,
        TransactionVotes.class,
        Transactions.class
})
@RunWith(SpringRunner.class)

@TestPropertySource(locations = "../../test.properties")
@Slf4j
public class RocksDBConnectorTest {

    public static final byte[] key = "ABCDEF".getBytes();
    public static final byte[] value = "Object1".getBytes();

    @Autowired
    private RocksDBConnector rocksDBConnector;

    @Before
    public void init() {
        rocksDBConnector.init();
    }

    @Test
    // Testing all functionality in one test, because otherwise the DB is locked
    public void TestInsertAndGet_AssertEquals() {
        rocksDBConnector.put(Transactions.class.getName(), key, value);
        byte[] returnedObject = rocksDBConnector.getByKey(Transactions.class.getName(), key);
        RocksIterator rocksIterator = rocksDBConnector.getIterator(Transactions.class.getName());
        Assert.assertTrue(Arrays.equals(returnedObject, value) && !rocksDBConnector.isEmpty(Transactions.class.getName()));
    }

    @After
    public void tearDown() {
        rocksDBConnector.shutdown();
    }

}
