import coti.crypto.AlphaNetProofOfWork;
import io.coti.common.crypto.BaseTransactionWithPrivateKey;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.TransactionData;
import io.coti.fullnode.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@RunWith(SpringRunner.class)
@Slf4j
public class DecentPowTest {

    private String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f83";

    @Test
    public void testPow(){
        ArrayList<BaseTransactionData> bxDataList = new ArrayList<>();
        bxDataList.add(new BaseTransactionWithPrivateKey(new BigDecimal(-10), new Date(), hexPrivateKey));
        TransactionData transactionData = new TransactionData(bxDataList, "test", 80.53, new Date());
        AlphaNetProofOfWork pow = new AlphaNetProofOfWork("abcdefghijk".getBytes(), 0);

    }



}
