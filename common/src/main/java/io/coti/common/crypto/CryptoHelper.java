package io.coti.common.crypto;

import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;

import org.bouncycastle.crypto.params.ECDomainParameters;

import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;


import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


public class CryptoHelper {

    static final String ecSpec="secp256k1";
    static final String ecAlgorithm = "ECDSA";
    private static final X9ECParameters curve = SECNamedCurves.getByName (ecSpec);
    private static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve (), curve.getG (), curve.getN (), curve.getH ());
    private static final ECParameterSpec spec = new ECParameterSpec(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());


    public static PublicKey getPublicKeyFromHexString(String pubKeyHex) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pointX = pubKeyHex.substring(0, (pubKeyHex.length()/2));
        String pointY = pubKeyHex.substring(pubKeyHex.length()/2);

        BigInteger p256_xp = new BigInteger(pointX, 16);
        BigInteger p256_yp = new BigInteger(pointY, 16);

        org.bouncycastle.math.ec.ECPoint point = curve.getCurve().createPoint(p256_xp,p256_yp);
        ECPublicKeySpec publicSpec = new ECPublicKeySpec(point, spec);
        KeyFactory keyfac = KeyFactory.getInstance(ecAlgorithm, new BouncyCastleProvider());

        PublicKey pubKey = keyfac.generatePublic(publicSpec);
        return pubKey;
    }





    private static PublicKey getPublicKeyFromByte(byte[] pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        org.bouncycastle.math.ec.ECPoint point = curve.getCurve().decodePoint(pubKey);
        PublicKey result = KeyFactory.getInstance(ecAlgorithm,new BouncyCastleProvider()).generatePublic(new ECPublicKeySpec(point, CryptoHelper.spec));
        return result;
    }
    public static boolean VerifyByPublicKey(byte[] originalMessageToVerify, String rHex, String sHex, String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return VerifyByPublicKey(originalMessageToVerify,rHex,sHex,getPublicKeyFromHexString(publicKey));
    }



    public static byte[] RemoveLeadingZerosFromAddress(byte[] addressBytes){
        byte[] xPart = Arrays.copyOfRange(addressBytes,0, addressBytes.length/2 );
        byte[] yPart = Arrays.copyOfRange(addressBytes,addressBytes.length/2,addressBytes.length);

        byte[] xPointPart = new byte[0];
        byte[] yPointPart = new byte[0];


        for (int i=0; i <xPart.length;i++)
        {
            if (xPart[i] == 0)
                continue;
            xPointPart = Arrays.copyOfRange(xPart, i, xPart.length);
            break;
        }

        for (int i=0; i <yPart.length;i++)
        {
            if (yPart[i] == 0)
                continue;

            yPointPart = Arrays.copyOfRange(yPart, i, yPart.length);
            break;
        }

        ByteBuffer addressBuffer = ByteBuffer.allocate(xPointPart.length + yPointPart.length);
        addressBuffer.put(xPointPart);
        addressBuffer.put(yPointPart);
        return addressBuffer.array();
    }



    public static boolean  VerifyByPublicKey(byte[] originalDataToVerify, String rHex, String sHex, PublicKey publicKey)
    {
        ECDSASigner signer = new ECDSASigner();
        signer.init(false, new ECPublicKeyParameters(((ECPublicKey)publicKey).getQ(), domain));
        BigInteger r = new BigInteger( rHex, 16);
        BigInteger s = new BigInteger( sHex, 16);
        return signer.verifySignature(originalDataToVerify, r, s);
    }

    public static KeyPair generateKeyPair() throws  NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator g = KeyPairGenerator.getInstance(ecAlgorithm, new BouncyCastleProvider());
        g.initialize(ECNamedCurveTable.getParameterSpec(ecSpec), new SecureRandom());
        KeyPair pair = g.generateKeyPair();
        return pair;
    }

    public static boolean IsAddressValid(Hash addressHash)
    {
        byte[] addressBytes = addressHash.getBytes();
        if (addressBytes.length != 68)
            return false;

        Checksum checksum = new CRC32();
        byte[] addressWithoutCheckSum = Arrays.copyOfRange(addressBytes,0 , addressBytes.length-4);
        byte[] addressWithoutPadding =  CryptoHelper.RemoveLeadingZerosFromAddress(addressWithoutCheckSum);

        byte[] addressCheckSum = Arrays.copyOfRange(addressBytes,addressBytes.length -4 , addressBytes.length);
        checksum.update(addressWithoutPadding, 0, addressWithoutPadding.length);

        byte[] checksumValue = ByteBuffer.allocate(4).putInt((int)checksum.getValue()).array();
        return Arrays.equals(checksumValue,addressCheckSum);

    }
}
