import io.coti.common.crypto.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

@Slf4j
@RunWith(SpringRunner.class)
public class CryptoTests {

    @Test
    public void testStringConvertion() {
        byte[] privateKey = {123};
        ECKeyPair pair = ECKeyPair.create(privateKey);
        log.info(CryptoUtils.bytesToHex(pair.getPublicKey().toByteArray()));
        Sign.SignatureData originalSignature = Sign.signMessage("Currency Of The Internet!".getBytes(), pair);
        String signatureString =
                CryptoUtils.convertSignatureToString(originalSignature);

        log.info(signatureString);

        Sign.SignatureData signatureData = CryptoUtils.convertSignatureFromString(signatureString);

        Assert.assertEquals(originalSignature, signatureData);
    }

}
