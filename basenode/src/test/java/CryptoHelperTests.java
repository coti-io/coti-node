import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;


public class CryptoHelperTests {


    @Test
    public void CheckPublicKeyRecovery() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();
        PublicKey key = CryptoHelper.getPublicKeyFromHexString("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5bcf5cb0819c3ef7f046d9955659fe2a433eadaa5db674405d3780f9b637768d54");
        Assert.assertEquals("c9fd981b86819a190272911bb9890ab29292fdb0666c2601331559ffd01c4b5b", ((ECPublicKey) key).getQ().getRawXCoord().toString());
    }


    @Test
    public void verifySignatureTest() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();

        byte[] dataToVerify = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        String rHex = "0af936b4ddb6e33269f63d52586ffa3ce7d9358a2fed7fde9536e19a70723860";
        String sHex = "c3a122626df0b7c9d731a8eb9cd42abce7fdd477c591d9f6569be8561ad27639";
        PublicKey key = CryptoHelper.getPublicKeyFromHexString("989fc9a6b0829cd4aa83e3d7f2d24322dc6c08db80fcef988f8fba226de8f28f5a624afacb6ac328547c94f4b3407e6012f81ebcd59b1b1883037198f3088770");
        Assert.assertEquals(CryptoHelper.VerifyByPublicKey(dataToVerify, rHex, sHex, key), true);
    }


    @Test
    public void CheckRemovingLeadingZerosFromXpointAddress() {
        CryptoHelper helper = new CryptoHelper();
        String addressWithoutChecksum = "0003dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b4";
        byte[] addressWithoutZeros = CryptoHelper.RemoveLeadingZerosFromAddress(new Hash(addressWithoutChecksum).getBytes());
        Assert.assertTrue(Arrays.equals(addressWithoutZeros, new Hash("03dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b4").getBytes()));
    }


    @Test
    public void CheckRemovingLeadingZerosFromYpointAddress() {
        CryptoHelper helper = new CryptoHelper();
        String addressWithoutChecksum = "d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfc0000b505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf4";
        byte[] addressWithoutZeros = CryptoHelper.RemoveLeadingZerosFromAddress(new Hash(addressWithoutChecksum).getBytes());
        Assert.assertTrue(Arrays.equals(addressWithoutZeros, new Hash("d4501c35e1662ce0d66db15de20c665abc8e462edb208d51fd573a5490883bfcb505805f18fe49bf2dc3cdf06ada9198328f61ed782e9bab7e9fd314ecf4").getBytes()));
    }


    @Test
    public void verifySignatureTestFails() throws InvalidKeySpecException, NoSuchAlgorithmException {

        CryptoHelper helper = new CryptoHelper();

        byte[] dataToVerify = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        String rHex = "0af936b4ddb6e33269f63d52586ffa3ce7d9358a2fed7fde9536e19a70723849"; //instead of .......60 changed r value to ......49
        String sHex = "c3a122626df0b7c9d731a8eb9cd42abce7fdd477c591d9f6569be8561ad27639";
        PublicKey key = CryptoHelper.getPublicKeyFromHexString("989fc9a6b0829cd4aa83e3d7f2d24322dc6c08db80fcef988f8fba226de8f28f5a624afacb6ac328547c94f4b3407e6012f81ebcd59b1b1883037198f3088770");
        Assert.assertEquals(CryptoHelper.VerifyByPublicKey(dataToVerify, rHex, sHex, key), false);
    }


    @Test
    public void VerifyCorrectAddressCheckSumWithYZerosPadding() {
        CryptoHelper helper = new CryptoHelper();
        boolean crcCheckResult = CryptoHelper.IsAddressValid(new Hash("a3cc8a4b8df1fa322bdf02fe33299f47d5f1ec1d94789b6c1dac1d9312eed400001e769eb710dd18244a404482670276edaa99bb8cb940fa285e3de10043011dad15a177"));
        Assert.assertTrue(crcCheckResult);
    }


    @Test
    public void VerifyCorrectAddressCheckSumWithXZerosPadding() {
        CryptoHelper helper = new CryptoHelper();
        boolean crcCheckResult = CryptoHelper.IsAddressValid(new Hash("0003dabc04ac8680698104f35d5818432c619a2a7e42bfc8d791181d897b6bfa664664133b80585844c7452bcb50c860a1167b1be8cfa201c44dd94eb706c8b447d81fd6"));
        Assert.assertTrue(crcCheckResult);
    }

    @Test
    public void VerifyCorrectAddressCheckSum() {

        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = CryptoHelper.IsAddressValid(new Hash("bc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662"));

        Assert.assertEquals(isVerified, true);
    }


    @Test
    public void WrongAddressCheckSum() {
        CryptoHelper helper = new CryptoHelper();
        boolean isVerified = CryptoHelper.IsAddressValid(new Hash("cc0798cc85e98a8ed4160b8a21e17df7ce86edfd1efabc87c069b345858a49ab3e51540465f175952d19ac877b42cb044c04bb1c624e13b2f73382841ad452c7578da662"));
        Assert.assertEquals(isVerified, false);
    }


    @Test
    public void SigningWithPrivateKeyTest() throws InvalidKeySpecException, NoSuchAlgorithmException {

        String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e";
        byte[] msgToSign = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e".getBytes();
        CryptoHelper helper = new CryptoHelper();
        SignatureData signatureData = CryptoHelper.SignBytes(msgToSign, hexPrivateKey);

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


}




