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
    public void isSignatureExists() {
        BaseTransactionData baseTransactionData =
                TestUtils.createBaseTransactionDataWithSpecificHash(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"));
        Assert.assertTrue(baseTransactionData.isSignatureExists());
    }

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