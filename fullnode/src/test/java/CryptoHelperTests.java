import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.crypto.CryptoUtils;
import io.coti.common.data.AddressData;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.TransactionData;
import io.coti.common.crypto.BasicTransactionCryptoWrapper;
import io.coti.common.crypto.CryptoHelper;
import io.coti.common.crypto.TransactionCryptoWrapper;
import io.coti.common.data.Hash;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;


public class CryptoHelperTests {


    @Test
    public void CheckPublicKeyRecovery() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();
        PublicKey key = helper.getPublicKeyFromHexString("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5bcf5cb0819c3ef7f046d9955659fe2a433eadaa5db674405d3780f9b637768d54");
        Assert.assertEquals("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5b",((ECPublicKey) key).getQ().getRawXCoord().toString());
    }




    @Test
    public void verifySignatureTest() throws InvalidKeySpecException,  NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();

        byte[] dataToVerify = new byte []{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        String rHex = "0af936b4ddb6e33269f63d52586ffa3ce7d9358a2fed7fde9536e19a70723860";
        String sHex = "c3a122626df0b7c9d731a8eb9cd42abce7fdd477c591d9f6569be8561ad27639";
        PublicKey key = helper.getPublicKeyFromHexString("989fc9a6b0829cd4aa83e3d7f2d24322dc6c08db80fcef988f8fba226de8f28f5a624afacb6ac328547c94f4b3407e6012f81ebcd59b1b1883037198f3088770");
        Assert.assertEquals(helper.VerifyByPublicKey(dataToVerify,rHex, sHex,key), true);
    }


    @Test
    public void CheckRemovingLeadingZerosFromXpointAddress()
    {
        CryptoHelper helper = new CryptoHelper();
        String address = "0003dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b447d81fd6";
        Hash addressWithoutZeros = helper.RemoveLeadingZerosFromAddress(new Hash(address));
        Assert.assertTrue(Arrays.equals(addressWithoutZeros.getBytes(),new Hash("03dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b447d81fd6").getBytes()));
    }


    @Test
    public void CheckRemovingLeadingZerosFromYpointAddress()
    {
        CryptoHelper helper = new CryptoHelper();
        String address = "d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfc0000b505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf40ac88438";
        Hash addressWithoutZeros = helper.RemoveLeadingZerosFromAddress(new Hash(address));
        Assert.assertTrue(Arrays.equals(addressWithoutZeros.getBytes(),new Hash("d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfcb505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf40ac88438").getBytes()));
    }





    @Test
    public void verifySignatureTestFails() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();

        byte[] dataToVerify = new byte []{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        String rHex = "0af936b4ddb6e33269f63d52586ffa3ce7d9358a2fed7fde9536e19a70723849"; //instead of .......60 changed r value to ......49
        String sHex = "c3a122626df0b7c9d731a8eb9cd42abce7fdd477c591d9f6569be8561ad27639";
        PublicKey key = helper.getPublicKeyFromHexString("989fc9a6b0829cd4aa83e3d7f2d24322dc6c08db80fcef988f8fba226de8f28f5a624afacb6ac328547c94f4b3407e6012f81ebcd59b1b1883037198f3088770");
        Assert.assertEquals(helper.VerifyByPublicKey(dataToVerify,rHex, sHex,key), false);
    }




    @Test
    public void VerifyCorrectAddressCheckSumWithYZerosPadding()
    {
        CryptoHelper helper = new CryptoHelper();
        boolean crcCheckResult = helper.IsAddressValid(new Hash("a3cc8a4b8df1fa322bdf02fe33299f47d5f1ec1d94789b6c1dac1d9312eed400001e769eb710dd18244a404482670276edaa99bb8cb940fa285e3de10043011dad15a177"));
        Assert.assertTrue(crcCheckResult);
    }


    @Test
    public void VerifyCorrectAddressCheckSumWithXZerosPadding()
    {
        CryptoHelper helper = new CryptoHelper();
        boolean crcCheckResult = helper.IsAddressValid(new Hash("0003dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b447d81fd6"));
        Assert.assertTrue(crcCheckResult);
    }

    @Test
    public void VerifyCorrectAddressCheckSum()  {

        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = helper.IsAddressValid(new Hash("bc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662"));

        Assert.assertEquals(isVerified, true);
    }


    @Test
    public void WrongAddressCheckSum()
    {
        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = helper.IsAddressValid(new Hash("cc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662"));
        Assert.assertEquals(isVerified, false);
    }

    @Test
    public void testBasicTransactionVerification() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '36b0e1b46556cb8e7a6fb1cf297bc799381d40bc83f48986dc9ac0dbdd52ce4ea894c517f635e8a4c0943f9a2c1d46f6ce55b34c277b7cf70e0704a7c6f4d113',\n" +
                "\t\t\t'createTime': 1531325119553,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'e33794aee374bc1a949a2aa365d056ad57efdb193d948bd51ea2a712d06e862b',\n" +
                "\t\t\t\t's': 'f9f3f79c0ed0609dff9894aba3a8d800c445c0e4885cd739cb4b69b6d0ed03f6'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '9eff5b4ed178c83af8c3b57cfbca4293728d52e188a947930437b0a1aa0a8bb396e6782667bfaa488400042c65098f0ac4f01863bbb32cb3e3cd1f92e5bff095',\n" +
                "\t\t\t'createTime': 1531325119557,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6dba0a0e7e620f5fca08ba10e3b30946c31f77af0851a82ec87511c0ef7800c',\n" +
                "\t\t\t\t's': '9edb9bd30fe90916ae10c9461e8626b4e0aba668f6c43bb7ecc752889989c3c1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'fce287772288c65be41ea1e607b8012b1f46cfd05bacc921a736db58e9a9d28dc215e6f231592688978cdf1a305b79526450d399436c0e927b473b1206878f2b',\n" +
                "\t\t\t'createTime': 1531325119558,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'c159fdcb2260c65264aaac9c4ee1274ba8b0173bc039645dd866f698a13183a4',\n" +
                "\t\t\t\t's': '8e974f8141c4e5bc401f51c42881946dca2aed9a9d2efb1acf61afd21150a4c0'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '63a5a3117695db3c66bd7015b82254a3b0a40fae64820f1d2e2e40d5bffe187b2c29b694e97808459d9fd70cc73b9793d09807cf89b6772c6ee0cf2654e12ddc',\n" +
                "\t\t\t'createTime': 1531325119559,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '16a90ab949412ce2f4627bab4808c65aa67d07976901cd181f864c114a109c69',\n" +
                "\t\t\t\t's': '1ec87f03a3b4ea5be2199c57ac6858acf698c940e7d0e855e70f0b1a8be2657e'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '16ae3ece9fcac1268f0407fcfb6402e568a917ea26ede90415f986b4337244bbf84fdcb07c2761d6886f4bd1eb424be1860bc9a0b2ace87adb908ef7864d1b01',\n" +
                "\t\t\t'createTime': 1531325119560\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531325119544,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '371d4730c61f94c2204c1cd29015f26ff588f49d5263aa84f408bf29a0188b4f19f720e3be2533c777a7ac8de5cf5787c6ec40d8761c23fc95b67d52f8e61cc3',\n" +
                "\t'senderTrustScore': 64.71902931299373\n" +
                "}";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction,TransactionData.class);


        for (BaseTransactionData basicTx: txData.getBaseTransactions())
        {
            BasicTransactionCryptoWrapper basicTransactionCrypto = new BasicTransactionCryptoWrapper(basicTx);
            Assert.assertTrue(basicTransactionCrypto.IsBasicTransactionValid(txData.getHash()));
        }
    }



    @Test
    public void testInvalidBasicTransactionVerification() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '36b0e1b46556cb8e7a6fb1cf297bc799381d40bc83f48986dc9ac0dbdd52ce4ea894c517f635e8a4c0943f9a2c1d46f6ce55b34c277b7cf70e0704a7c6f4d144',\n" +
                "\t\t\t'createTime': 1531325119553,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'e33794aee374bc1a949a2aa365d056ad57efdb193d948bd51ea2a712d06e862b',\n" +
                "\t\t\t\t's': 'f9f3f79c0ed0609dff9894aba3a8d800c445c0e4885cd739cb4b69b6d0ed03f6'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '9eff5b4ed178c83af8c3b57cfbca4293728d52e188a947930437b0a1aa0a8bb396e6782667bfaa488400042c65098f0ac4f01863bbb32cb3e3cd1f92e5bff077',\n" +
                "\t\t\t'createTime': 1531325119557,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6dba0a0e7e620f5fca08ba10e3b30946c31f77af0851a82ec87511c0ef7800c',\n" +
                "\t\t\t\t's': '9edb9bd30fe90916ae10c9461e8626b4e0aba668f6c43bb7ecc752889989c3c1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'fce287772288c65be41ea1e607b8012b1f46cfd05bacc921a736db58e9a9d28dc215e6f231592688978cdf1a305b79526450d399436c0e927b473b1206878f88',\n" +
                "\t\t\t'createTime': 1531325119558,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'c159fdcb2260c65264aaac9c4ee1274ba8b0173bc039645dd866f698a13183a4',\n" +
                "\t\t\t\t's': '8e974f8141c4e5bc401f51c42881946dca2aed9a9d2efb1acf61afd21150a4c0'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '63a5a3117695db3c66bd7015b82254a3b0a40fae64820f1d2e2e40d5bffe187b2c29b694e97808459d9fd70cc73b9793d09807cf89b6772c6ee0cf2654e12d90',\n" +
                "\t\t\t'createTime': 1531325119559,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '16a90ab949412ce2f4627bab4808c65aa67d07976901cd181f864c114a109c69',\n" +
                "\t\t\t\t's': '1ec87f03a3b4ea5be2199c57ac6858acf698c940e7d0e855e70f0b1a8be2657e'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '16ae3ece9fcac1268f0407fcfb6402e568a917ea26ede90415f986b4337244bbf84fdcb07c2761d6886f4bd1eb424be1860bc9a0b2ace87adb908ef7864d1b23',\n" +
                "\t\t\t'createTime': 1531325119560\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531325119544,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '371d4730c61f94c2204c1cd29015f26ff588f49d5263aa84f408bf29a0188b4f19f720e3be2533c777a7ac8de5cf5787c6ec40d8761c23fc95b67d52f8e61cc3',\n" +
                "\t'senderTrustScore': 64.71902931299373\n" +
                "}";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction,TransactionData.class);


        for (BaseTransactionData basicTx: txData.getBaseTransactions())
        {
            BasicTransactionCryptoWrapper basicTransactionCrypto = new BasicTransactionCryptoWrapper(basicTx);
            Assert.assertFalse(basicTransactionCrypto.IsBasicTransactionValid(txData.getHash()));
        }
    }

    @Test
    public void testTransactionVerification() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '36b0e1b46556cb8e7a6fb1cf297bc799381d40bc83f48986dc9ac0dbdd52ce4ea894c517f635e8a4c0943f9a2c1d46f6ce55b34c277b7cf70e0704a7c6f4d113',\n" +
                "\t\t\t'createTime': 1531325119553,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'e33794aee374bc1a949a2aa365d056ad57efdb193d948bd51ea2a712d06e862b',\n" +
                "\t\t\t\t's': 'f9f3f79c0ed0609dff9894aba3a8d800c445c0e4885cd739cb4b69b6d0ed03f6'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '9eff5b4ed178c83af8c3b57cfbca4293728d52e188a947930437b0a1aa0a8bb396e6782667bfaa488400042c65098f0ac4f01863bbb32cb3e3cd1f92e5bff095',\n" +
                "\t\t\t'createTime': 1531325119557,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6dba0a0e7e620f5fca08ba10e3b30946c31f77af0851a82ec87511c0ef7800c',\n" +
                "\t\t\t\t's': '9edb9bd30fe90916ae10c9461e8626b4e0aba668f6c43bb7ecc752889989c3c1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'fce287772288c65be41ea1e607b8012b1f46cfd05bacc921a736db58e9a9d28dc215e6f231592688978cdf1a305b79526450d399436c0e927b473b1206878f2b',\n" +
                "\t\t\t'createTime': 1531325119558,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'c159fdcb2260c65264aaac9c4ee1274ba8b0173bc039645dd866f698a13183a4',\n" +
                "\t\t\t\t's': '8e974f8141c4e5bc401f51c42881946dca2aed9a9d2efb1acf61afd21150a4c0'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '63a5a3117695db3c66bd7015b82254a3b0a40fae64820f1d2e2e40d5bffe187b2c29b694e97808459d9fd70cc73b9793d09807cf89b6772c6ee0cf2654e12ddc',\n" +
                "\t\t\t'createTime': 1531325119559,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '16a90ab949412ce2f4627bab4808c65aa67d07976901cd181f864c114a109c69',\n" +
                "\t\t\t\t's': '1ec87f03a3b4ea5be2199c57ac6858acf698c940e7d0e855e70f0b1a8be2657e'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '16ae3ece9fcac1268f0407fcfb6402e568a917ea26ede90415f986b4337244bbf84fdcb07c2761d6886f4bd1eb424be1860bc9a0b2ace87adb908ef7864d1b01',\n" +
                "\t\t\t'createTime': 1531325119560\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531325119544,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '371d4730c61f94c2204c1cd29015f26ff588f49d5263aa84f408bf29a0188b4f19f720e3be2533c777a7ac8de5cf5787c6ec40d8761c23fc95b67d52f8e61cc3',\n" +
                "\t'senderTrustScore': 64.71902931299373\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);
        TransactionCryptoWrapper transactionCryptoWrapper = new TransactionCryptoWrapper(obj);
        Assert.assertTrue(transactionCryptoWrapper.isTransactionValid());
    }





    @Test
    public void testTransactionVerificationInvalidTransactionHash() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '36b0e1b46556cb8e7a6fb1cf297bc799381d40bc83f48986dc9ac0dbdd52ce4ea894c517f635e8a4c0943f9a2c1d46f6ce55b34c277b7cf70e0704a7c6f4d113',\n" +
                "\t\t\t'createTime': 1531325119553,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'e33794aee374bc1a949a2aa365d056ad57efdb193d948bd51ea2a712d06e862b',\n" +
                "\t\t\t\t's': 'f9f3f79c0ed0609dff9894aba3a8d800c445c0e4885cd739cb4b69b6d0ed03f6'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '9eff5b4ed178c83af8c3b57cfbca4293728d52e188a947930437b0a1aa0a8bb396e6782667bfaa488400042c65098f0ac4f01863bbb32cb3e3cd1f92e5bff095',\n" +
                "\t\t\t'createTime': 1531325119557,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6dba0a0e7e620f5fca08ba10e3b30946c31f77af0851a82ec87511c0ef7800c',\n" +
                "\t\t\t\t's': '9edb9bd30fe90916ae10c9461e8626b4e0aba668f6c43bb7ecc752889989c3c1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'fce287772288c65be41ea1e607b8012b1f46cfd05bacc921a736db58e9a9d28dc215e6f231592688978cdf1a305b79526450d399436c0e927b473b1206878f2b',\n" +
                "\t\t\t'createTime': 1531325119558,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'c159fdcb2260c65264aaac9c4ee1274ba8b0173bc039645dd866f698a13183a4',\n" +
                "\t\t\t\t's': '8e974f8141c4e5bc401f51c42881946dca2aed9a9d2efb1acf61afd21150a4c0'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '63a5a3117695db3c66bd7015b82254a3b0a40fae64820f1d2e2e40d5bffe187b2c29b694e97808459d9fd70cc73b9793d09807cf89b6772c6ee0cf2654e12ddc',\n" +
                "\t\t\t'createTime': 1531325119559,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '16a90ab949412ce2f4627bab4808c65aa67d07976901cd181f864c114a109c69',\n" +
                "\t\t\t\t's': '1ec87f03a3b4ea5be2199c57ac6858acf698c940e7d0e855e70f0b1a8be2657e'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '16ae3ece9fcac1268f0407fcfb6402e568a917ea26ede90415f986b4337244bbf84fdcb07c2761d6886f4bd1eb424be1860bc9a0b2ace87adb908ef7864d1b01',\n" +
                "\t\t\t'createTime': 1531325119560\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531325119544,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '371d4730c61f94c2204c1cd29015f26ff588f49d5263aa84f408bf29a0188b4f19f720e3be2533c777a7ac8de5cf5787c6ec40d8761c23fc95b67d52f8e61cff',\n" +
                "\t'senderTrustScore': 64.71902931299373\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);


        TransactionCryptoWrapper transactionCryptoWrapper = new TransactionCryptoWrapper(obj);

        Assert.assertFalse(transactionCryptoWrapper.isTransactionValid());



    }

}




