package unitTest.crypto;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

@TestPropertySource(locations = "../../test.properties")
@SpringBootTest(classes = BaseTransactionData.class)
@RunWith(SpringRunner.class)
public class BaseTransactionDataTest {

    @Test
    public void equals_whenOtherTransactionHasTheSameHash_returnTrue() {
        BaseTransactionData baseTransactionData1 = TestUtils.createBaseTransactionDataWithSpecificHash(new Hash("ab"));
        BaseTransactionData baseTransactionData2 = TestUtils.createBaseTransactionDataWithSpecificHash(new Hash("ab"));
        Assert.assertTrue(baseTransactionData1.equals(baseTransactionData2));
    }

    @Test
    public void equals_whenOtherTransactionHasDifferentHash_returnFalse() {
        BaseTransactionData baseTransactionData1 = TestUtils.createBaseTransactionDataWithSpecificHash(new Hash("ab"));
        BaseTransactionData baseTransactionData2 = TestUtils.createBaseTransactionDataWithSpecificHash(new Hash("cb"));
        Assert.assertFalse(baseTransactionData1.equals(baseTransactionData2));
    }

    @Test
    public void equals_whenOtherObjectIsNotATransactionData_returnFalse() {
        BaseTransactionData baseTransactionData = TestUtils.createBaseTransactionDataWithSpecificHash(new Hash("ab"));
        Assert.assertFalse(baseTransactionData.equals(new Hash("ab")));
    }
}