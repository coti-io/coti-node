import io.coti.common.services.ValidationService;
import io.coti.common.services.interfaces.IValidationService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = ValidationService.class
)
public class ValidationServiceTests {

    @Autowired
    private IValidationService validationService;
/*
    @Test
    public void validateSenderAddress_CorrectAddress_AssertTrue() {
        Date date = new Date ();
        BaseTransactionData baseTransaction =
                new BaseTransactionData("562cd1a12533d2e3826019fa72cd534037dc9738fbe40fa6cfbca2a4ea24bd1bd2c4da221e59cf1f276d71424f2969fef6cf20c475840e63241426d8542ee8904DD5D4BF",
                                        new BigDecimal(-30),new SignatureData("6b5a1c3148acd417b50bd1f8a7b860e3c20b8ecdd0a0863d448d06c5eb061ee","2c7be9935c42dd72b05c7f1c4ed606d198df9723846b3bbe578ce6a18e66d997",date.setTime(1530781319130L )));
                        );
        String seed1 = "1010";
        String message = "This is my message";
        BigInteger seed1PrivateKey1 = CryptoUtils.generatePrivateKey(seed1, 1);
        BigInteger seed1Address1 = CryptoUtils.getPublicKeyFromHexString(seed1PrivateKey1);
        ECKeyPair pair = ECKeyPair.create(seed1PrivateKey1);

        Assert.assertTrue(
                validationService.validateBaseTransaction(
                        message,
                        Sign.signMessage(message.getBytes(), pair),
                        new Hash(seed1Address1.toByteArray())));
    }*/

    /*@Test
    public void validateSenderAddress_IncorrectAddress_AssertFalse() {
        String seed1 = "1010";
        String message = "This is my message";
        BigInteger seed1PrivateKey1 = CryptoUtils.generatePrivateKey(seed1, 1);
        ECKeyPair pair = ECKeyPair.create(seed1PrivateKey1);
        Hash incorrectAddress = new Hash("AAAAAA");
        Assert.assertFalse(
                validationService.validateBaseTransaction(
                        message,
                        Sign.signMessage(message.getBytes(), pair),
                        incorrectAddress));
    }*/
}
