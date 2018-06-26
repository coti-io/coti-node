package io.coti.cotinode.crypto;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;

public class CryptoUtils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String getSignatureStringFromPrivateKeyAndMessage(BigInteger privateKey, String message){
        ECKeyPair pair = ECKeyPair.create(privateKey);
        Sign.SignatureData signatureData = Sign.signMessage(message.getBytes(), pair);
        return convertSignatureToString(signatureData);
    }

    public static BigInteger getPublicKeyFromPrivateKey(BigInteger privateKey){
        return Sign.publicKeyFromPrivate(privateKey);
    }



    public static Sign.SignatureData convertSignatureFromString(String signatureString) {
        String[] signatureParts = signatureString.split("\\$");
        if (signatureParts.length != 3) {
            return null;
        }
        byte v = hexStringToByteArray(signatureParts[0])[0];
        byte[] r = hexStringToByteArray(signatureParts[1]);
        byte[] s = hexStringToByteArray(signatureParts[2]);
        return new Sign.SignatureData(v, r, s);
    }

    public static String convertSignatureToString(Sign.SignatureData signatureData) {
        String vString = bytesToHex(new byte[]{signatureData.getV()});
        String rString = bytesToHex(signatureData.getR());
        String sString = bytesToHex(signatureData.getS());
        return vString + "$" + rString + "$" + sString;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


}
