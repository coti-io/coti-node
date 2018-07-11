import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.data.AddressData;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.TransactionData;
import io.coti.common.crypto.BasicTransactionCryptoDecorator;
import io.coti.common.crypto.CryptoHelper;
import io.coti.common.crypto.TransactionCryptoDecorator;
import io.coti.common.data.Hash;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.Buffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;


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
    public void verifySignatureTestFails() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

        CryptoHelper helper = new CryptoHelper();

        byte[] dataToVerify = new byte []{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        String rHex = "0af936b4ddb6e33269f63d52586ffa3ce7d9358a2fed7fde9536e19a70723849"; //instead of .......60 changed r value to ......49
        String sHex = "c3a122626df0b7c9d731a8eb9cd42abce7fdd477c591d9f6569be8561ad27639";
        PublicKey key = helper.getPublicKeyFromHexString("989fc9a6b0829cd4aa83e3d7f2d24322dc6c08db80fcef988f8fba226de8f28f5a624afacb6ac328547c94f4b3407e6012f81ebcd59b1b1883037198f3088770");
        Assert.assertEquals(helper.VerifyByPublicKey(dataToVerify,rHex, sHex,key), false);
    }


    @Test
    public void VerifyCorrectAddressCheckSum()  {

        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = helper.IsAddressValid(new AddressData(new Hash("bc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662")));

        Assert.assertEquals(isVerified, true);
    }


    @Test
    public void WrongAddressCheckSum()
    {
        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = helper.IsAddressValid(new AddressData(new Hash("cc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662")));
        Assert.assertEquals(isVerified, false);
    }

    @Test
    public void testBasicTransactionVerification() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '64ebe7231e74b430682e24aac710116d626f0aa66031d080cbef6392e537be4d',\n" +
                "\t\t\t'createTime': 1531227901014,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '32e9dd10e56f10d4527636040d390953c44920a06557220a4e8b71d090710832',\n" +
                "\t\t\t\t's': '89040ccf9109d6a0b6eb291d68c847deb8b22ff7f551f527ebbb55c06f63b2c2'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8c72322f61555a2104a75153d03f6c9a0c8f7768f728a402a36388e7841161d7',\n" +
                "\t\t\t'createTime': 1531227901019,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '12231b51dff44e891543d8ee531effe432daa38af6b32f448bdf199336402b7e',\n" +
                "\t\t\t\t's': 'd6bacc6bada3409514887ebf83e4f1faa2fa2c9f30c7ee62283310f5d7bf6202'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '4f296f77e686e2020f40c1211e4d830aea29660791e155f7c8a366093605590d',\n" +
                "\t\t\t'createTime': 1531227901022,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f47a7',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8d252ad6b7531fd2ef9f31b5458bdd1328c9b2fe20a311deed5d844f72569932',\n" +
                "\t\t\t'createTime': 1531227901026,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f47a7',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': 'b65beed81a47e5286352d23e8ad0f7b8640380131e112c207cc7658bc3cc88d2',\n" +
                "\t\t\t'createTime': 1531227901027\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531227901004,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '198025c61e21ed27d6a38999c183a786b67561c89ef5d99a370cfce277f50080',\n" +
                "\t'senderTrustScore': 86.82785706862107\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction,TransactionData.class);


        for (BaseTransactionData basicTx: txData.getBaseTransactions())
        {
            BasicTransactionCryptoDecorator basicTransactionCrypto = new BasicTransactionCryptoDecorator(basicTx);
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
                "\t\t\t'hash': '64ebe7231e74b430682e24aac710116d626f0aa66031d080cbef6392e537be45',\n" +
                "\t\t\t'createTime': 1531227901014,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '32e9dd10e56f10d4527636040d390953c44920a06557220a4e8b71d090710832',\n" +
                "\t\t\t\t's': '89040ccf9109d6a0b6eb291d68c847deb8b22ff7f551f527ebbb55c06f63b2c2'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8c72322f61555a2104a75153d03f6c9a0c8f7768f728a402a36388e7841161d6',\n" +
                "\t\t\t'createTime': 1531227901019,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '12231b51dff44e891543d8ee531effe432daa38af6b32f448bdf199336402b7e',\n" +
                "\t\t\t\t's': 'd6bacc6bada3409514887ebf83e4f1faa2fa2c9f30c7ee62283310f5d7bf6202'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '4f296f77e686e2020f40c1211e4d830aea29660791e155f7c8a3660936055909',\n" +
                "\t\t\t'createTime': 1531227901022,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f47a7',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8d252ad6b7531fd2ef9f31b5458bdd1328c9b2fe20a311deed5d844f72569932',\n" +
                "\t\t\t'createTime': 1531227901026,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f4780',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': 'b65beed81a47e5286352d23e8ad0f7b8640380131e112c207cc7658bc3cc8890',\n" +
                "\t\t\t'createTime': 1531227901027\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531227901004,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '198025c61e21ed27d6a38999c183a786b67561c89ef5d99a370cfce277f50080',\n" +
                "\t'senderTrustScore': 86.82785706862107\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = mapper.readValue(jsonOfTransaction,TransactionData.class);


        for (BaseTransactionData basicTx: txData.getBaseTransactions())
        {
            BasicTransactionCryptoDecorator basicTransactionCrypto = new BasicTransactionCryptoDecorator(basicTx);
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
                "\t\t\t'hash': '64ebe7231e74b430682e24aac710116d626f0aa66031d080cbef6392e537be4d',\n" +
                "\t\t\t'createTime': 1531227901014,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '32e9dd10e56f10d4527636040d390953c44920a06557220a4e8b71d090710832',\n" +
                "\t\t\t\t's': '89040ccf9109d6a0b6eb291d68c847deb8b22ff7f551f527ebbb55c06f63b2c2'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8c72322f61555a2104a75153d03f6c9a0c8f7768f728a402a36388e7841161d7',\n" +
                "\t\t\t'createTime': 1531227901019,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '12231b51dff44e891543d8ee531effe432daa38af6b32f448bdf199336402b7e',\n" +
                "\t\t\t\t's': 'd6bacc6bada3409514887ebf83e4f1faa2fa2c9f30c7ee62283310f5d7bf6202'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '4f296f77e686e2020f40c1211e4d830aea29660791e155f7c8a366093605590d',\n" +
                "\t\t\t'createTime': 1531227901022,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f47a7',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8d252ad6b7531fd2ef9f31b5458bdd1328c9b2fe20a311deed5d844f72569932',\n" +
                "\t\t\t'createTime': 1531227901026,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f47a7',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': 'b65beed81a47e5286352d23e8ad0f7b8640380131e112c207cc7658bc3cc88d2',\n" +
                "\t\t\t'createTime': 1531227901027\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531227901004,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '198025c61e21ed27d6a38999c183a786b67561c89ef5d99a370cfce277f50080',\n" +
                "\t'senderTrustScore': 86.82785706862107\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);
        TransactionCryptoDecorator transactionCryptoDecorator = new TransactionCryptoDecorator(obj);
        Assert.assertTrue(transactionCryptoDecorator.isTransactionValid());
    }





    @Test
    public void testTransactionVerificationInvalidTransactionHash() throws IOException{
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '64ebe7231e74b430682e24aac710116d626f0aa66031d080cbef6392e537be4d',\n" +
                "\t\t\t'createTime': 1531227901014,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '32e9dd10e56f10d4527636040d390953c44920a06557220a4e8b71d090710832',\n" +
                "\t\t\t\t's': '89040ccf9109d6a0b6eb291d68c847deb8b22ff7f551f527ebbb55c06f63b2c2'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8c72322f61555a2104a75153d03f6c9a0c8f7768f728a402a36388e7841161d7',\n" +
                "\t\t\t'createTime': 1531227901019,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '12231b51dff44e891543d8ee531effe432daa38af6b32f448bdf199336402b7e',\n" +
                "\t\t\t\t's': 'd6bacc6bada3409514887ebf83e4f1faa2fa2c9f30c7ee62283310f5d7bf6202'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '4f296f77e686e2020f40c1211e4d830aea29660791e155f7c8a366093605590d',\n" +
                "\t\t\t'createTime': 1531227901022,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f47a7',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'd648cf986268d54b444e081d23c4ee8342fde69a550b666a89e11ca6af77a5cecf8875ef7000636dcfb85ffdfc29bc31af3e2be3b644951c503a06ae4ee753a24945641a',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '8d252ad6b7531fd2ef9f31b5458bdd1328c9b2fe20a311deed5d844f72569932',\n" +
                "\t\t\t'createTime': 1531227901026,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '25ab84bc50a8bf7b7cdb5c3699166f4433664a1adafdb5c6018dc4d4c62f47a7',\n" +
                "\t\t\t\t's': '4c8b25e4524b50f417be8d2be58b38dd845c4acfb977d23d527decbc87a6bde9'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': 'b65beed81a47e5286352d23e8ad0f7b8640380131e112c207cc7658bc3cc88d2',\n" +
                "\t\t\t'createTime': 1531227901027\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531227901004,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '198025c61e21ed27d6a38999c183a786b67561c89ef5d99a370cfce277f50099',\n" +
                "\t'senderTrustScore': 86.82785706862107\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);


        TransactionCryptoDecorator transactionCryptoDecorator = new TransactionCryptoDecorator(obj);

        Assert.assertFalse(transactionCryptoDecorator.isTransactionValid());



    }

    @Test
    public void testCrc32Check()
    {
        CryptoHelper helper = new CryptoHelper();
        boolean crcCheckResult = helper.VerifyAddressCrc32(new Hash("244b3b31d29b93fb1e69dd07277c070ec2768620297a0c7e46e27b8974189ef10739ef8efcd02e2c710c4405fa3cf5e49627a5704c8a9ee3547868fb6f3e9b8c8ddafa95"));
        Assert.assertTrue(crcCheckResult);
    }
}




