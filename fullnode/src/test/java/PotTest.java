import io.coti.basenode.crypto.BaseTransactionWithPrivateKey;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.crypto.TransactionCyptoCreator;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.fullnode.FullNodeApplication;
import io.coti.pot.ProofOfTrust;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FullNodeApplication.class)
@Slf4j
public class PotTest {

    private String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f83";
    private byte[] targetDifficulty = parseHexBinary("00F000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000");


    @Autowired
    private TransactionCrypto transactionCrypto;

    @Test
    public void testPow() {
        ArrayList<BaseTransactionData> bxDataList = new ArrayList<>();
        bxDataList.add(new BaseTransactionWithPrivateKey(new BigDecimal(-10), new Date(), hexPrivateKey));
        TransactionData transactionData = new TransactionData(bxDataList, "test", 80.53, new Date());
        TransactionCyptoCreator txCreator = new TransactionCyptoCreator(transactionData);
        txCreator.signTransaction();
        transactionCrypto.signMessage(transactionData);
        ProofOfTrust pow = new ProofOfTrust(0);  // setup
        int[] nonces = pow.hash(transactionData.getHash().getBytes(), targetDifficulty); //calc
        boolean valid = pow.verify(transactionData.getHash().getBytes(), nonces, targetDifficulty); // verify - o(1)
        Assert.assertTrue(valid);

    }


}
