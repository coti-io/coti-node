package unitTest;

import io.coti.common.crypto.BaseTransactionCryptoWrapper;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import testUtils.TestUtils;

public class BaseTransactionCryptoWrapperTest {

private BaseTransactionCryptoWrapper baseTransactionCryptoWrapper1;
private BaseTransactionCryptoWrapper baseTransactionCryptoWrapper2;

    @Before
    public void init() {
        BaseTransactionData baseTransactionData1 =
                TestUtils.createBaseTransactionDataWithSpecificHash(new Hash
                        ("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"));
        baseTransactionCryptoWrapper1 = new BaseTransactionCryptoWrapper(baseTransactionData1);

        BaseTransactionData baseTransactionData2 =
                TestUtils.createBaseTransactionDataWithSpecificHash(new Hash
                        ("bb"));
        baseTransactionCryptoWrapper2 = new BaseTransactionCryptoWrapper(baseTransactionData2);
    }

    @Test
    public void getMessageInBytes_withLegalHash_lengthOfMessageInBytesIs73() {
        byte[] messageInBytes =  baseTransactionCryptoWrapper1.getMessageInBytes();
        Assert.assertTrue(messageInBytes.length == 73);

    }
    @Test
    public void getMessageInBytes_withNotLegalHash_lengthOfMessageInBytesIsNot73() {
        byte[] messageInBytes =  baseTransactionCryptoWrapper2.getMessageInBytes();
        Assert.assertFalse(messageInBytes.length == 73);
    }

    @Test
    public void createBaseTransactionHashFromData_withLegalHash_lengthOfBaseTransactionHashIs64() {
        Hash baseTransactionHash = baseTransactionCryptoWrapper1.createBaseTransactionHashFromData();
        Assert.assertTrue(baseTransactionHash.toHexString().length() == 64);
    }

    @Test
    public void createBaseTransactionHashFromData_withNotLegalHash_lengthOfBaseTransactionHashIs64() {
        Hash baseTransactionHash = baseTransactionCryptoWrapper2.createBaseTransactionHashFromData();
        Assert.assertTrue(baseTransactionHash.toHexString().length() == 64);
    }

    @Test
    public void setBaseTransactionHash_withLegalHash() {
        Hash baseTransactionHashBeforeSetting = baseTransactionCryptoWrapper1.getBaseTransactionHash();
        baseTransactionCryptoWrapper1.setBaseTransactionHash();
        Hash baseTransactionHashAfterSetting = baseTransactionCryptoWrapper1.getBaseTransactionHash();
        Assert.assertTrue(baseTransactionHashBeforeSetting.toString().length() == 136 &&
                baseTransactionHashAfterSetting.toString().length() == 64);
    }
    @Test
    public void setBaseTransactionHash_withNotLegalHash() {
        Hash baseTransactionHashBeforeSetting = baseTransactionCryptoWrapper2.getBaseTransactionHash();
        baseTransactionCryptoWrapper1.setBaseTransactionHash();
        Hash baseTransactionHashAfterSetting = baseTransactionCryptoWrapper2.getBaseTransactionHash();
        Assert.assertFalse(baseTransactionHashBeforeSetting.toString().length() == 136 &&
                baseTransactionHashAfterSetting.toString().length() == 64);
    }

    @Test
    public void isBaseTransactionValid_whenValid_returnTrue() {
        Assert.assertTrue(baseTransactionCryptoWrapper1.IsBaseTransactionValid
                (baseTransactionCryptoWrapper1.getBaseTransactionHash()));
    }

    @Test
    public void isBaseTransactionValid_whenNotValid_returnFalse() {
        Assert.assertFalse(baseTransactionCryptoWrapper2.IsBaseTransactionValid
                (baseTransactionCryptoWrapper2.getBaseTransactionHash()));
    }
}