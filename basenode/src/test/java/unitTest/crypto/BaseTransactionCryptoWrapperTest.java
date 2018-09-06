package unitTest.crypto;

import io.coti.basenode.crypto.BaseTransactionCryptoWrapper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import static org.junit.Assert.assertNull;

@TestPropertySource(locations = "../../test.properties")
@SpringBootTest(classes = {BaseTransactionData.class, BaseTransactionCryptoWrapper.class})
@RunWith(SpringRunner.class)
@Slf4j
public class BaseTransactionCryptoWrapperTest {

    private BaseTransactionCryptoWrapper baseTransactionCryptoWrapper;
    private BaseTransactionData baseTransactionDataWithBadHash;
    private TransactionData validTransactionData;
    private TransactionData notValidTransactionData;

    @Before
    public void init() {
        baseTransactionDataWithBadHash =
                TestUtils.createBaseTransactionDataWithSpecificHash(new Hash
                        ("bb"));

        validTransactionData =
                TestUtils.createTransactionFromJson("{\"baseTransactions\":[{\"addressHash\":\"138ae277d61cf5dd2d52dc9f71b0f71e66ff80859430c3cb353652b0c264b2e2750257cb713f7eacb1209ca15a0cd40f688aa5da8526ecb4ea688ea9753da501ef18975c\",\"amount\":\"-2.592552663684784\",\"hash\":\"6a547e15ffbd46d7153b47fe3d89254ac28cb8b43a73828168964dbb5ecaf3ac\",\"createTime\":1535374639017,\"signatureData\":{\"r\":\"2fd7e73adf4ea69716b4bbf49bdd2bec728fe5810cc7f349068dfc651b1b847\",\"s\":\"577c7f1d98de8271bec2c34924b6c74378a1175c1676c4447ac525b58b8ab923\"}},{\"addressHash\":\"4831b1320fe931dba6c8ff7648f861f3e6cf0fb38a3499e6c4516d551e0232e471bfd6d803f2d0432adf4a1948995780497a6c467ffb2779cd1ccab52f2d9904c6603128\",\"amount\":\"-2.865889886713445\",\"hash\":\"6318bbacd1c5cffe794803f36ba230e028aff2d817f28062696ef0be57b54a00\",\"createTime\":1535374639018,\"signatureData\":{\"r\":\"7004da0ec3d9faaedaa9494b1537c349165a0a9171bdff4039652cd9ee90cb13\",\"s\":\"6cf920b1a6204e2a7423fd08d0fc67d1a703b686ace09879a1f064031eca6a95\"}},{\"addressHash\":\"2d1e71ba021550f13d7bd78a043fc16a4569e5a1b844fec6b96f9a98feeada1569f8e86f1157d44a654e9e3228aba51a5716ce5976b25543d9470b3fd2351c8c2825de9c\",\"amount\":\"-7.5797959209527095\",\"hash\":\"662d773e3ca8fde65c6a01f638a8f6cd32e6c10d5a9b0a274340fc0a3cbbcc37\",\"createTime\":1535374639018,\"signatureData\":{\"r\":\"3b5b47634792de9889f0f3cb9ce27d51d2dc1f9a7e6f372537212543892efb80\",\"s\":\"6fcffedd2125845541353bf4788eb25ba00dd9618e81183902eab2a42e335545\"}},{\"addressHash\":\"b281116421c606713e01ec812eee5b580e27a7313e97b8c5a1cc72ca43e28eb374b38e12eca33488c298ccbcdd976c352d1b53b009352a3a100911757495f6a55e7bcd0e\",\"amount\":\"13.0382384713509385\",\"hash\":\"b5e182a5d175cde38a3b10f46d6755d8b7320111f62c19eec2d8a7c1915f9623\",\"createTime\":1535374639018}],\"createTime\":1535374639016,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"transactionHash\":\"56236ae7d17e7f0bf1888d45dfb9cfb7648c15549aef9b79e360050a66388d21\",\"trustScore\":78.63868,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"47b794934c2017c67a5891974d76497c6540d96ca4ece89f859ab3f588019521\",\"s\":\"446d103c236bf3e8430fd76031a9197aa3ea7952bd60c4c486741cb588fe8453\"}}],\"senderHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"hash\":\"56236ae7d17e7f0bf1888d45dfb9cfb7648c15549aef9b79e360050a66388d21\"}");

        notValidTransactionData = TestUtils.createTransactionWithSpecificHash(new Hash("cc"));
    }

    @Test
    public void getMessageInBytes_noExceptionIsThrown() {
        baseTransactionCryptoWrapper =
                new BaseTransactionCryptoWrapper(validTransactionData.getBaseTransactions().get(0));
        try {
            baseTransactionCryptoWrapper.getMessageInBytes();
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void createBaseTransactionHashFromData_withValidTransaction_lengthOfBaseTransactionHashIsCorrect() {
        baseTransactionCryptoWrapper =
                new BaseTransactionCryptoWrapper(validTransactionData.getBaseTransactions().get(0));
        Hash baseTransactionHash = baseTransactionCryptoWrapper.createBaseTransactionHashFromData();
        Assert.assertTrue(baseTransactionHash.toHexString().length() == 64);
    }

    @Test
    public void createBaseTransactionHashFromData_withNotValidTransaction_lengthOfBaseTransactionHashIsCorrect() {
        baseTransactionCryptoWrapper =
                new BaseTransactionCryptoWrapper(notValidTransactionData.getBaseTransactions().get(0));
        Hash baseTransactionHash = baseTransactionCryptoWrapper.createBaseTransactionHashFromData();
        Assert.assertTrue(baseTransactionHash.toHexString().length() == 64);
    }

    @Test
    public void getBaseTransactionHash_withValidTransaction_lengthIsCorrect() {
        baseTransactionCryptoWrapper =
                new BaseTransactionCryptoWrapper(validTransactionData.getBaseTransactions().get(0));
        Hash baseTransactionHashBeforeSetting = baseTransactionCryptoWrapper.getBaseTransactionHash();
        Assert.assertTrue(baseTransactionHashBeforeSetting.toString().length() == 64);
    }

    @Test
    public void getBaseTransactionHash_withShortHash_lengthIsNotCorrect() {
        baseTransactionCryptoWrapper =
                new BaseTransactionCryptoWrapper(notValidTransactionData.getBaseTransactions().get(0));
        Hash baseTransactionHashBeforeSetting = baseTransactionCryptoWrapper.getBaseTransactionHash();
        Assert.assertFalse(baseTransactionHashBeforeSetting.toString().length() == 64);
    }

    @Test
    public void isBaseTransactionValid_whenValid_returnTrue() {
        baseTransactionCryptoWrapper =
                new BaseTransactionCryptoWrapper(validTransactionData.getBaseTransactions().get(0));
        Assert.assertTrue(baseTransactionCryptoWrapper.IsBaseTransactionValid
                (validTransactionData.getHash()));
    }

    @Test
    public void isBaseTransactionValid_whenNotMatchesBaseTransaction_returnsFalse() {
        baseTransactionCryptoWrapper =
                new BaseTransactionCryptoWrapper(validTransactionData.getBaseTransactions().get(0));
        Assert.assertFalse(baseTransactionCryptoWrapper.IsBaseTransactionValid
                (new Hash("c44574cbfb9bcb5b5dc64ed3f7a867b29e3f90197420b48c43ffe28e17f4e6ab")));
    }

    @Test
    public void isBaseTransactionValid_whenNotValidBaseTransactionHash_returnFalse() {
        baseTransactionCryptoWrapper = new BaseTransactionCryptoWrapper(baseTransactionDataWithBadHash);
        Assert.assertFalse(baseTransactionCryptoWrapper.IsBaseTransactionValid
                (baseTransactionCryptoWrapper.getBaseTransactionHash()));
    }
}