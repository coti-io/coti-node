package io.coti.cotinode;

import io.coti.cotinode.crypto.CryptoHelper;
import io.coti.cotinode.data.AddressData;
import io.coti.cotinode.data.Hash;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.Assert;
import org.junit.Test;

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
    public void testBasicTransactionVerification()
    {
        /*
        {
        "baseTransactions": [{
            "from": "562cd1a12533d2e3826019fa72cd534037dc9738fbe40fa6cfbca2a4ea24bd1bd2c4da221e59cf1f276d71424f2969fef6cf20c475840e63241426d8542ee8904DD5D4BF",
                    "indexInTransactionsChain": 0,
                    "amount": -30,
                    "hash": "D6D8616C1386DD5189522DB465A4B741D01A71804EB68148ABF65CC5C54B126B",
                    "currentUtc": 1530706540173,
                    "signature": {
                "r": "6b5a1c3148acd417b50bd1f8a7b860e3c20b8ecdd0a0863d448d06c5eb061ee",
                        "s": "2c7be9935c42dd72b05c7f1c4ed606d198df9723846b3bbe578ce6a18e66d997"
            }
        }, {
            "from": "efe5ba44b6a69539a207dd6ec2915091c48f2e15bcddf80c7d409e33aaea7f797f7d0a8916ac2e5a41fb143f3a64f671b53f2d2eb9b4024671db3a6046939353EA55C4D7",
                    "indexInTransactionsChain": 1,
                    "amount": -10,
                    "hash": "77E5B4C2F1446C45E1847D5D450D09206A4EFE721CC292F581080E0B7E62BBC7",
                    "currentUtc": 1530706540184,
                    "signature": {
                "r": "522a95a88c77c2555e2f7a5f4eae114100cd8577fadd4c1861e792e7dcf5f8ef",
                        "s": "3d37e68de8b6c5b0730f4cc2c87710cf5daa5d5deb1234a01a2c2c325628ca8b"
            }
        }, {
            "to": "751b0a3e2cf9d3ea9aec49538ff450f737181f0c4ecad7dc7995a205fbfe417a3a9ab2e8289db3a5bc28cd5a8c30f64ca246da6e241c8468c5ab2fe07a3cc6bde76d7cd0",
                    "indexInTransactionsChain": 2,
                    "amount": 30,
                    "hash": "CFEF06F86779EAD9769F11660D1AB6C12E68E2534D63B1A086E0CB7E4D1B8A43",
                    "currentUtc": 1530706540187
        }
	],
        "currentUtc": 1530706540171,
                "baseTransactionsCount": 3,
                "transferDescription": "test",
                "transactionHash": "DF45D5B8DDB962968E843A6A5A108B7198C781EF5D0B8412E1F1DEF1867AAA35"
    }*/

}
}




