package utils;

import io.coti.basenode.data.SignatureData;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;

public class CryptoTestUtils {

    private static final String ecSpec = "secp256k1";
    private static final X9ECParameters curve = SECNamedCurves.getByName(ecSpec);
    private static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());

    public static String getPublicKeyFromPrivateKey(String privateKeyHex) {
        byte[] privateKey = DatatypeConverter.parseHexBinary(privateKeyHex);
        ECPoint curvePt = domain.getG().multiply(new BigInteger(1, privateKey));
        curvePt = curvePt.normalize();
        String x = curvePt.getXCoord().toBigInteger().toString(16);
        String y = curvePt.getYCoord().toBigInteger().toString(16);
        return paddingPublicKey(x, y);
    }

    private static String paddingPublicKey(String x, String y) {
        String paddingLetter = "0";

        while (x.length() < 64) {
            x = paddingLetter + x;
        }

        while (y.length() < 64) {
            y = paddingLetter + y;
        }
        return x + y;
    }

    public static SignatureData signBytes(byte[] bytesToSign, String privateKeyHex) {

        byte[] privateKey = DatatypeConverter.parseHexBinary(privateKeyHex);
        ECDSASigner signer = new ECDSASigner();
        signer.init(true, new ECPrivateKeyParameters(new BigInteger(1, privateKey), domain));
        BigInteger[] signature = signer.generateSignature(bytesToSign);
        BigInteger r = signature[0];
        BigInteger s = signature[1];
        return new SignatureData(r.toString(16), s.toString(16));
    }

}
