import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.crypto.*;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import io.coti.common.data.Hash;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.crypto.WalletFile;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.stream.Collectors;


public class CryptoHelperTests {

    @Test
    public void deserializeBaseTransaction() throws IOException {


        String jsonOfTransaction = "{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': 'b9e54ac5619b7b4f0c6726482690f66dff06d07f7462643174d4aae0bef77708',\n" +
                "\t\t\t'createTime': 1533118612404,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '5854c891faa14df54fd1e9a7b7d32984c649342e75fe434f028b225f820d9053',\n" +
                "\t\t\t\t's': 'ac8f70d37234d675fc8511435dfd0454101cdf43a8277bde98a4914e850e557c'\n" +
                "\t\t\t}}\n";


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        BaseTransactionData bxData = mapper.readValue(jsonOfTransaction, BaseTransactionData.class);
    }

    @Test
    public void CreateAndSignTransaction() {

        String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e";
        ArrayList<BaseTransactionData> bxDataList = new ArrayList<>();
        bxDataList.add(new BasicTransactionWithPrivateKey(new BigDecimal(-10), new Date(), hexPrivateKey));
        bxDataList.add(new BaseTransactionData(
                new Hash("19ecfb8159ee64f3907f2305fb52737f96efb3ed5cd8893bb9e79a98abd534ae331b0096f0fb5e1e18f9128231ee330cd025a243cc0e98aac40bdc7475d43d318763c3b0"),
                new BigDecimal(10), new Date()));

        TransactionData tx = new TransactionData(bxDataList, "test", 80.53, new Date());
        TransactionCyptoCreator txCreator = new TransactionCyptoCreator(tx);
        txCreator.signTransaction();
        Assert.assertTrue(txCreator.getTransactionCryptoWrapper().isTransactionValid());
    }


    @Test
    public void CheckPublicKeyRecovery() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();
        PublicKey key = helper.getPublicKeyFromHexString("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5bcf5cb0819c3ef7f046d9955659fe2a433eadaa5db674405d3780f9b637768d54");
        Assert.assertEquals("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5b", ((ECPublicKey) key).getQ().getRawXCoord().toString());
    }


    @Test
    public void verifySignatureTest() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();

        byte[] dataToVerify = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        String rHex = "0af936b4ddb6e33269f63d52586ffa3ce7d9358a2fed7fde9536e19a70723860";
        String sHex = "c3a122626df0b7c9d731a8eb9cd42abce7fdd477c591d9f6569be8561ad27639";
        PublicKey key = helper.getPublicKeyFromHexString("989fc9a6b0829cd4aa83e3d7f2d24322dc6c08db80fcef988f8fba226de8f28f5a624afacb6ac328547c94f4b3407e6012f81ebcd59b1b1883037198f3088770");
        Assert.assertEquals(helper.VerifyByPublicKey(dataToVerify, rHex, sHex, key), true);
    }


    @Test
    public void CheckRemovingLeadingZerosFromXpointAddress() {
        CryptoHelper helper = new CryptoHelper();
        String addressWithoutChecksum = "0003dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b4";
        byte[] addressWithoutZeros = helper.RemoveLeadingZerosFromAddress(new Hash(addressWithoutChecksum).getBytes());
        Assert.assertTrue(Arrays.equals(addressWithoutZeros, new Hash("03dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b4").getBytes()));
    }


    @Test
    public void CheckRemovingLeadingZerosFromYpointAddress() {
        CryptoHelper helper = new CryptoHelper();
        String addressWithoutChecksum = "d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfc0000b505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf4";
        byte[] addressWithoutZeros = helper.RemoveLeadingZerosFromAddress(new Hash(addressWithoutChecksum).getBytes());
        Assert.assertTrue(Arrays.equals(addressWithoutZeros, new Hash("d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfcb505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf4").getBytes()));
    }


    @Test
    public void verifySignatureTestFails() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();

        byte[] dataToVerify = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        String rHex = "0af936b4ddb6e33269f63d52586ffa3ce7d9358a2fed7fde9536e19a70723849"; //instead of .......60 changed r value to ......49
        String sHex = "c3a122626df0b7c9d731a8eb9cd42abce7fdd477c591d9f6569be8561ad27639";
        PublicKey key = helper.getPublicKeyFromHexString("989fc9a6b0829cd4aa83e3d7f2d24322dc6c08db80fcef988f8fba226de8f28f5a624afacb6ac328547c94f4b3407e6012f81ebcd59b1b1883037198f3088770");
        Assert.assertEquals(helper.VerifyByPublicKey(dataToVerify, rHex, sHex, key), false);
    }


    @Test
    public void VerifyCorrectAddressCheckSumWithYZerosPadding() {
        CryptoHelper helper = new CryptoHelper();
        boolean crcCheckResult = helper.IsAddressValid(new Hash("a3cc8a4b8df1fa322bdf02fe33299f47d5f1ec1d94789b6c1dac1d9312eed400001e769eb710dd18244a404482670276edaa99bb8cb940fa285e3de10043011dad15a177"));
        Assert.assertTrue(crcCheckResult);
    }


    @Test
    public void VerifyCorrectAddressCheckSumWithXZerosPadding() {
        CryptoHelper helper = new CryptoHelper();
        boolean crcCheckResult = helper.IsAddressValid(new Hash("0003dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b447d81fd6"));
        Assert.assertTrue(crcCheckResult);
    }

    @Test
    public void VerifyCorrectAddressCheckSum() {

        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = helper.IsAddressValid(new Hash("bc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662"));

        Assert.assertEquals(isVerified, true);
    }


    @Test
    public void WrongAddressCheckSum() {
        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = helper.IsAddressValid(new Hash("cc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662"));
        Assert.assertEquals(isVerified, false);
    }

    @Test
    public void testBasicTransactionVerification() throws IOException {
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': 'b9e54ac5619b7b4f0c6726482690f66dff06d07f7462643174d4aae0bef77708',\n" +
                "\t\t\t'createTime': 1533118612404,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '5854c891faa14df54fd1e9a7b7d32984c649342e75fe434f028b225f820d9053',\n" +
                "\t\t\t\t's': 'ac8f70d37234d675fc8511435dfd0454101cdf43a8277bde98a4914e850e557c'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '1dc0c19b19a90f159290a00df920891f8b9db428f89254dc794d83c4ee8c7887',\n" +
                "\t\t\t'createTime': 1533118612405,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '97ba4f3952d81effce2d467895c12c261b58c89f9695d33c0e1b05f8769dc240',\n" +
                "\t\t\t\t's': '21875d570e1c13efe19b664124dd1a77ec4da2f3ad55366fc6b1accdd2a81a0d'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'cdb44e2dff6a54c03468d253469ac245df8558641caad801b6c20dec225a9c55',\n" +
                "\t\t\t'createTime': 1533118612409,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'ecf124dd05fe2471d7e31a09e2f42cf1c86a8e46188262489466988bf34def3e',\n" +
                "\t\t\t\t's': 'e01bcce2e31065c35f25ee5865ec1e4bfd360e4b3e8e67a0b9c931938b47292'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'deec3d4b1d2f3f97c955b74cafa4caafcc1770ac2963a150a1f786198fafd38e',\n" +
                "\t\t\t'createTime': 1533118612411,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '8c8db7c07d65268b966b707da9b92f64c814d92545e8bc166fb787bb9a0a0700',\n" +
                "\t\t\t\t's': 'fe2da8c3015ad0903a5c25a2e56b570e30d01bdbc748c0a86efec54bfe48b2f1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '1f0b94a18e06369e5dfa8a289c189c435c9d91114fff319e53d17b12b94eef27',\n" +
                "\t\t\t'createTime': 1533118612412\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1533118612395,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': 'f5dab95a2a2dc3809c12ddfc6439b5caf2e15dce42c3f4e4ad99fb3b60676a50',\n" +
                "\t'senderTrustScore': 60.99031461090401\n" +
                "}";


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction, TransactionData.class);


        for (BaseTransactionData basicTx : txData.getBaseTransactions()) {
            BaseTransactionCryptoWrapper basicTransactionCrypto = new BaseTransactionCryptoWrapper(basicTx);
            Assert.assertTrue(basicTransactionCrypto.IsBaseTransactionValid(txData.getHash()));
        }
    }


    @Test
    public void testInvalidBasicTransactionVerification() throws IOException {
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '44ca2a07bcd3e5cddb5f1f0e52aee1377e9bf7afd2d3850e3717588f95bae7dff66eaf1d5ecc86f5f7c064b12c15a0e53332180f0275eba0d3c9b1fc6af33d9dea96daf2',\n" +
                "\t\t\t'amount': '-6.885770981428475',\n" +
                "\t\t\t'hash': '5a12046d4a61ebea39585340bc9dc003a05cfa817d6265e14de7e283f6979462',\n" +
                "\t\t\t'createTime': 1533130656569,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'ca6c50f2b34373103f67d7b2c5c38ed4460f16242b9213512f657821d1b8d1b1',\n" +
                "\t\t\t\t's': '7e40dc950d2c28a097fb38d26a0ae6573378d7b2b315bda6fdbcebeaa3e2f1ed'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'e799e58c517d4b28aeb693cf87a0eea83852272858718c53007755b1c7420031cc51042eab026c5ae376a1414760298af1888e8a37109bb333a92f3ff324bc42fca16710',\n" +
                "\t\t\t'amount': '-6.334304379460492',\n" +
                "\t\t\t'hash': '9bcbcd98c80d19196072a717ae7eeb8f74e873642d553ebdf60583158a415341',\n" +
                "\t\t\t'createTime': 1533130656570,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '2a3f0367bce32ad583080f9d8f4581c64b4a5f620cc2ae2ddc95734fffb4f5ce',\n" +
                "\t\t\t\t's': 'f0e4c0f57a7221fafe8b08c194808bbea4e3739703ca9bf224728105c4ae1659'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '19ecfb8159ee64f3907f2305fb52737f96efb3ed5cd8893bb9e79a98abd534ae331b0096f0fb5e1e18f9128231ee330cd025a243cc0e98aac40bdc7475d43d318763c3b0',\n" +
                "\t\t\t'amount': '13.220075360888967',\n" +
                "\t\t\t'hash': '519c52551ec8dbc120a02e811df2b9b51f7ac1cd59b2af1791ba7bf85f6c56f2',\n" +
                "\t\t\t'createTime': 1533130656570\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1533130656568,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': 'be820cf14fc8c09e4fde7d85cc2c8700324d7c02c3ddccfafe176b2a7b1a687d',\n" +
                "\t'senderTrustScore': 46.04248982483402\n" +
                "}\n";


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction, TransactionData.class);


        for (BaseTransactionData basicTx : txData.getBaseTransactions()) {
            BaseTransactionCryptoWrapper basicTransactionCrypto = new BaseTransactionCryptoWrapper(basicTx);
            Assert.assertFalse(basicTransactionCrypto.IsBaseTransactionValid(txData.getHash()));
        }
    }

    @Test
    public void testTransactionVerification() throws IOException {
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': 'b9e54ac5619b7b4f0c6726482690f66dff06d07f7462643174d4aae0bef77708',\n" +
                "\t\t\t'createTime': 1533118612404,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '5854c891faa14df54fd1e9a7b7d32984c649342e75fe434f028b225f820d9053',\n" +
                "\t\t\t\t's': 'ac8f70d37234d675fc8511435dfd0454101cdf43a8277bde98a4914e850e557c'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '1dc0c19b19a90f159290a00df920891f8b9db428f89254dc794d83c4ee8c7887',\n" +
                "\t\t\t'createTime': 1533118612405,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '97ba4f3952d81effce2d467895c12c261b58c89f9695d33c0e1b05f8769dc240',\n" +
                "\t\t\t\t's': '21875d570e1c13efe19b664124dd1a77ec4da2f3ad55366fc6b1accdd2a81a0d'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'cdb44e2dff6a54c03468d253469ac245df8558641caad801b6c20dec225a9c55',\n" +
                "\t\t\t'createTime': 1533118612409,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'ecf124dd05fe2471d7e31a09e2f42cf1c86a8e46188262489466988bf34def3e',\n" +
                "\t\t\t\t's': 'e01bcce2e31065c35f25ee5865ec1e4bfd360e4b3e8e67a0b9c931938b47292'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'deec3d4b1d2f3f97c955b74cafa4caafcc1770ac2963a150a1f786198fafd38e',\n" +
                "\t\t\t'createTime': 1533118612411,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '8c8db7c07d65268b966b707da9b92f64c814d92545e8bc166fb787bb9a0a0700',\n" +
                "\t\t\t\t's': 'fe2da8c3015ad0903a5c25a2e56b570e30d01bdbc748c0a86efec54bfe48b2f1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '1f0b94a18e06369e5dfa8a289c189c435c9d91114fff319e53d17b12b94eef27',\n" +
                "\t\t\t'createTime': 1533118612412\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1533118612395,\n" +
                "\t'transactionDescription':  'test',\n" +
                "\t'hash': 'f5dab95a2a2dc3809c12ddfc6439b5caf2e15dce42c3f4e4ad99fb3b60676a50',\n" +
                "\t'senderTrustScore': 60.99031461090401\n" +
                "}\n";


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction, TransactionData.class);
        TransactionCryptoWrapper transactionCryptoWrapper = new TransactionCryptoWrapper(obj);
        Assert.assertTrue(transactionCryptoWrapper.isTransactionValid());
    }


    @Test
    public void SigningWithPrivateKeyTest() throws InvalidKeySpecException, NoSuchAlgorithmException {

        String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e";
        byte[] msgToSign = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e".getBytes();
        CryptoHelper helper = new CryptoHelper();
        SignatureData signatureData = helper.SignBytes(msgToSign, hexPrivateKey);

        String publicKey = CryptoHelper.GetPublicKeyFromPrivateKey(hexPrivateKey);
        boolean resultVerify = CryptoHelper.VerifyByPublicKey(msgToSign, signatureData.getR(), signatureData.getS(), publicKey);
        Assert.assertTrue(resultVerify);
    }


    @Test
    public void ExtractPublicKeyFromPrivateKey() {

        String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e";
        String publicKey = CryptoHelper.GetPublicKeyFromPrivateKey(hexPrivateKey);
        Assert.assertTrue("a053a4ddfd9c4e27b919a26ccb2d99a55f679c13fec197efc48fc887661a626db19a99660f8ae3babddebf924923afb22c7d4fe251f96f1880c4b8f89106d139".equals(publicKey));
    }


    @Test
    public void testTransactionVerificationInvalidTransactionHash() throws IOException {
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '44ca2a07bcd3e5cddb5f1f0e52aee1377e9bf7afd2d3850e3717588f95bae7dff66eaf1d5ecc86f5f7c064b12c15a0e53332180f0275eba0d3c9b1fc6af33d9dea96daf2',\n" +
                "\t\t\t'amount': '-6.885770981428475',\n" +
                "\t\t\t'hash': '5a12046d4a61ebea39585340bc9dc003a05cfa817d6265e14de7e283f697946c',\n" +
                "\t\t\t'createTime': 1533130656569,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'ca6c50f2b34373103f67d7b2c5c38ed4460f16242b9213512f657821d1b8d1b1',\n" +
                "\t\t\t\t's': '7e40dc950d2c28a097fb38d26a0ae6573378d7b2b315bda6fdbcebeaa3e2f1ed'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'e799e58c517d4b28aeb693cf87a0eea83852272858718c53007755b1c7420031cc51042eab026c5ae376a1414760298af1888e8a37109bb333a92f3ff324bc42fca16710',\n" +
                "\t\t\t'amount': '-6.334304379460492',\n" +
                "\t\t\t'hash': '9bcbcd98c80d19196072a717ae7eeb8f74e873642d553ebdf60583158a41534f',\n" +
                "\t\t\t'createTime': 1533130656570,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '2a3f0367bce32ad583080f9d8f4581c64b4a5f620cc2ae2ddc95734fffb4f5ce',\n" +
                "\t\t\t\t's': 'f0e4c0f57a7221fafe8b08c194808bbea4e3739703ca9bf224728105c4ae1659'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '19ecfb8159ee64f3907f2305fb52737f96efb3ed5cd8893bb9e79a98abd534ae331b0096f0fb5e1e18f9128231ee330cd025a243cc0e98aac40bdc7475d43d318763c3b0',\n" +
                "\t\t\t'amount': '13.220075360888967',\n" +
                "\t\t\t'hash': '519c52551ec8dbc120a02e811df2b9b51f7ac1cd59b2af1791ba7bf85f6c56fe',\n" +
                "\t\t\t'createTime': 1533130656570\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1533130656568,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': 'be820cf14fc8c09e4fde7d85cc2c8700324d7c02c3ddccfafe176b2a7b1a6872',\n" +
                "\t'senderTrustScore': 46.04248982483402\n" +
                "}";


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction, TransactionData.class);


        TransactionCryptoWrapper transactionCryptoWrapper = new TransactionCryptoWrapper(obj);

        Assert.assertFalse(transactionCryptoWrapper.isTransactionValid());


    }

}




