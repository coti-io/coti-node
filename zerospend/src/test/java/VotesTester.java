import io.coti.common.communication.interfaces.ITransactionSender;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import io.coti.zerospend.ZeroSpendConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ZeroSpendConfiguration.class)
@TestPropertySource(locations="classpath:application.properties", properties = "resetDatabase=true")
@Slf4j
public class VotesTester {
    @Autowired
    private ITransactionSender transactionSender;

    @Test
    public void testDspToZeroSpendVote(){
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(new BaseTransactionData(new Hash("BE"), new BigDecimal(-150)
                ,new Hash("BE"),new SignatureData("",""),new Date()));
        TransactionData transactionData = new TransactionData(baseTransactionDataList, new Hash("ABCD"),
                "TEST Description", 50.5, new Date());
        transactionSender.sendTransaction(transactionData);
    }
}
