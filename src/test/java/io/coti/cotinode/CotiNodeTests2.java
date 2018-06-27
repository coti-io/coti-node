package io.coti.cotinode;

import io.coti.cotinode.crypto.CryptoUtils;
import org.junit.Test;

import java.math.BigInteger;

public class CotiNodeTests2 {

    @Test
    public void TransferAmountBetweenAddresses(){
        String seed1 = "1010";
        String seed2 = "2020";
        String message = "This is my message";
        BigInteger seed1PrivateKey1 = CryptoUtils.generatePrivateKey(seed1, 1);
        BigInteger seed1PrivateKey2 = CryptoUtils.generatePrivateKey(seed1, 2);
        BigInteger seed2PrivateKey1 = CryptoUtils.generatePrivateKey(seed2, 1);
        BigInteger seed1Address1 = CryptoUtils.getPublicKeyFromPrivateKey(seed1PrivateKey1);
        BigInteger seed1Address2 = CryptoUtils.getPublicKeyFromPrivateKey(seed1PrivateKey2);
        BigInteger seed2Address1 = CryptoUtils.getPublicKeyFromPrivateKey(seed2PrivateKey1);

        String seed1Address1Signature = CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(seed1PrivateKey1, message);
        String seed1Address2Signature = CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(seed1PrivateKey2, message);
        String seed2Address1Signature = CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(seed2PrivateKey1, message);

        System.out.println(seed1Address1Signature);
        System.out.println(seed1Address2Signature);
        System.out.println(seed2Address1Signature);

        System.out.println(CryptoUtils.bytesToHex(seed1Address1.toByteArray()));
        System.out.println(CryptoUtils.bytesToHex(seed1Address2.toByteArray()));
        System.out.println(CryptoUtils.bytesToHex(seed2Address1.toByteArray()));
    }
}
