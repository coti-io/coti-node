import io.coti.basenode.crypto.BaseTransactionWithPrivateKey;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.zerospend.crypto.TransactionCyptoCreator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class ZeroSpendTests {

    @Test
    public void CreateAndSignTransaction() {

        String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e";
        ArrayList<BaseTransactionData> bxDataList = new ArrayList<>();
        bxDataList.add(new BaseTransactionWithPrivateKey(new BigDecimal(-10), new Date(), hexPrivateKey));
        bxDataList.add(new BaseTransactionData(
                new Hash("19ecfb8159ee64f3907f2305fb52737f96efb3ed5cd8893bb9e79a98abd534ae331b0096f0fb5e1e18f9128231ee330cd025a243cc0e98aac40bdc7475d43d318763c3b0"),
                new BigDecimal(10), new Date()));

        TransactionData tx = new TransactionData(bxDataList, "test", 80.53, new Date());
        TransactionCyptoCreator txCreator = new TransactionCyptoCreator(tx);
        txCreator.signTransaction();
        Assert.assertTrue(txCreator.getTransactionCryptoWrapper().isTransactionValid());
    }
}