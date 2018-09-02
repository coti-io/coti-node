package unitTest.crypto;

import io.coti.basenode.crypto.TransactionCryptoWrapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

@TestPropertySource(locations = "../../test.properties")
@SpringBootTest(classes = {TransactionData.class})
@RunWith(SpringRunner.class)
public class TransactionCryptoWrapperTest {

    private TransactionCryptoWrapper validTransactionCryptoWrapper;
    private TransactionCryptoWrapper notValidTransactionCryptoWrapper;
    private TransactionData validTransactionData;
    private TransactionData notValidTransactionData;

    @Before
    public void init() {
        validTransactionData =
                TestUtils.createTransactionFromJson("{\"baseTransactions\":[{\"addressHash\":\"138ae277d61cf5dd2d52dc9f71b0f71e66ff80859430c3cb353652b0c264b2e2750257cb713f7eacb1209ca15a0cd40f688aa5da8526ecb4ea688ea9753da501ef18975c\",\"amount\":\"-2.592552663684784\",\"hash\":\"6a547e15ffbd46d7153b47fe3d89254ac28cb8b43a73828168964dbb5ecaf3ac\",\"createTime\":1535374639017,\"signatureData\":{\"r\":\"2fd7e73adf4ea69716b4bbf49bdd2bec728fe5810cc7f349068dfc651b1b847\",\"s\":\"577c7f1d98de8271bec2c34924b6c74378a1175c1676c4447ac525b58b8ab923\"}},{\"addressHash\":\"4831b1320fe931dba6c8ff7648f861f3e6cf0fb38a3499e6c4516d551e0232e471bfd6d803f2d0432adf4a1948995780497a6c467ffb2779cd1ccab52f2d9904c6603128\",\"amount\":\"-2.865889886713445\",\"hash\":\"6318bbacd1c5cffe794803f36ba230e028aff2d817f28062696ef0be57b54a00\",\"createTime\":1535374639018,\"signatureData\":{\"r\":\"7004da0ec3d9faaedaa9494b1537c349165a0a9171bdff4039652cd9ee90cb13\",\"s\":\"6cf920b1a6204e2a7423fd08d0fc67d1a703b686ace09879a1f064031eca6a95\"}},{\"addressHash\":\"2d1e71ba021550f13d7bd78a043fc16a4569e5a1b844fec6b96f9a98feeada1569f8e86f1157d44a654e9e3228aba51a5716ce5976b25543d9470b3fd2351c8c2825de9c\",\"amount\":\"-7.5797959209527095\",\"hash\":\"662d773e3ca8fde65c6a01f638a8f6cd32e6c10d5a9b0a274340fc0a3cbbcc37\",\"createTime\":1535374639018,\"signatureData\":{\"r\":\"3b5b47634792de9889f0f3cb9ce27d51d2dc1f9a7e6f372537212543892efb80\",\"s\":\"6fcffedd2125845541353bf4788eb25ba00dd9618e81183902eab2a42e335545\"}},{\"addressHash\":\"b281116421c606713e01ec812eee5b580e27a7313e97b8c5a1cc72ca43e28eb374b38e12eca33488c298ccbcdd976c352d1b53b009352a3a100911757495f6a55e7bcd0e\",\"amount\":\"13.0382384713509385\",\"hash\":\"b5e182a5d175cde38a3b10f46d6755d8b7320111f62c19eec2d8a7c1915f9623\",\"createTime\":1535374639018}],\"createTime\":1535374639016,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"transactionHash\":\"56236ae7d17e7f0bf1888d45dfb9cfb7648c15549aef9b79e360050a66388d21\",\"trustScore\":78.63868,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"47b794934c2017c67a5891974d76497c6540d96ca4ece89f859ab3f588019521\",\"s\":\"446d103c236bf3e8430fd76031a9197aa3ea7952bd60c4c486741cb588fe8453\"}}],\"senderHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"hash\":\"56236ae7d17e7f0bf1888d45dfb9cfb7648c15549aef9b79e360050a66388d21\"}");

        notValidTransactionData =
                TestUtils.createTransactionFromJson("{\"baseTransactions\":[{\"addressHash\":\"7b979f3ec14f082c1c1777a34035765dd3eae5d9ab7f8b1ed3d733de46f6a63f3d92a40f71071d9418073b5aa50d6af662f79e092b03253a4650c05e4dc90cded411014d\",\"amount\":\"-2.00163718107925\",\"hash\":\"bf48818f91831083de6d6df91238df1740964b4de5f7af25c2b89c858cbb7faa\",\"createTime\":1535374641237,\"signatureData\":{\"r\":\"dc066359c7254c35491f59faae21fbeb5706ce004258ae41c610aac734afbcb2\",\"s\":\"6188e293d4c399b989050faea0535ba23c4b4e8de8fe1a789154e34c5ed631d9\"}},{\"addressHash\":\"dd6341294a73960e7ad0f040c43b587c0d6a313e271f4ebb405995a790c23d4172c015949e35f302fc68ba3e942db241797b0520d3ba8b182e7d2e2642bc32b7a816c0fb\",\"amount\":\"-6.883689048854351\",\"hash\":\"a8472d0ff42b1c5bfcfb6c6e72017cf69c85f5600d6d32ba8bb953a6e444d757\",\"createTime\":1535374641238,\"signatureData\":{\"r\":\"e2c57f2d04dcb3b3ede1054157eca96aa9dd55f91defdd9b89594e8a534adc6d\",\"s\":\"ae5ab921be77fecf3a60555cc609e9e71089508886c7775d23214d12ab7ee2c\"}},{\"addressHash\":\"49fe9a2c48e53ef93465fdecb1722d67b9123358b993bd9b7f6a40f9299aae466f206d7989a61f6b4175b7715e5f91b027fe7ad117c6188f072661f2284003f194ce9652\",\"amount\":\"-9.89487580774032\",\"hash\":\"47ec253276a97e3b46449e3bc90cb7a40952cb28325272e2b40fb10b2a3f4c45\",\"createTime\":1535374641239,\"signatureData\":{\"r\":\"cf20aad319a847d36b264e641c28999dff81fd4b37a6c4efa48dfaba20ce61dd\",\"s\":\"a9add756742a7714d01bbe6d7c7466226ef1038e8ee4ad9ce50d95bdf5c6d3ff\"}},{\"addressHash\":\"f391d2f14515434f9b6842d135fbf583ed4e0b446cf1d33582aa2be95b0ce6dcc3e1c30892e5a29fa60bd6342d5120b9e0c41c7fef4c16a214c279cd5fe8458e0720dab3\",\"amount\":\"18.780202037673921\",\"hash\":\"a5d8b71c5e1daff09f997776ba48806f928bf3e7f76a8aabfec0e9102b2adf4b\",\"createTime\":1535374641239}],\"createTime\":1535374641237,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04bf8bf270293886641e8ff9db722e2dfcb255f3d9464a0fc135e28ad01db9dca0ec5f4b70bb0b980774eaa410350b355da932c0827c334242eb0924c025a954b4\",\"transactionHash\":\"abaa62e30ae91c0a9ec9756eafa0a9f2946f8f3780bee291a9b3e1c522cd1163\",\"trustScore\":60.14619,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"4c905efb40df63642b876015304fcced0a84d808959358489fe71165d5229924\",\"s\":\"587fca3fa1d9fc2ea3be0b24f83164f7baf3e2f06fb1d83a2647570f9d1f29ba\"}}],\"senderHash\":\"04bf8bf270293886641e8ff9db722e2dfcb255f3d9464a0fc135e28ad01db9dca0ec5f4b70bb0b980774eaa410350b355da932c0827c334242eb0924c025a954b4\",\"hash\":\"abaa62e30ae91c0a9ec9756eafa0a9f2946f8f3780bee291a9b3e1c522cd1163\"}");
        validTransactionCryptoWrapper = new TransactionCryptoWrapper(validTransactionData);
        notValidTransactionCryptoWrapper = new TransactionCryptoWrapper(notValidTransactionData);
    }

    @Test
    public void testGetBaseTransactionsHashesBytes() {
        Hash BaseTransactionsHash1 = validTransactionCryptoWrapper.getHashFromBaseTransactionHashesData();
        Hash BaseTransactionsHash2 = notValidTransactionCryptoWrapper.getHashFromBaseTransactionHashesData();
        Assert.assertTrue(BaseTransactionsHash1.toString()
                .equals("56236ae7d17e7f0bf1888d45dfb9cfb7648c15549aef9b79e360050a66388d21")
        && BaseTransactionsHash2.toString()
                .equals("ebaa62e30ae91c0a9ec9756eafa0a9f2946f8f3780bee291a9b3e1c522cd1163"));
    }

    @Test
    public void isTransactionValid_whenTransactionValid_returnTrue() {
        Assert.assertTrue(validTransactionCryptoWrapper.isTransactionValid());
    }

    @Test
    public void isTransactionValid_whenTransactionNotValid_returnFalse() {
        Assert.assertFalse(notValidTransactionCryptoWrapper.isTransactionValid());
    }
}