import io.coti.common.database.RocksDBConnector;
import io.coti.common.model.Transactions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = RocksDBConnector.class
)
public class RocksDBConnectorTests {
    @Autowired
    private RocksDBConnector rocksDBConnector;

    @Test
    public void TestInsertAndGet_AssertEquals() {
        rocksDBConnector.put(Transactions.class.getName(), "ABCDEF".getBytes(), "Object1".getBytes());
        byte[] returnedObject = rocksDBConnector.getByKey(Transactions.class.getName(), "ABCDEF".getBytes());
        Assert.assertTrue(Arrays.equals(returnedObject, "Object1".getBytes()));
    }
}
