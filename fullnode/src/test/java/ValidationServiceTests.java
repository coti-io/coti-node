import io.coti.common.crypto.CryptoUtils;
import io.coti.common.data.Hash;
import io.coti.fullnode.service.ValidationService;
import io.coti.fullnode.service.interfaces.IValidationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import java.math.BigInteger;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = ValidationService.class
)
public class ValidationServiceTests {

    @Autowired
    private IValidationService validationService;

    @Test
    public void validateSenderAddress_CorrectAddress_AssertTrue() {
        String seed1 = "1010";
        String message = "This is my message";
        BigInteger seed1PrivateKey1 = CryptoUtils.generatePrivateKey(seed1, 1);
        BigInteger seed1Address1 = CryptoUtils.getPublicKeyFromPrivateKey(seed1PrivateKey1);
        ECKeyPair pair = ECKeyPair.create(seed1PrivateKey1);

        Assert.assertTrue(
                validationService.validateSenderAddress(
                        message,
                        Sign.signMessage(message.getBytes(), pair),
                        new Hash(seed1Address1.toByteArray())));
    }

    @Test
    public void validateSenderAddress_IncorrectAddress_AssertFalse() {
        String seed1 = "1010";
        String message = "This is my message";
        BigInteger seed1PrivateKey1 = CryptoUtils.generatePrivateKey(seed1, 1);
        ECKeyPair pair = ECKeyPair.create(seed1PrivateKey1);
        Hash incorrectAddress = new Hash("AAAAAA");
        Assert.assertFalse(
                validationService.validateSenderAddress(
                        message,
                        Sign.signMessage(message.getBytes(), pair),
                        incorrectAddress));
    }
}
