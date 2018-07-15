import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.TransactionData;
import io.coti.common.crypto.BaseTransactionCryptoWrapper;
import io.coti.common.crypto.CryptoHelper;
import io.coti.common.crypto.TransactionCryptoWrapper;
import io.coti.common.data.Hash;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
        String addressWithoutChecksum = "0003dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b4";
        byte[] addressWithoutZeros = helper.RemoveLeadingZerosFromAddress(new Hash(addressWithoutChecksum).getBytes());
        Assert.assertTrue(Arrays.equals(addressWithoutZeros,new Hash("03dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b4").getBytes()));
    }


    @Test
    public void CheckRemovingLeadingZerosFromYpointAddress()
    {
        CryptoHelper helper = new CryptoHelper();
        String addressWithoutChecksum = "d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfc0000b505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf4";
        byte[] addressWithoutZeros = helper.RemoveLeadingZerosFromAddress(new Hash(addressWithoutChecksum).getBytes());
        Assert.assertTrue(Arrays.equals(addressWithoutZeros,new Hash("d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfcb505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf4").getBytes()));
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
                "\t\t\t'hash': '914755201efd8eadea48430e92bda14a7357b4bf95f366bf3641f0a26ac113d9',\n" +
                "\t\t\t'createTime': 1531688758490,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '664d990b6d49a83bd9380642480facbe77a283875ba1e327836178caed358784',\n" +
                "\t\t\t\t's': '9a5e6590b2720db6ebdab71bbaac2618839c381bf6c227590e9a776533728bef'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'd05c75913d6fc08673111f965c83a451b0469ce0c98e07ccb2ad418a1f2d39af',\n" +
                "\t\t\t'createTime': 1531688758492,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '57ddb4df8e408e8708b3e7a68e82d514ce0ef4409b3e0560a8f6f7558e339584',\n" +
                "\t\t\t\t's': '6be9722669490e4abfda538cb8e86f4a1b08038e5d6bfa40c28cc9f90473b404'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '664792bbf4c80c7e5952888b054ab8a96a0109806ec7519289d652d082b44405',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'dec9e04812c9de6a4384b27b4b58823b045c95b0e9aedc60781326acbeb06dfc',\n" +
                "\t\t\t\t's': 'f3d88198f4a6964790ed61c8146600da2384929a378f2649e2193cfdd068609d'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '1636f09572156d0298a439254543f93faecee0413bf437ae74fd6e7ee5cf5483',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '71270624df733adf4d9c33f5ffccce5f8b9669f34033460c9dabf75b48596ad5',\n" +
                "\t\t\t\t's': '3890ea35df9fe7f42e7697469378d6b9e499be51068d2b601456cdcd1b72585c'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '34ecf925edf9fcd76d8bdaccb6bf1e2ee2bd0a22a1b307920325fce1bf1b616b',\n" +
                "\t\t\t'createTime': 1531688758497\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531688758484,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '81b4f2dc2b07cd92ed1b1b678568b112f53d1147687193686d17d35801522b34',\n" +
                "\t'senderTrustScore': 56.239619552073165\n" +
                "}";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction,TransactionData.class);


        for (BaseTransactionData basicTx: txData.getBaseTransactions())
        {
            BaseTransactionCryptoWrapper basicTransactionCrypto = new BaseTransactionCryptoWrapper(basicTx);
            Assert.assertTrue(basicTransactionCrypto.IsBaseTransactionValid(txData.getHash()));
        }
    }



    @Test
    public void testInvalidBasicTransactionVerification() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '914755201efd8eadea48430e92bda14a7357b4bf95f366bf3641f0a26ac113d8',\n" +
                "\t\t\t'createTime': 1531688758490,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '664d990b6d49a83bd9380642480facbe77a283875ba1e327836178caed358784',\n" +
                "\t\t\t\t's': '9a5e6590b2720db6ebdab71bbaac2618839c381bf6c227590e9a776533728bef'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'd05c75913d6fc08673111f965c83a451b0469ce0c98e07ccb2ad418a1f2d39ae',\n" +
                "\t\t\t'createTime': 1531688758492,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '57ddb4df8e408e8708b3e7a68e82d514ce0ef4409b3e0560a8f6f7558e339584',\n" +
                "\t\t\t\t's': '6be9722669490e4abfda538cb8e86f4a1b08038e5d6bfa40c28cc9f90473b404'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '664792bbf4c80c7e5952888b054ab8a96a0109806ec7519289d652d082b4440t',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'dec9e04812c9de6a4384b27b4b58823b045c95b0e9aedc60781326acbeb06dfc',\n" +
                "\t\t\t\t's': 'f3d88198f4a6964790ed61c8146600da2384929a378f2649e2193cfdd068609d'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '1636f09572156d0298a439254543f93faecee0413bf437ae74fd6e7ee5cf548c',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '71270624df733adf4d9c33f5ffccce5f8b9669f34033460c9dabf75b48596ad5',\n" +
                "\t\t\t\t's': '3890ea35df9fe7f42e7697469378d6b9e499be51068d2b601456cdcd1b72585c'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '34ecf925edf9fcd76d8bdaccb6bf1e2ee2bd0a22a1b307920325fce1bf1b616a',\n" +
                "\t\t\t'createTime': 1531688758497\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531688758484,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '81b4f2dc2b07cd92ed1b1b678568b112f53d1147687193686d17d35801522b34',\n" +
                "\t'senderTrustScore': 56.239619552073165\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction,TransactionData.class);


        for (BaseTransactionData basicTx: txData.getBaseTransactions())
        {
            BaseTransactionCryptoWrapper basicTransactionCrypto = new BaseTransactionCryptoWrapper(basicTx);
            Assert.assertFalse(basicTransactionCrypto.IsBaseTransactionValid(txData.getHash()));
        }
    }

    @Test
    public void testTransactionVerification() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '914755201efd8eadea48430e92bda14a7357b4bf95f366bf3641f0a26ac113d9',\n" +
                "\t\t\t'createTime': 1531688758490,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '664d990b6d49a83bd9380642480facbe77a283875ba1e327836178caed358784',\n" +
                "\t\t\t\t's': '9a5e6590b2720db6ebdab71bbaac2618839c381bf6c227590e9a776533728bef'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'd05c75913d6fc08673111f965c83a451b0469ce0c98e07ccb2ad418a1f2d39af',\n" +
                "\t\t\t'createTime': 1531688758492,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '57ddb4df8e408e8708b3e7a68e82d514ce0ef4409b3e0560a8f6f7558e339584',\n" +
                "\t\t\t\t's': '6be9722669490e4abfda538cb8e86f4a1b08038e5d6bfa40c28cc9f90473b404'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '664792bbf4c80c7e5952888b054ab8a96a0109806ec7519289d652d082b44405',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'dec9e04812c9de6a4384b27b4b58823b045c95b0e9aedc60781326acbeb06dfc',\n" +
                "\t\t\t\t's': 'f3d88198f4a6964790ed61c8146600da2384929a378f2649e2193cfdd068609d'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '1636f09572156d0298a439254543f93faecee0413bf437ae74fd6e7ee5cf5483',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '71270624df733adf4d9c33f5ffccce5f8b9669f34033460c9dabf75b48596ad5',\n" +
                "\t\t\t\t's': '3890ea35df9fe7f42e7697469378d6b9e499be51068d2b601456cdcd1b72585c'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '34ecf925edf9fcd76d8bdaccb6bf1e2ee2bd0a22a1b307920325fce1bf1b616b',\n" +
                "\t\t\t'createTime': 1531688758497\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531688758484,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '81b4f2dc2b07cd92ed1b1b678568b112f53d1147687193686d17d35801522b34',\n" +
                "\t'senderTrustScore': 56.239619552073165\n" +
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
                "\t\t\t'hash': '914755201efd8eadea48430e92bda14a7357b4bf95f366bf3641f0a26ac113d9',\n" +
                "\t\t\t'createTime': 1531688758490,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '664d990b6d49a83bd9380642480facbe77a283875ba1e327836178caed358784',\n" +
                "\t\t\t\t's': '9a5e6590b2720db6ebdab71bbaac2618839c381bf6c227590e9a776533728bef'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'd05c75913d6fc08673111f965c83a451b0469ce0c98e07ccb2ad418a1f2d39af',\n" +
                "\t\t\t'createTime': 1531688758492,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '57ddb4df8e408e8708b3e7a68e82d514ce0ef4409b3e0560a8f6f7558e339584',\n" +
                "\t\t\t\t's': '6be9722669490e4abfda538cb8e86f4a1b08038e5d6bfa40c28cc9f90473b404'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '664792bbf4c80c7e5952888b054ab8a96a0109806ec7519289d652d082b44405',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'dec9e04812c9de6a4384b27b4b58823b045c95b0e9aedc60781326acbeb06dfc',\n" +
                "\t\t\t\t's': 'f3d88198f4a6964790ed61c8146600da2384929a378f2649e2193cfdd068609d'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '1636f09572156d0298a439254543f93faecee0413bf437ae74fd6e7ee5cf5483',\n" +
                "\t\t\t'createTime': 1531688758496,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '71270624df733adf4d9c33f5ffccce5f8b9669f34033460c9dabf75b48596ad5',\n" +
                "\t\t\t\t's': '3890ea35df9fe7f42e7697469378d6b9e499be51068d2b601456cdcd1b72585c'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '34ecf925edf9fcd76d8bdaccb6bf1e2ee2bd0a22a1b307920325fce1bf1b616b',\n" +
                "\t\t\t'createTime': 1531688758497\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531688758484,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '81b4f2dc2b07cd92ed1b1b678568b112f53d1147687193686d17d35801522b31',\n" +
                "\t'senderTrustScore': 56.239619552073165\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);


        TransactionCryptoWrapper transactionCryptoWrapper = new TransactionCryptoWrapper(obj);

        Assert.assertFalse(transactionCryptoWrapper.isTransactionValid());



    }

}




