package io.coti.cotinode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.cotinode.crypto.BasicTransactionCryptoDecorator;
import io.coti.cotinode.crypto.CryptoHelper;
import io.coti.cotinode.crypto.TransactionCryptoDecorator;
import io.coti.cotinode.data.AddressData;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;


public class CryptoHelperTests {


    @Test
    public void CheckPublicKeyRecovery() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

        CryptoHelper helper = new CryptoHelper();
        PublicKey key = helper.getPublicKeyFromHexString("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5bcf5cb0819c3ef7f046d9955659fe2a433eadaa5db674405d3780f9b637768d54");
        Assert.assertEquals("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5b",((ECPublicKey) key).getQ().getRawXCoord().toString());
    }




    @Test
    public void verifySignatureTest() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

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
        boolean isVerified = helper.IsAddressValid(new AddressData(new Hash("bc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578DA662")));

        Assert.assertEquals(isVerified, true);
    }


    @Test
    public void WrongAddressCheckSum()
    {
        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = helper.IsAddressValid(new AddressData(new Hash("cc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578DA662")));
        Assert.assertEquals(isVerified, false);
    }

    @Test
    public void testBasicTransactionVerification() throws IOException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '562cd1a12533d2e3826019fa72cd534037dc9738fbe40fa6cfbca2a4ea24bd1bd2c4da221e59cf1f276d71424f2969fef6cf20c475840e63241426d8542ee8904DD5D4BF',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': -30,\n" +
                "\t\t\t'hash': '180F10C1B32FF728C4F6CBAA16B3F0C7EF927E527B27762BABBE1DB667CDE8F3',\n" +
                "\t\t\t'createTime': 1530781319130,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6b5a1c3148acd417b50bd1f8a7b860e3c20b8ecdd0a0863d448d06c5eb061ee',\n" +
                "\t\t\t\t's': '2c7be9935c42dd72b05c7f1c4ed606d198df9723846b3bbe578ce6a18e66d997'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'efe5ba44b6a69539a207dd6ec2915091c48f2e15bcddf80c7d409e33aaea7f797f7d0a8916ac2e5a41fb143f3a64f671b53f2d2eb9b4024671db3a6046939353EA55C4D7',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': -10,\n" +
                "\t\t\t'hash': '3F71725D8DA943523AB944EBE1A9FF36E1CAD86B71BB31F67A7A04A889CEAAA6',\n" +
                "\t\t\t'createTime': 1530781477026,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '522a95a88c77c2555e2f7a5f4eae114100cd8577fadd4c1861e792e7dcf5f8ef',\n" +
                "\t\t\t\t's': '3d37e68de8b6c5b0730f4cc2c87710cf5daa5d5deb1234a01a2c2c325628ca8b'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '751b0a3e2cf9d3ea9aec49538ff450f737181f0c4ecad7dc7995a205fbfe417a3a9ab2e8289db3a5bc28cd5a8c30f64ca246da6e241c8468c5ab2fe07a3cc6bde76d7cd0',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': 30,\n" +
                "\t\t\t'hash': '482EA5C35EF9BDE588D4EA91DAB6BD9A23E9B4EADE994763E099D38CB71D3541',\n" +
                "\t\t\t'createTime': 1530781477030\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1530781319128,\n" +
                "\t'baseTransactionsCount': 3,\n" +
                //"\t'transferDescription': 'test',\n" +
                "\t'hash': '5EA6EB8335D71128DFD21459D4DDF329F529937646264A9882F73B34F7DA9589'\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);


        for (BaseTransactionData basicTx: obj.getBaseTransactions())
        {
            BasicTransactionCryptoDecorator basicTransactionCrypto = new BasicTransactionCryptoDecorator(basicTx);
            Assert.assertTrue(basicTransactionCrypto.IsBasicTransactionValid());
        }


    }





    @Test
    public void testTransactionVerification() throws IOException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '562cd1a12533d2e3826019fa72cd534037dc9738fbe40fa6cfbca2a4ea24bd1bd2c4da221e59cf1f276d71424f2969fef6cf20c475840e63241426d8542ee8904DD5D4BF',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': -30,\n" +
                "\t\t\t'hash': '180F10C1B32FF728C4F6CBAA16B3F0C7EF927E527B27762BABBE1DB667CDE8F3',\n" +
                "\t\t\t'createTime': 1530781319130,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6b5a1c3148acd417b50bd1f8a7b860e3c20b8ecdd0a0863d448d06c5eb061ee',\n" +
                "\t\t\t\t's': '2c7be9935c42dd72b05c7f1c4ed606d198df9723846b3bbe578ce6a18e66d997'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'efe5ba44b6a69539a207dd6ec2915091c48f2e15bcddf80c7d409e33aaea7f797f7d0a8916ac2e5a41fb143f3a64f671b53f2d2eb9b4024671db3a6046939353EA55C4D7',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': -10,\n" +
                "\t\t\t'hash': '3F71725D8DA943523AB944EBE1A9FF36E1CAD86B71BB31F67A7A04A889CEAAA6',\n" +
                "\t\t\t'createTime': 1530781477026,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '522a95a88c77c2555e2f7a5f4eae114100cd8577fadd4c1861e792e7dcf5f8ef',\n" +
                "\t\t\t\t's': '3d37e68de8b6c5b0730f4cc2c87710cf5daa5d5deb1234a01a2c2c325628ca8b'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '751b0a3e2cf9d3ea9aec49538ff450f737181f0c4ecad7dc7995a205fbfe417a3a9ab2e8289db3a5bc28cd5a8c30f64ca246da6e241c8468c5ab2fe07a3cc6bde76d7cd0',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': 30,\n" +
                "\t\t\t'hash': '482EA5C35EF9BDE588D4EA91DAB6BD9A23E9B4EADE994763E099D38CB71D3541',\n" +
                "\t\t\t'createTime': 1530781477030\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1530781319128,\n" +
                "\t'baseTransactionsCount': 3,\n" +
                //"\t'transferDescription': 'test',\n" +
                "\t'hash': '5EA6EB8335D71128DFD21459D4DDF329F529937646264A9882F73B34F7DA9589'\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);


        TransactionCryptoDecorator transactionCryptoDecorator = new TransactionCryptoDecorator(obj);

        Assert.assertTrue(transactionCryptoDecorator.isTransactionValid());



    }





    @Test
    public void testTransactionVerificationInvalidTransactionHash() throws IOException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '562cd1a12533d2e3826019fa72cd534037dc9738fbe40fa6cfbca2a4ea24bd1bd2c4da221e59cf1f276d71424f2969fef6cf20c475840e63241426d8542ee8904DD5D4BF',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': -30,\n" +
                "\t\t\t'hash': '180F10C1B32FF728C4F6CBAA16B3F0C7EF927E527B27762BABBE1DB667CDE8F3',\n" +
                "\t\t\t'createTime': 1530781319130,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6b5a1c3148acd417b50bd1f8a7b860e3c20b8ecdd0a0863d448d06c5eb061ee',\n" +
                "\t\t\t\t's': '2c7be9935c42dd72b05c7f1c4ed606d198df9723846b3bbe578ce6a18e66d997'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'efe5ba44b6a69539a207dd6ec2915091c48f2e15bcddf80c7d409e33aaea7f797f7d0a8916ac2e5a41fb143f3a64f671b53f2d2eb9b4024671db3a6046939353EA55C4D7',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': -10,\n" +
                "\t\t\t'hash': '3F71725D8DA943523AB944EBE1A9FF36E1CAD86B71BB31F67A7A04A889CEAAA6',\n" +
                "\t\t\t'createTime': 1530781477026,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '522a95a88c77c2555e2f7a5f4eae114100cd8577fadd4c1861e792e7dcf5f8ef',\n" +
                "\t\t\t\t's': '3d37e68de8b6c5b0730f4cc2c87710cf5daa5d5deb1234a01a2c2c325628ca8b'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '751b0a3e2cf9d3ea9aec49538ff450f737181f0c4ecad7dc7995a205fbfe417a3a9ab2e8289db3a5bc28cd5a8c30f64ca246da6e241c8468c5ab2fe07a3cc6bde76d7cd0',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': 30,\n" +
                "\t\t\t'hash': '482EA5C35EF9BDE588D4EA91DAB6BD9A23E9B4EADE994763E099D38CB71D3541',\n" +
                "\t\t\t'createTime': 1530781477030\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1530781319128,\n" +
                "\t'baseTransactionsCount': 3,\n" +
                //"\t'transferDescription': 'test',\n" +
                "\t'hash': '5EA6EB8335D71128DFD21459D4DDF329F529937646264A9882F73B34F7DA9566'\n" +
                "}\n";



        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData obj = mapper.readValue(jsonOfTransaction,TransactionData.class);


        TransactionCryptoDecorator transactionCryptoDecorator = new TransactionCryptoDecorator(obj);

        Assert.assertFalse(transactionCryptoDecorator.isTransactionValid());



    }
}




